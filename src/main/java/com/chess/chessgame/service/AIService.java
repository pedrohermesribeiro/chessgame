package com.chess.chessgame.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ReasoningEffort;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

@Service
public class AIService {

    private final OpenAIClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String HARD_MODEL = "gpt-5";
    private static final String EASY_MODEL = "gpt-5-mini";
    private static final int HARD_MAX_ATTEMPTS = 3;

    // Lê a chave do application.properties (openai.api.key)
    public AIService(@Value("${openai.api.key}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    /**
     * Gera um lance sugerido pelo modelo mais barato (usado no modo "vsAIo3").
     */
    public String suggestMove(String boardStateJson, String sideToMove) {
        return suggestMove(boardStateJson, sideToMove, "", "N/A", false);
    }

    public String suggestMove(String boardStateJson, String sideToMove, String moveHistory, String fen, boolean inCheck) {
        String prompt = """
            You are a chess engine embedded in a Java game.

            CONTEXT:
            - Board JSON: provided below
            - Move history (UCI): %s
            - FEN: %s
            - In check: %s
            - To move: %s

            Rules:
            - Return ONLY ONE move in UCI format (4-5 chars: e2e4, g1f3, e7e8q, e1g1 etc.). No other text.
            - Move must be legal.
            - Consider history, position evaluation, and tactics.
            """.formatted(
                moveHistory != null && !moveHistory.isEmpty() ? moveHistory : "None",
                fen,
                inCheck,
                sideToMove
            );

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .model(EASY_MODEL)
            .addUserMessage(prompt + "\nBoard JSON:\n" + boardStateJson)
            .build();

        ChatCompletion completion = client.chat().completions().create(params);
        Optional<String> content = completion.choices().get(0).message().content();
        return content.orElseThrow(() -> new IllegalStateException("OpenAI did not return a move")).trim();
    }

    /**
     * Retorna ate 3 lances candidatos (ordenados por forca decrescente) para
     * o modo chatGPT-Hard. O chamador (GameService) faz o rerank tatico final via
     * evaluateBoard e escolhe o vencedor. A lista sempre esta contida em legalMoves.
     *
     * @param effort quantidade de raciocinio a gastar. O chamador decide com base
     *               em sinais taticos (posicao em xeque, endgame, desequilibrio
     *               material). Se null, cai em MEDIUM.
     */
    public List<String> suggestHardMoveCandidates(
            String sideToMove,
            String moveHistory,
            String fen,
            boolean inCheck,
            List<String> legalMoves,
            String tacticalFacts,
            ReasoningEffort effort) {

        if (legalMoves == null || legalMoves.isEmpty()) {
            throw new IllegalStateException("Nenhum lance legal disponivel para " + sideToMove);
        }

        String systemPrompt = buildHardSystemPrompt();
        String userPrompt = buildHardUserPrompt(sideToMove, moveHistory, fen, inCheck, legalMoves, tacticalFacts);

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
            .model(HARD_MODEL)
            .reasoningEffort(effort != null ? effort : ReasoningEffort.MEDIUM)
            .addSystemMessage(systemPrompt)
            .addUserMessage(userPrompt);

        String lastRawAnswer = null;
        String lastError = null;

        for (int attempt = 1; attempt <= HARD_MAX_ATTEMPTS; attempt++) {
            if (attempt > 1 && lastRawAnswer != null && lastError != null) {
                paramsBuilder
                    .addAssistantMessage(lastRawAnswer)
                    .addUserMessage(buildRetryFeedback(lastError, legalMoves));
            }

            ChatCompletion completion = client.chat().completions().create(paramsBuilder.build());
            String raw = completion.choices().get(0).message().content()
                    .orElse("")
                    .trim();
            lastRawAnswer = raw;

            completion.usage().ifPresent(u -> {
                long reasoning = u.completionTokensDetails()
                    .flatMap(d -> d.reasoningTokens())
                    .orElse(0L);
                System.out.println("[chatGPT-Hard] tokens in=" + u.promptTokens()
                    + " out=" + u.completionTokens()
                    + " reasoning=" + reasoning
                    + " total=" + u.totalTokens());
            });

            System.out.println("[chatGPT-Hard] tentativa " + attempt + " resposta bruta: " + truncate(raw, 200));

            List<String> parsed = parseCandidates(raw);
            List<String> filtered = filterLegal(parsed, legalMoves);

            if (!filtered.isEmpty()) {
                return filtered;
            }

            lastError = parsed.isEmpty()
                ? "Sua resposta nao veio no formato JSON esperado {\"candidates\":[\"...\"]}."
                : "Todos os lances retornados sao ilegais: " + parsed + ".";
        }

        throw new IllegalStateException(
            "IA nao produziu nenhum lance legal apos " + HARD_MAX_ATTEMPTS + " tentativas. Ultima resposta: " + lastRawAnswer);
    }

    private String buildHardSystemPrompt() {
        return """
            You are a grandmaster-level chess engine playing BLACK. Goal: checkmate the WHITE king.

            RESPONSE (STRICT JSON, no prose, no code fences):
            {"candidates": ["<uci>"]}
            - EXACTLY 1 UCI move (4 or 5 chars, e.g. e7e5, e2e1q).
            - MUST be one of the LEGAL MOVES listed in the user message.

            Priorities (in order):
            1. If in check, escape with the best follow-up.
            2. Find forced mate (in 1-3) and play it.
            3. Capture hanging enemy pieces flagged in TACTICAL FACTS.
            4. Coordinate pieces toward the white king; open attacking lines.
            5. Promote passed pawns (default: queen, suffix "q").
            6. Avoid checks that just lose material. Avoid stalemate when winning.
            """;
    }

    private String buildHardUserPrompt(String sideToMove, String moveHistory,
                                       String fen, boolean inCheck, List<String> legalMoves, String tacticalFacts) {
        StringBuilder sb = new StringBuilder();
        sb.append("POSITION:\n");
        sb.append("- FEN: ").append(fen != null ? fen : "N/A").append('\n');
        sb.append("- Side to move: ").append(sideToMove).append('\n');
        sb.append("- In check: ").append(inCheck).append('\n');
        sb.append("- Move history (UCI): ")
          .append(moveHistory != null && !moveHistory.isEmpty() ? moveHistory : "No previous moves")
          .append('\n');
        sb.append('\n');
        if (tacticalFacts != null && !tacticalFacts.isBlank()) {
            sb.append("TACTICAL FACTS (already computed by server, trust these):\n");
            sb.append(tacticalFacts).append('\n');
            sb.append('\n');
        }
        sb.append("LEGAL MOVES (you MUST choose exactly from this list, no other move is accepted):\n");
        sb.append(String.join(", ", legalMoves)).append('\n');
        sb.append('\n');
        sb.append("Reply now with STRICT JSON: {\"candidates\":[\"<uci>\"]}");
        return sb.toString();
    }

    private String buildRetryFeedback(String reason, List<String> legalMoves) {
        return "Your previous answer was rejected. Reason: " + reason
            + " Reply again with STRICT JSON {\"candidates\":[\"<uci>\"]} choosing EXACTLY 1 move from the LEGAL MOVES list ONLY. "
            + "Remember: no prose, no code fences, no invented moves. Legal moves reminder: "
            + String.join(", ", legalMoves);
    }

    private List<String> parseCandidates(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        String cleaned = stripCodeFences(raw);
        try {
            JsonNode root = mapper.readTree(cleaned);
            JsonNode arr = root.get("candidates");
            if (arr != null && arr.isArray()) {
                List<String> out = new ArrayList<>();
                for (JsonNode n : arr) {
                    if (n.isTextual()) {
                        String v = n.asText().trim();
                        if (!v.isEmpty()) out.add(v);
                    }
                }
                return out;
            }
        } catch (Exception ignored) {
            // fallback: talvez o modelo tenha respondido so com um UCI puro
        }
        String single = cleaned.replaceAll("[^a-h1-8qrbnQRBN]", "");
        if (single.length() == 4 || single.length() == 5) {
            return List.of(single);
        }
        return List.of();
    }

    private List<String> filterLegal(List<String> parsed, List<String> legalMoves) {
        if (parsed == null || parsed.isEmpty() || legalMoves == null || legalMoves.isEmpty()) {
            return Collections.emptyList();
        }
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (String c : parsed) {
            if (c == null) continue;
            String candidate = c.trim();
            if (legalMoves.contains(candidate)) {
                out.add(candidate);
            }
        }
        return new ArrayList<>(out);
    }

    private String stripCodeFences(String s) {
        String t = s.trim();
        if (t.startsWith("```")) {
            int firstNl = t.indexOf('\n');
            if (firstNl > 0) t = t.substring(firstNl + 1);
            if (t.endsWith("```")) t = t.substring(0, t.length() - 3);
        }
        return t.trim();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...(truncated)";
    }
}
