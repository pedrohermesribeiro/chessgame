package com.chess.chessgame.model;

import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.model.enums.PieceType;

public class Piece {

    private PieceType type;
    private PieceColor color;
    private Integer valuePiece;

    public Piece() {
        // Construtor padrão necessário para deserialização do Jackson
    }

    public Piece(PieceType type, PieceColor color, Integer valuePiece) {
        this.type = type;
        this.color = color;
        this.valuePiece = valuePiece;
    }

    public PieceType getType() {
        return type;
    }

    public void setType(PieceType type) {
        this.type = type;
    }

    public PieceColor getColor() {
        return color;
    }

    public void setColor(PieceColor color) {
        this.color = color;
    }

	public Integer getValuePiece() {
		return valuePiece;
	}

	public void setValuePiece(Integer valuePiece) {
		this.valuePiece = valuePiece;
	}
    
    
}
