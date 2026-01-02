package com.chess.chessgame.model;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_checkmate_pattern")
public class CheckmatePattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nome do padrão
    private String name;

    // Sequência de jogadas (formato PGN simplificado ou UCI)
    @ElementCollection
    @CollectionTable(name = "tb_checkmate_moves", joinColumns = @JoinColumn(name = "pattern_id"))
    @Column(name = "move")
    private List<String> moves; // ex.: ["e2e4", "f7f6", "d1h5", "g7g6", "h5g6#"]

    // Cor que aplica o mate
    private String winningColor; // "WHITE" ou "BLACK"
    
    private Integer indexNextMove = 0;
    
    private boolean isApply = true;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getMoves() {
		return moves;
	}

	public void setMoves(List<String> moves) {
		this.moves = moves;
	}

	public String getWinningColor() {
		return winningColor;
	}

	public void setWinningColor(String winningColor) {
		this.winningColor = winningColor;
	}

	public Integer getIndexLastMove() {
		return indexNextMove;
	}

	public void setIndexLastMove(Integer indexLastMove) {
		this.indexNextMove = indexLastMove;
	}

	public Integer getIndexNextMove() {
		return indexNextMove;
	}

	public void setIndexNextMove(Integer indexNextMove) {
		this.indexNextMove = indexNextMove;
	}

	public boolean isApply() {
		return isApply;
	}

	public void setApply(boolean isApply) {
		this.isApply = isApply;
	}
    
    
}
