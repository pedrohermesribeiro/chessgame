package com.chess.chessgame.dto;

public class CastleOptionsDTO {
    private boolean kingside;
    private boolean queenside;

    public CastleOptionsDTO(boolean kingside, boolean queenside) {
        this.kingside = kingside;
        this.queenside = queenside;
    }

    public boolean isKingside() {
        return kingside;
    }

    public boolean isQueenside() {
        return queenside;
    }
}
