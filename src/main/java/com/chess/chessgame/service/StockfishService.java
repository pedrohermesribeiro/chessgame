package com.chess.chessgame.service;

import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class StockfishService {

    // coloque o caminho correto do seu executável
    private static final String STOCKFISH_PATH = "C:/Sistemas/Chessgame/chessgame - Copia/chessgame - Copia/engine/stockfish.exe";

    /**
     * Retorna o melhor movimento em UCI (ex: e2e4, g1f3) usando Stockfish
     */
    public String getBestMove(String fen, int depth) {
        try {
            ProcessBuilder pb = new ProcessBuilder(STOCKFISH_PATH);
            Process engine = pb.start();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(engine.getOutputStream()));
            BufferedReader reader = new BufferedReader(new InputStreamReader(engine.getInputStream()));

            // Envia posição
            writer.write("position fen " + fen + "\n");
            writer.flush();

            // Pede melhor lance
            writer.write("go depth " + depth + "\n");
            writer.flush();

            String line;
            String bestMove = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("bestmove")) {
                    bestMove = line.split(" ")[1];
                    break;
                }
            }

            engine.destroy();
            return bestMove;

        } catch (Exception e) {
            throw new RuntimeException("Erro ao executar Stockfish", e);
        }
    }
}

