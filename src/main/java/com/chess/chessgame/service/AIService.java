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

            PRIMARY GOAL OF THE GAME:
            Your single objective is to CHECKMATE the WHITE king. The game of chess is won by delivering checkmate, not by winning material, occupying squares, or making nice positional moves: those are only tools that should serve the mating plan. Every move you select must be evaluated through the question: "Does this move bring me closer to checkmating the white king?" If you have a material advantage and are not actively pressing for mate, you are playing badly. Do not coast, do not shuffle pieces, do not let the game drift. Hunt the white king.

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

            CONVERTING A MATERIAL ADVANTAGE INTO MATE:
            - When you are ahead by 3 or more points of material, you ARE winning. Switch into MATING MODE: every move should coordinate pieces toward the white king.
            - Do NOT keep trading pieces down to a quiet endgame just because the trades look favorable. Keep enough attacking material on the board to deliver mate.
            - Do NOT trade your queen unless the trade forces mate or leads to a trivially winning king-and-pawn endgame.
            - Bring ALL your pieces into the attack before sacrificing material. A queen alone rarely mates a defended king; a queen plus a rook or knight usually does.
            - Open lines toward the white king even at the cost of a pawn or two: an exposed king is worth more than a pawn.

            MATING PATTERNS TO RECOGNIZE:
            - Back-rank mate: rook or queen on the 1st rank when the white king is blocked by its own pawns on f2/g2/h2.
            - Smothered mate: knight delivers mate while the king is surrounded by its own pieces.
            - Ladder mate with two rooks alternating ranks, pushing the king to the edge.
            - Queen-and-king vs lone king: drive the king to the edge with the queen one knight-move away, then bring your king up. Never give stalemate.
            - Rook-and-king vs lone king: cut off the king with the rook and walk your king in.
            - Heavy pieces invading the 7th/8th rank through an open file.
            - Two-bishop battery on adjacent diagonals aimed at the castled white king.

            PHASE AWARENESS:
            - Opening (~moves 1-10): develop minor pieces with tempo, control the center, castle, but already aim minor pieces at squares around the white king.
            - Middlegame: this is when most mating attacks start. Look for sacrifices on f2/g2/h2 if white castled kingside, on c2/b2 if white castled queenside. Create concrete threats every move.
            - Endgame: if ahead in material, drive directly toward mate. Do not shuffle, do not stall. Only mate matters.

            STALEMATE AVOIDANCE:
            - Before playing a move that drastically restricts white's options, verify white still has at least one legal reply. NEVER deliver stalemate when you are winning. If your candidate move risks stalemate, choose a slower move that keeps a legal reply for white and still progresses toward mate.

            CHECK DISCIPLINE (do NOT give pointless checks):
            - Giving check is NOT automatically a good move. A check is only worth playing if at least ONE of these conditions is true:
              (a) The check leads to forced checkmate within a clearly visible sequence (mate in 1, 2, or 3), OR
              (b) The check forces a concrete tactical gain on the next move (winning material, decisive king exposure, fork, pin, deflection), OR
              (c) The piece delivering the check CANNOT be captured by white, OR if it can be captured, the recapture is favorable to BLACK (i.e. you do not lose the checking piece for less value, or the recapture itself opens a winning line).
            - Before playing a check, run this two-step safety test:
              1. List every white piece that attacks the destination square of your checking piece.
              2. If the checking piece is undefended OR defended by a lower-value piece than the cheapest white attacker, the check loses material and MUST be discarded unless it leads to forced mate (rule (a)) or wins more than it loses (rule (b)).
            - Specifically: do NOT throw the queen into a check on a square defended by a white pawn, knight, or bishop just to "give check". Losing the queen for a single pawn is a blunder, no matter how aggressive the check looks.
            - Random checks that the white king simply walks away from, leaving you with a hanging or exchanged piece, are STRICTLY FORBIDDEN. If the only effect of your check is "white king moves one square and your piece is now lost or traded down", choose a different move.
            - When in doubt, prefer a quiet move that improves coordination, doubles rooks, opens a file, or restricts the white king's escape squares over a flashy but unsound check.

            PUSH YOUR PAWNS TO PROMOTION (BLACK promotes on rank 1):
            - A black pawn that reaches rank 1 must be promoted. The promotion piece is appended to the UCI move as a lowercase letter: 'q' (queen), 'r' (rook), 'b' (bishop), 'n' (knight). Examples: a black pawn from b2 to b1 with queen promotion is "b2b1q"; a capture promotion d2xc1 with queen is "d2c1q".
            - ALWAYS promote to QUEEN (suffix 'q'), unless promoting to knight delivers immediate checkmate that the queen cannot give. The default and overwhelming preference is queen.
            - Treat your own passed pawns as priority assets. A passed black pawn on rank 4/3/2 is often more valuable than another piece because it threatens to become a queen.
            - Identify passed pawns each move: a black pawn is "passed" if there are no white pawns on the same file or on the two adjacent files between the pawn's current rank and rank 1.
            - When you have a passed pawn, plan its push: clear blockaders (capture or chase off any white piece sitting in front of it), defend the pawn with your own pieces, and use checks/threats elsewhere to gain tempo for the push.
            - If a single move can promote AND avoid losing the new queen to a forced capture, play that move. Always include the 'q' suffix.
            - In any endgame where you have a passed pawn and the white king is far, racing the pawn to promotion is often stronger than chasing material.

            STOP WHITE PAWN PROMOTION:
            - Track every white pawn on ranks 5, 6, and 7. Any white pawn on rank 7 is one move from queening and must be neutralized this move if possible.
            - Priority defensive actions against an advancing white pawn (in order): (a) capture the pawn with any black piece, even at the cost of a minor exchange; (b) capture the piece that defends the promotion square; (c) place a black piece directly on the promotion square (file letter + rank 8) so the white pawn cannot promote without being immediately recaptured; (d) place a black piece on the square in front of the pawn (blockader, preferably a knight or bishop) so it cannot advance.
            - If a white pawn would promote next move and you cannot stop it, AND you cannot capture the resulting queen, treat that as a losing threat: prefer a move that creates a counter-threat at least as strong (your own promotion, a check, or a winning capture).
            - Trading a knight or bishop (value 3) to eliminate a white pawn that is about to promote (value 9 once queened) is a favorable trade. Do not hesitate.
            - In endgames with passed pawns on both sides, evaluate which pawn queens first by counting tempos. If white queens first, your move must either (i) stop it, (ii) queen on the same move with check, or (iii) start a forcing sequence that wins the new white queen.

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


