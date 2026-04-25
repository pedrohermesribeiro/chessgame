package com.chess.chessgame.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

@Service
public class AIService {

    private final OpenAIClient client;

    // Lê a chave do application.properties (openai.api.key)
    public AIService(@Value("${openai.api.key}") String apiKey) {
        this.client = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    /**
     * Gera um lance sugerido pelo modelo o3-mini.
     *
     * @param boardStateJson JSON do tabuleiro (mesmo formato que você serializa em Game.boardState)
     * @param sideToMove "WHITE" ou "BLACK"
     * @return lance no formato "e2e4"
     */
    public String suggestMove(String boardStateJson, String sideToMove) {
        // Backward compatibility - call with empty context
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

        return suggestMoveWithPrompt(prompt, boardStateJson);
    }

    public String suggestHardMove(String boardStateJson, String sideToMove, String moveHistory, String fen, boolean inCheck) {
        String prompt = """
            You are a grandmaster chess engine embedded in a Java Spring Boot chess application.

            CONTEXT PROVIDED:
            - Board state: JSON map of positions to pieces (see below)
            - Move history (in UCI format): %s
            - Current FEN notation: %s
            - Black is in check: %s
            - Side to move: %s (you are playing as BLACK in hard mode)

            Rules for your response:
            - Return ONLY the move in exact UCI notation (4 or 5 characters). Examples: "e2e4", "g1f3", "e7e8q", "e1g1" (castling), "e8c8" (queenside).
            - NO explanations, no text, no markdown, no newlines, no extra spaces.
            - The move MUST be 100%% legal based on the provided board, history, and chess rules.
            - Validate against castling rights, en passant, promotions, pins, checks.

            STRATEGY (prioritized):
            1. If in check, find the best legal move that resolves the check (capture, block, or king move).
            2. Look for checkmate or forced wins.
            3. Capture unprotected or higher-value pieces with favorable exchanges.
            4. Prioritize king safety, piece activity, center control, development.
            5. Use pawn structure, avoid isolated/doubled pawns when possible.
            6. Consider the move history to recognize patterns, avoid blunders, build on previous plans.
            7. Standard piece values: Pawn=1, Knight/Bishop=3, Rook=5, Queen=9, King=100.

            Use the full context (FEN + history + board JSON) to evaluate the position accurately like a strong player would.
            Play the single best move for BLACK.
            """.formatted(
                moveHistory != null && !moveHistory.isEmpty() ? moveHistory : "No previous moves",
                fen != null ? fen : "N/A",
                inCheck,
                sideToMove
            );

        return suggestMoveWithPrompt(prompt, boardStateJson);
    }

    private String suggestMoveWithPrompt(String prompt, String boardStateJson) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
            .model("gpt-5-mini")
            .addUserMessage(prompt + "\nBoard JSON:\n" + boardStateJson)
            .build();

        ChatCompletion completion = client.chat().completions().create(params);
        Optional<String> content = completion.choices().get(0).message().content();
        String raw = completion._choices().toString();
        Object raw1 = completion._choices().asObject();
        System.err.println("retorno da IA: " + raw + " Objeto " + raw1 + " String " + content);
        return content.orElseThrow(() -> new IllegalStateException("OpenAI did not return a move")).trim();
    }
}

	
	


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/*

	OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    ResponseCreateParams params = ResponseCreateParams.builder()
            .input("Say this is a test")
            .model("gpt-5-nano")
            .build();

    Response response = client.responses().create(params);
    //System.out.println(response.outputText());
    

    public String getAIMoveO3(String boardStateJson, String lastMove) {

        String prompt = """
Você é uma IA de xadrez. Receberá:

1. O estado do tabuleiro em JSON
2. O último movimento (se existir)
3. Sua tarefa é devolver APENAS um movimento legal em notação UCI, ex: "e7e5".

REGRAS:
- Não explique nada.
- Não escreva frases.
- Não envie código.
- Apenas responda com um único movimento válido.
- Não invente movimentos impossíveis.
""";

        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("o3-mini")  // IA mais barata
                .messages(java.util.List.of(
                        ChatCompletionMessage.user(prompt),
                        ChatCompletionMessage.user("Tabuleiro atual (JSON): " + boardStateJson),
                        ChatCompletionMessage.user("Último movimento: " + lastMove),
                        ChatCompletionMessage.user("Responda apenas com o próximo movimento UCI.")
                ))
                .maxTokens(10) // super baixo custo
                .build();

        ChatCompletionResponse response = openai.chat().complete(request);
        return response.choices().get(0).message().content().trim();
    } */


