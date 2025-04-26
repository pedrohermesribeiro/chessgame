package com.chess.chessgame.dto;

import java.util.List;
import java.util.Map;

import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.GameStatus;

import lombok.Data;

@Data
public class GameDTO {
    private Long id;
    private String playerWhite;
    private String playerBlack;
    private boolean whiteTurn;
    private GameStatus status;
    private String lastMove;
    private boolean inCheck;
    private boolean checkmate;
    private Map<String, Piece> board;
    private List<String> boardStateHistory;
    private boolean whiteKingMoved;
    private boolean whiteRookA1Moved;
    private boolean whiteRookH1Moved;
    private boolean blackKingMoved;
    private boolean blackRookA8Moved;
    private boolean blackRookH8Moved;
    
	public GameDTO(Long long1, String playerWhite, String playerBlack, boolean whiteTurn, GameStatus status, String lastMove,
			boolean inCheck, boolean checkmate, Map<String, Piece> board, List<String> boardStateHistory) {
		super();
		this.playerWhite = playerWhite;
		this.playerBlack = playerBlack;
		this.whiteTurn = whiteTurn;
		this.status = status;
		this.lastMove = lastMove;
		this.inCheck = inCheck;
		this.checkmate = checkmate;
		this.board = board;
		this.boardStateHistory = boardStateHistory;
	}

	public GameDTO(String playerWhite, String playerBlack, boolean whiteTurn, GameStatus status, String lastMove,
			boolean inCheck, boolean checkmate, Map<String, Piece> board, List<String> boardStateHistory,
			boolean whiteKingMoved, boolean whiteRookA1Moved, boolean whiteRookH1Moved, boolean blackKingMoved,
			boolean blackRookA8Moved, boolean blackRookH8Moved) {
		super();
		this.playerWhite = playerWhite;
		this.playerBlack = playerBlack;
		this.whiteTurn = whiteTurn;
		this.status = status;
		this.lastMove = lastMove;
		this.inCheck = inCheck;
		this.checkmate = checkmate;
		this.board = board;
		this.boardStateHistory = boardStateHistory;
		this.whiteKingMoved = whiteKingMoved;
		this.whiteRookA1Moved = whiteRookA1Moved;
		this.whiteRookH1Moved = whiteRookH1Moved;
		this.blackKingMoved = blackKingMoved;
		this.blackRookA8Moved = blackRookA8Moved;
		this.blackRookH8Moved = blackRookH8Moved;
	}

	public boolean isWhiteTurn() {
		return whiteTurn;
	}

	public void setWhiteTurn(boolean whiteTurn) {
		this.whiteTurn = whiteTurn;
	}
    
    
}

