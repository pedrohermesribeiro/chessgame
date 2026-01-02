package com.chess.chessgame.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

@Entity
@Data
public class BadOpenig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private List<String> moveSequence = new ArrayList<String>(); // Sequência de movimentos (ex.: ["e2e4", "e7e5", "f1c4", ...])

    private int mateInMoves; // Número de movimentos até o mate
    
    private Long gameId;

    // Construtor
    public BadOpenig(List<String> moveSequence, int mateInMoves) {
        this.moveSequence = moveSequence;
        this.mateInMoves = mateInMoves;
        this.gameId = gameId;
    }
    
   	public BadOpenig() {
   		
	}



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<String> getMoveSequence() {
		return moveSequence;
	}

	public void setMoveSequence(String moveSequence) {
		this.moveSequence.add(moveSequence);
	}

	public int getMateInMoves() {
		return mateInMoves;
	}

	public void setMateInMoves(int mateInMoves) {
		this.mateInMoves = mateInMoves;
	}

	public Long getGameId() {
		return gameId;
	}

	public void setGameId(Long gameId) {
		this.gameId = gameId;
	}
    

}
