package com.chess.chessgame.config;

import com.chess.chessgame.model.CheckmatePattern;
import com.chess.chessgame.repository.CheckmatePatternRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CheckmateDataLoader {

    @Bean
    CommandLineRunner initCheckmatePatterns(CheckmatePatternRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                // Mate do Louco
                CheckmatePattern foolMate = new CheckmatePattern();
                foolMate.setName("Mate do Louco");
                foolMate.setWinningColor("BLACK");
                foolMate.setMoves(List.of("f2f3", "e7e5", "g2g4", "d8h4#"));

                // Mate Pastor
                CheckmatePattern scholarMate = new CheckmatePattern();
                scholarMate.setName("Mate Pastor");
                scholarMate.setWinningColor("WHITE");
                scholarMate.setMoves(List.of("e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7#"));
                
                // Mate Pastor
                CheckmatePattern scholarMateI = new CheckmatePattern();
                scholarMate.setName("Mate PastorI");
                scholarMate.setWinningColor("WHITE");
                scholarMate.setMoves(List.of("e7e6", "d8h4", "f8c5", "h4f2#"));

                // Mate de Anastasia (simplificado)
                CheckmatePattern anastasia = new CheckmatePattern();
                anastasia.setName("Mate de Anastasia");
                anastasia.setWinningColor("WHITE");
                anastasia.setMoves(List.of("e2e4", "e7e5", "g1f3", "b8c6", "f1c4", "g8f6", "f3g5", "f6e4", "g5f7#"));

                repo.saveAll(List.of(foolMate, scholarMate, scholarMateI, anastasia));
            }
        };
    }
}
