package com.chess.chessgame.dto;

import java.util.Map;
import com.chess.chessgame.model.Piece;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BoardStateDTO {
    private Map<String, Piece> board;

	public BoardStateDTO(Map<String, Piece> board) {
		super();
		this.board = board;
	}

	public BoardStateDTO() {
		super();
	}

	public Map<String, Piece> getBoard() {
		return board;
	}

	public void setBoard(Map<String, Piece> board) {
		this.board = board;
	}
    
    
}