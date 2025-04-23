package com.chess.chessgame.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GameRequestDTO {
    private String playerWhite;
    private String playerBlack;
    private String status;
    private String boardState;
    

	public GameRequestDTO(String playerWhite, String playerBlack, String status, String strGame) {
		super();
		this.playerWhite = playerWhite;
		this.playerBlack = playerBlack;
		this.status = status;
		this.boardState = strGame;
	}


	public String getPlayerWhite() {
		return playerWhite;
	}
	public void setPlayerWhite(String playerWhite) {
		this.playerWhite = playerWhite;
	}
	public String getPlayerBlack() {
		return playerBlack;
	}
	public void setPlayerBlack(String playerBlack) {
		this.playerBlack = playerBlack;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}


	public String getBoardState() {
		return boardState;
	}


	public void setBoardState(String boardState) {
		this.boardState = boardState;
	}

    
	
	
    
}

