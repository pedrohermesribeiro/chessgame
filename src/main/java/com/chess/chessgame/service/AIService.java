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
        String prompt = """
            You are a chess engine embedded in a Java game.

            You receive the chess board as a JSON map:
            {
              "a2": {"type": "PAWN", "color": "WHITE", "valuePiece": 1, "codigo": 1},
              "b1": {"type": "KNIGHT", "color": "WHITE", ...},
              ...
            }

            It is %s to move.

            Rules:
            - Return ONLY ONE move.
            - The move must be a legal chess move.
            - Format: exactly 4 or 5 characters:
              * Normal moves: "e2e4", "g1f3", etc.
              * Castling: "e1g1", "e1c1", "e8g8", or "e8c8".
              * Promotion: 5 chars, e.g. "e7e8q" (promote to queen).
            - Do not explain the move, do not add any text, no spaces, no newlines.
            - Just output the move notation.
            """.formatted(sideToMove);

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
        	.model("gpt-5-mini")
            //.model(ChatModel.O3_MINI)       // modelo mais barato
            .addUserMessage(prompt + "\nBoard JSON:\n" + boardStateJson)
            //.maxTokens(32)                 // é só um lance curto
            .build();

        ChatCompletion completion = client.chat().completions().create(params);

        // Pega o texto da primeira escolha.
        // Se os métodos forem ligeiramente diferentes, deixa o IDE te sugerir;
        // a ideia é: choices[0].message.content[0].text
        Optional<String> content = completion.choices().get(0).message().content();
        String raw = completion._choices().toString();
        Object raw1 = completion._choices().asObject();
            //.get(0)
            //.getMessage()
            //.getContent()
            //.get(0)
            //.getText()
            //.trim();
        System.err.println("retorno da IA: " + raw + " Objeto " + raw1 + " String " + content);
        // Sanitiza só pra garantir (remove espaços/quebras)
        return content.get();
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


