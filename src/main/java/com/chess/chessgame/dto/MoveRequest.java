package com.chess.chessgame.dto;

import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveRequest {
	@Lob
    private String moveNotation;

	public String getMoveNotation() {
		return moveNotation;
	}

	public void setMoveNotation(String moveNotation) {
		this.moveNotation = moveNotation;
	}
    
    
}