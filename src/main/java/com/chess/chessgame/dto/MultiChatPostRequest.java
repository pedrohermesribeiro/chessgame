package com.chess.chessgame.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiChatPostRequest {
    private String color;
    private String name;
    private String text;
}
