package com.chess.chessgame.dto;

import java.util.List;
import java.util.Map;

import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.GameStatus;

import jakarta.persistence.Lob;
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
    @Lob
    private List<String> boardStateHistory;
    private boolean whiteKingMoved;
    private boolean whiteRookA1Moved;
    private boolean whiteRookH1Moved;
    private boolean blackKingMoved;
    private boolean blackRookA8Moved;
    private boolean blackRookH8Moved;
    
    public GameDTO() {
		super();
	}

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

	public GameDTO(Long id,String playerWhite, String playerBlack, boolean whiteTurn, GameStatus status, String lastMove,
			boolean inCheck, boolean checkmate, Map<String, Piece> board, List<String> boardStateHistory,
			boolean whiteKingMoved, boolean whiteRookA1Moved, boolean whiteRookH1Moved, boolean blackKingMoved,
			boolean blackRookA8Moved, boolean blackRookH8Moved) {
		super();
		this.id = id;
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

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}

	public String getLastMove() {
		return lastMove;
	}

	public void setLastMove(String lastMove) {
		this.lastMove = lastMove;
	}

	public boolean isInCheck() {
		return inCheck;
	}

	public void setInCheck(boolean inCheck) {
		this.inCheck = inCheck;
	}

	public boolean isCheckmate() {
		return checkmate;
	}

	public void setCheckmate(boolean checkmate) {
		this.checkmate = checkmate;
	}

	public Map<String, Piece> getBoard() {
		return board;
	}

	public void setBoard(Map<String, Piece> board) {
		this.board = board;
	}

	public List<String> getBoardStateHistory() {
		return boardStateHistory;
	}

	public void setBoardStateHistory(List<String> boardStateHistory) {
		this.boardStateHistory = boardStateHistory;
	}

	public boolean isWhiteKingMoved() {
		return whiteKingMoved;
	}

	public void setWhiteKingMoved(boolean whiteKingMoved) {
		this.whiteKingMoved = whiteKingMoved;
	}

	public boolean isWhiteRookA1Moved() {
		return whiteRookA1Moved;
	}

	public void setWhiteRookA1Moved(boolean whiteRookA1Moved) {
		this.whiteRookA1Moved = whiteRookA1Moved;
	}

	public boolean isWhiteRookH1Moved() {
		return whiteRookH1Moved;
	}

	public void setWhiteRookH1Moved(boolean whiteRookH1Moved) {
		this.whiteRookH1Moved = whiteRookH1Moved;
	}

	public boolean isBlackKingMoved() {
		return blackKingMoved;
	}

	public void setBlackKingMoved(boolean blackKingMoved) {
		this.blackKingMoved = blackKingMoved;
	}

	public boolean isBlackRookA8Moved() {
		return blackRookA8Moved;
	}

	public void setBlackRookA8Moved(boolean blackRookA8Moved) {
		this.blackRookA8Moved = blackRookA8Moved;
	}

	public boolean isBlackRookH8Moved() {
		return blackRookH8Moved;
	}

	public void setBlackRookH8Moved(boolean blackRookH8Moved) {
		this.blackRookH8Moved = blackRookH8Moved;
	}
    
    
}

