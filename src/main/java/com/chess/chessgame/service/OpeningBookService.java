package com.chess.chessgame.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

/**
 * Livro de aberturas para o lado BLACK no modo chatGPT-Hard.
 *
 * A chave e o historico de lances (UCI) separado por espaco, sempre terminando
 * no ultimo lance jogado pelas brancas. O valor e a lista de respostas
 * candidatas que a IA pode jogar, escolhidas para dar posicoes solidas
 * seguindo teoria conhecida.
 *
 * O objetivo dessa camada e (a) economizar chamadas caras ao gpt-5 nos
 * primeiros lances de teoria e (b) evitar aberturas ruins que o modelo
 * poderia inventar. Fora do livro, o servico devolve {@link #NO_BOOK_MOVE} e
 * o fluxo normal da IA e acionado.
 */
@Service
public class OpeningBookService {

    /** Marker de "posicao nao esta no livro". */
    public static final String NO_BOOK_MOVE = null;

    /** Historico max (ply) coberto pelo livro; alem disso deixa a IA decidir. */
    private static final int MAX_BOOK_PLY = 10;

    private final Map<String, List<String>> book = new HashMap<>();
    private final Random random = new Random();

    public OpeningBookService() {
        // Respostas ao primeiro lance branco (ply 1)
        put("e2e4", "c7c5", "e7e5", "e7e6", "c7c6", "d7d5");
        put("d2d4", "g8f6", "d7d5", "e7e6", "c7c5");
        put("c2c4", "g8f6", "e7e5", "c7c5");
        put("g1f3", "g8f6", "d7d5", "c7c5");
        put("b2b3", "e7e5", "d7d5", "g8f6");
        put("f2f4", "d7d5", "g8f6", "c7c5");
        put("b1c3", "d7d5", "g8f6");

        // ---- Ramos apos e2e4 ----
        // Ruy Lopez / Italiana (1.e4 e5 2.Nf3 Nc6 3.Bb5 ou 3.Bc4)
        put("e2e4 e7e5 g1f3", "b8c6", "g8f6", "d7d6");
        put("e2e4 e7e5 g1f3 b8c6 f1b5", "a7a6");           // Berlin / anti-Ruy
        put("e2e4 e7e5 g1f3 b8c6 f1b5 a7a6 b5a4", "g8f6");
        put("e2e4 e7e5 g1f3 b8c6 f1c4", "g8f6", "f8c5");   // Italiana
        // King's Gambit
        put("e2e4 e7e5 f2f4", "e5f4", "f8c5");

        // Siciliana Najdorf/Kan/Taimanov generico
        put("e2e4 c7c5 g1f3", "d7d6", "b8c6", "e7e6", "g7g6");
        put("e2e4 c7c5 g1f3 d7d6 d2d4", "c5d4");
        put("e2e4 c7c5 g1f3 d7d6 d2d4 c5d4 f3d4", "g8f6");
        put("e2e4 c7c5 g1f3 d7d6 d2d4 c5d4 f3d4 g8f6 b1c3", "a7a6"); // Najdorf
        put("e2e4 c7c5 b1c3", "b8c6", "g8f6");             // Closed Sicilian
        // Alapin
        put("e2e4 c7c5 c2c3", "d7d5", "g8f6");

        // Francesa
        put("e2e4 e7e6 d2d4", "d7d5");
        put("e2e4 e7e6 d2d4 d7d5 b1c3", "f8b4", "g8f6");   // Winawer/Classica
        put("e2e4 e7e6 d2d4 d7d5 e4e5", "c7c5");            // Avancada
        put("e2e4 e7e6 d2d4 d7d5 e4d5", "e6d5");            // Trocada

        // Caro-Kann
        put("e2e4 c7c6 d2d4", "d7d5");
        put("e2e4 c7c6 d2d4 d7d5 b1c3", "d5e4");           // Classica
        put("e2e4 c7c6 d2d4 d7d5 e4d5", "c6d5");           // Trocada
        put("e2e4 c7c6 d2d4 d7d5 e4e5", "c8f5");           // Avancada

        // Escandinava
        put("e2e4 d7d5 e4d5", "d8d5", "g8f6");
        put("e2e4 d7d5 e4d5 d8d5 b1c3", "d5a5", "d5d6");

        // ---- Ramos apos d2d4 ----
        put("d2d4 g8f6 c2c4", "e7e6", "g7g6", "c7c5");
        put("d2d4 g8f6 c2c4 e7e6 b1c3", "f8b4");            // Nimzo-Indiana
        put("d2d4 g8f6 c2c4 g7g6 b1c3", "f8g7");            // King's Indian
        put("d2d4 g8f6 c2c4 g7g6 b1c3 f8g7 e2e4", "d7d6");
        put("d2d4 g8f6 g1f3", "g7g6", "e7e6", "d7d5");
        // Queen's Gambit
        put("d2d4 d7d5 c2c4", "e7e6", "c7c6", "d5c4");
        put("d2d4 d7d5 c2c4 e7e6 b1c3", "g8f6");            // QGD
        put("d2d4 d7d5 c2c4 c7c6 b1c3", "g8f6");            // Slav
        put("d2d4 d7d5 c2c4 d5c4 g1f3", "g8f6");            // QGA
        // Dutch
        put("d2d4 f7f5 c2c4", "g8f6");

        // ---- Ramos apos c2c4 (English) ----
        put("c2c4 e7e5 b1c3", "g8f6");
        put("c2c4 g8f6 b1c3", "e7e5", "g7g6");

        // ---- Reti / g1f3 ----
        put("g1f3 d7d5 g2g3", "g8f6", "c8f5");
        put("g1f3 g8f6 g2g3", "d7d5", "g7g6");
    }

    private void put(String history, String... replies) {
        book.put(history, new ArrayList<>(Arrays.asList(replies)));
    }

    /**
     * Retorna um lance do livro para o historico atual, ou {@link #NO_BOOK_MOVE}
     * (null) se a posicao ja saiu do livro / passou do ply maximo.
     *
     * @param moveHistoryUci historico de lances em UCI separado por espaco
     *                       ({@code "e2e4 c7c5 g1f3"}), pode ser null ou vazio.
     * @param legalMoves lances legais atuais para a cor a jogar; usados para
     *                   descartar respostas do livro que porventura fiquem
     *                   incompativeis com a posicao (por seguranca).
     */
    public String lookup(String moveHistoryUci, List<String> legalMoves) {
        if (moveHistoryUci == null) return NO_BOOK_MOVE;
        String key = moveHistoryUci.trim();
        if (key.isEmpty()) return NO_BOOK_MOVE;

        int ply = key.split("\\s+").length;
        if (ply > MAX_BOOK_PLY) return NO_BOOK_MOVE;

        List<String> options = book.get(key);
        if (options == null || options.isEmpty()) return NO_BOOK_MOVE;

        List<String> playable = filterByLegal(options, legalMoves);
        if (playable.isEmpty()) return NO_BOOK_MOVE;

        return playable.get(random.nextInt(playable.size()));
    }

    private List<String> filterByLegal(List<String> options, List<String> legalMoves) {
        if (legalMoves == null || legalMoves.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o != null && legalMoves.contains(o)) {
                out.add(o);
            }
        }
        return out;
    }
}
