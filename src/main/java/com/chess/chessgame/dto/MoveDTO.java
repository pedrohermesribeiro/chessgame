package com.chess.chessgame.dto;

public class MoveDTO {
    private String notation;

    // Construtor padrão (necessário para desserialização JSON)
    public MoveDTO() {}

    // Construtor com parâmetro
    public MoveDTO(String notation) {
        this.notation = notation;
    }

    // Getter
    public String getNotation() {
        return notation;
    }

    // Setter
    public void setNotation(String notation) {
        this.notation = notation;
    }
}