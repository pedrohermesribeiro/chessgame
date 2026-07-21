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
     */
    public List<String> suggestHardMoveCandidates(
            String boardStateJson,
            String sideToMove,
            String moveHistory,
            String fen,
            boolean inCheck,
            List<String> legalMoves,
            String tacticalFacts) {

        if (legalMoves == null || legalMoves.isEmpty()) {
            throw new IllegalStateException("Nenhum lance legal disponivel para " + sideToMove);
        }

        String systemPrompt = buildHardSystemPrompt();
        String userPrompt = buildHardUserPrompt(boardStateJson, sideToMove, moveHistory, fen, inCheck, legalMoves, tacticalFacts);

        ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
            .model(HARD_MODEL)
            .reasoningEffort(ReasoningEffort.HIGH)
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
            You are a grandmaster-level chess engine embedded in a Java chess app. You always play as BLACK in this mode.

            ABSOLUTE GOAL:
            Deliver CHECKMATE to the WHITE king. Material, tempo, space, and piece activity are only tools that serve the mating plan. Every move must be evaluated by the question: "Does this move bring me closer to checkmating the white king?" Do not shuffle, do not stall, hunt the king.

            RESPONSE FORMAT (STRICT):
            You MUST reply with STRICT JSON only, no prose, no markdown, no code fences:
            {"candidates": ["<uci1>", "<uci2>", "<uci3>"]}
            - EXACTLY 1 to 3 candidates.
            - Each MUST be a UCI string (4 or 5 chars, e.g. e7e5, g8f6, e2e1q).
            - Order them from strongest (index 0) to weakest.
            - Each MUST be EXACTLY one of the strings from the LEGAL MOVES list you will receive in the user message.
            - Do NOT invent moves, do NOT normalize case, do NOT reorder the string characters, do NOT add explanations.

            STRATEGY (in priority):
            1. If in check: play a legal move that resolves the check with the best follow-up.
            2. Look for forced mate (mate in 1, 2, 3). If seen, play it.
            3. Win material with favorable exchanges; capture hanging enemy pieces flagged in TACTICAL FACTS.
            4. Coordinate pieces toward the white king; open lines against the castled position.
            5. Push passed pawns toward promotion (rank 1 for BLACK). Default promotion is queen ("q" suffix).
            6. Neutralize any white pawn on rank 6 or 7: capture, block, or attack the promotion square.
            7. When ahead in material by 3+, switch to MATING MODE: keep enough attacking pieces, do not trade the queen.

            CHECK DISCIPLINE:
            - A check is only worth playing if it forces mate, wins material, or the checking piece cannot be captured (or the recapture is favorable).
            - Never sacrifice the queen for a check that just loses the queen for a pawn.

            STALEMATE AVOIDANCE:
            - When winning, verify white always has at least one legal reply. Avoid stalemate.

            You will now receive the position and the exact LEGAL MOVES list. Pick from that list only.
            """;
    }

    private String buildHardUserPrompt(String boardStateJson, String sideToMove, String moveHistory,
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
        sb.append("BOARD JSON:\n").append(boardStateJson).append('\n');
        sb.append('\n');
        if (tacticalFacts != null && !tacticalFacts.isBlank()) {
            sb.append("TACTICAL FACTS (already computed by server, trust these):\n");
            sb.append(tacticalFacts).append('\n');
            sb.append('\n');
        }
        sb.append("LEGAL MOVES (you MUST choose exactly from this list, no other move is accepted):\n");
        sb.append(String.join(", ", legalMoves)).append('\n');
        sb.append('\n');
        sb.append("Reply now with STRICT JSON: {\"candidates\":[\"<uci1>\", ...]}");
        return sb.toString();
    }

    private String buildRetryFeedback(String reason, List<String> legalMoves) {
        return "Your previous answer was rejected. Reason: " + reason
            + " Reply again with STRICT JSON {\"candidates\":[\"<uci>\", ...]} choosing 1 to 3 moves from the LEGAL MOVES list ONLY. "
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
