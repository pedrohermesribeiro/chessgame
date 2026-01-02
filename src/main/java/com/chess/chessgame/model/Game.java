	package com.chess.chessgame.model;

	import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chess.chessgame.model.enums.GameStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import lombok.Data;

	@Data
	@Entity
	public class Game {
	    @Id
	    @GeneratedValue(strategy = GenerationType.AUTO)
	    private Long id = (long) 1;

	    private String playerWhite;
	    private String playerBlack;
	    @Enumerated(EnumType.STRING)
	    private GameStatus status;
	    private boolean whiteTurn;
	    
	    @Lob
	    @Column(name="boardState", length = 1300)
	    private String boardState;
	    @Lob
	    private String boardTest;
	    @Lob
	    private List<String> boardStateHistory = new ArrayList<>();
	    @Lob
	    private String lastMove;

	    private boolean whiteKingMoved;
	    private boolean whiteRookA1Moved;
	    private boolean whiteRookH1Moved;
	    private boolean blackKingMoved;
	    private boolean blackRookA8Moved;
	    private boolean blackRookH8Moved;
	    private boolean inCheck;
	    private boolean checkmate;

	    @OneToMany(mappedBy = "game") // O campo 'game' deve existir em Move
	    private List<Move> moves = new ArrayList<>();

	    

		public Game() {
			super();
		}

		public Game(String playerWhite, String playerBlack, GameStatus status, boolean whiteTurn, String boardState,
				List<String> boardStateHistory) {
			super();
			this.id ++;
			this.playerWhite = playerWhite;
			this.playerBlack = playerBlack;
			this.status = status;
			this.whiteTurn = whiteTurn;
			this.boardState = boardState;
			this.boardStateHistory = boardStateHistory;
			System.out.println(this.boardState);
		}
		
		public Game(String playerWhite, String playerBlack, GameStatus status, boolean whiteTurn, String boardState,
				String lastMove, List<String> boardStateHistory) {
			super();
			this.id ++;
			this.playerWhite = playerWhite;
			this.playerBlack = playerBlack;
			this.status = status;
			this.whiteTurn = whiteTurn;
			this.boardState = boardState;
			this.lastMove = lastMove;
			this.boardStateHistory = boardStateHistory;
			
			System.out.println(this.boardState);
		}
		
		public Game(String playerWhite, String playerBlack, GameStatus status, boolean whiteTurn, String boardState,
				String boardTest, List<String> boardStateHistory, String lastMove, boolean whiteKingMoved,
				boolean whiteRookA1Moved, boolean whiteRookH1Moved, boolean blackKingMoved, boolean blackRookA8Moved,
				boolean blackRookH8Moved, boolean inCheck, boolean checkmate, List<Move> moves) {
			super();
			this.playerWhite = playerWhite;
			this.playerBlack = playerBlack;
			this.status = status;
			this.whiteTurn = whiteTurn;
			this.boardState = boardState;
			this.boardTest = boardTest;
			this.boardStateHistory = boardStateHistory;
			this.lastMove = lastMove;
			this.whiteKingMoved = whiteKingMoved;
			this.whiteRookA1Moved = whiteRookA1Moved;
			this.whiteRookH1Moved = whiteRookH1Moved;
			this.blackKingMoved = blackKingMoved;
			this.blackRookA8Moved = blackRookA8Moved;
			this.blackRookH8Moved = blackRookH8Moved;
			this.inCheck = inCheck;
			this.checkmate = checkmate;
			this.moves = moves;
		}
		
		

		public Game(Game game) {
			//this.id++;
			// TODO Auto-generated constructor stub
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

		public boolean isWhiteTurn() {
			return whiteTurn;
		}

		public void setWhiteTurn(boolean whiteTurn) {
			this.whiteTurn = whiteTurn;
		}

		public String getBoardState() {
			
			//Object obj = this.boardState;
			return this.boardState;
		}

		public void setBoardState(String boardState) {
			this.boardState = boardState;
		}

		public List<String> getBoardStateHistory() {
			
			return boardStateHistory;
		}

		public void setBoardStateHistory(List<String> boardStateHistory) {
			this.boardStateHistory = boardStateHistory;
		}

		public String getBoardTest() {
			return boardTest;
		}

		public void setBoardTest(Map<String,Piece> boardTest) {
			this.boardTest = this.convertToDatabaseColumn(boardTest);
		}
	    
		
		@Transient
		private final ObjectMapper objectMapper = new ObjectMapper();
  

	    
	    public String convertToDatabaseColumn(Map<String, Piece> map) {  
	    	try {  
	            return objectMapper.writeValueAsString(map);  
	        } catch (JsonProcessingException e) {  
	            throw new IllegalArgumentException("Error converting map to JSON", e);  
	        }  
	    }
	    
	    public void setLastMove(String lastMove) {
	        this.lastMove = lastMove;
	    }

	    public String getLastMove() {
	        return lastMove;
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

		public List<Move> getMoves() {
			return moves;
		}

		public void setMoves(List<Move> moves) {
			this.moves = moves;
		}


	    
	    
	}
