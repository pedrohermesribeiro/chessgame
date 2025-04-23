package com.chess.chessgame.model;

import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.model.enums.PieceType;

public class Piece {

    private PieceType type;
    private PieceColor color;

    public Piece() {
        // Construtor padrão necessário para deserialização do Jackson
    }

    public Piece(PieceType type, PieceColor color) {
        this.type = type;
        this.color = color;
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
}
