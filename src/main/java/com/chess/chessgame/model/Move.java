package com.chess.chessgame.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
@Entity
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id = (long) 1;

    @Column(name = "from_pos")
    private String from;

    @Column(name = "to_pos")
    private String to;

    private boolean castling;
    private boolean promotion;

    @Lob
    @ManyToOne
    private Game game; // Relacionamento com a entidade Game
    
    private Long numId;
    
    

	public Move(String from, String to, boolean castling, boolean promotion,Long numId) {
		super();
		//this.id++;
		this.from = from;
		this.to = to;
		this.castling = castling;
		this.promotion = promotion;
		this.numId = numId;
	}
	
	public Move(String from, String to, boolean castling, boolean promotion,Game game,Long numId) {
		super();
		//this.id++;
		this.from = from;
		this.to = to;
		this.castling = castling;
		this.promotion = promotion;
		this.game = game;
		this.numId = numId;
	}
	
	
	
	
	
    public Move(String from, String to, boolean castling, boolean promotion) {
		super();

		this.from = from;
		this.to = to;
		this.castling = castling;
		this.promotion = promotion;
	}





	public Move(String from, String to) {
    	//this.id++;
		this.from = from;
		this.to = to;

	}
    
    



	public Move(String from, String to, Game game, Long numId) {

		this.id++;
		this.from = from;
		this.to = to;
		this.game = game;
		this.numId = numId;
	}

	


	public Move() {
		super();
	}

	public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
    
    public boolean isCastling() {
        return castling;
    }

    public boolean isPromotion() {
        return promotion;
    }



	public Game getGame() {
		return game;
	}



	public void setGame(Game game) {
		this.game = game;
	}



	public Long getNumId() {
		return numId;
	}



	public void setNumId(Long gameId) {
		this.numId = gameId;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setCastling(boolean castling) {
		this.castling = castling;
	}

	public void setPromotion(boolean promotion) {
		this.promotion = promotion;
	}
    
    
}