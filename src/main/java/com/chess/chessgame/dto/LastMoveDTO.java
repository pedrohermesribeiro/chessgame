package com.chess.chessgame.dto;

public class LastMoveDTO {
	private String from;
	private String to;
	private String piece;
	public LastMoveDTO(String from, String to, String piece) {
		super();
		this.from = from;
		this.to = to;
		this.piece = piece;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getPiece() {
		return piece;
	}
	public void setPiece(String piece) {
		this.piece = piece;
	}

	
}
