package com.chess.chessgame.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.chessgame.dto.GameDTO;
import com.chess.chessgame.model.CheckmatePattern;
import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.repository.CheckmatePatternRepository;

@Service
public class CheckmateService {

    public static final String CURRENT_CHECKMATE_NAME = "checkmate-atual";

    @Autowired
    private CheckmatePatternRepository checkmateRepo;

    // método que tenta encontrar próxima jogada de checkmate
    public String tryCheckmateFromBook(Long gameId, List<String> moveHistory, PieceColor color) {
        List<CheckmatePattern> patterns = checkmateRepo.findByWinningColor(color.toString());

        for (CheckmatePattern pattern : patterns) {
            List<String> moves = pattern.getMoves();
            if (moveHistory.size() < moves.size()) {
                // verifica se o histórico é prefixo
                boolean match = true;
                for (int i = 0; i < moveHistory.size(); i++) {
                    if (!moves.get(i).equals(moveHistory.get(i))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    // próxima jogada do book
                    return moves.get(moveHistory.size());
                }
            }
        }
        return null; // nada encontrado
    }
    
    public String applyMoveCheckMate(Long id, GameDTO gameDTO) {
        Map<String, Piece> board = gameDTO.getBoard();
        Piece piece = new Piece();
        
        String moveCheck = null;
        CheckmatePattern patterns = checkmateRepo.findById(id).orElse(null);
        if (patterns == null || patterns.getMoves() == null || patterns.getMoves().isEmpty()) {
            return null;
        }
        if(patterns.getIndexLastMove() == patterns.getMoves().size()) {
            patterns.setIndexLastMove(0);
        }
        if(patterns != null) {
            String to = patterns.getMoves().get(patterns.getIndexLastMove());
            piece = board.get(to);
            if(piece != null) {
                return null;
            }
            moveCheck = to;
            patterns.setIndexLastMove(patterns.getIndexLastMove() + 1);
            
            
            checkmateRepo.save(patterns);
        }
        return moveCheck;
    }

    public Optional<CheckmatePattern> findActiveCurrentCheckmatePlan() {
        return checkmateRepo.findFirstByName(CURRENT_CHECKMATE_NAME)
                .filter(CheckmatePattern::isApply);
    }

    public CheckmatePattern saveCurrentCheckmatePlan(List<String> moves) {
        CheckmatePattern pattern = checkmateRepo.findFirstByName(CURRENT_CHECKMATE_NAME)
                .orElseGet(CheckmatePattern::new);
        pattern.setName(CURRENT_CHECKMATE_NAME);
        pattern.setWinningColor(PieceColor.BLACK.toString());
        pattern.setMoves(new ArrayList<>(moves));
        pattern.setIndexNextMove(0);
        pattern.setApply(true);
        return checkmateRepo.save(pattern);
    }

    public String nextCurrentCheckmateMove() {
        return findActiveCurrentCheckmatePlan()
                .map(this::nextMove)
                .orElse(null);
    }

    public void advanceCurrentCheckmatePlan() {
        findActiveCurrentCheckmatePlan().ifPresent(pattern -> {
            pattern.setIndexNextMove(pattern.getIndexNextMove() + 1);
            checkmateRepo.save(pattern);
        });
    }

    public void abortCurrentCheckmatePlan() {
        checkmateRepo.findFirstByName(CURRENT_CHECKMATE_NAME).ifPresent(pattern -> {
            pattern.setApply(false);
            pattern.setIndexNextMove(0);
            checkmateRepo.save(pattern);
        });
    }

    private String nextMove(CheckmatePattern pattern) {
        if (pattern == null || pattern.getMoves() == null || pattern.getMoves().isEmpty()) {
            return null;
        }
        int index = pattern.getIndexNextMove() == null ? 0 : pattern.getIndexNextMove();
        if (index < 0 || index >= pattern.getMoves().size()) {
            return null;
        }
        return pattern.getMoves().get(index);
    }
    
    public void saveIsApplyCheck(Long idCheck) {
    	CheckmatePattern patterns = checkmateRepo.findById(idCheck).orElse(null);
    	if(patterns != null) {
    		patterns.setApply(false);
    		checkmateRepo.save(patterns);
    	}
    }
    
    public CheckmatePattern getCheckmatePatterById(Long patternId) {
    	
    	CheckmatePattern patterns = checkmateRepo.findById(patternId).orElse(null);
    	return patterns;
    	
    }
    
    public int contCheckmatePatterById() {
    	
    	int cont = (int) checkmateRepo.count();
    	
    	return cont;
    }

}