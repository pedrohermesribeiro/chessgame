package com.chess.chessgame.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class Move {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id = (long) 0;

    @Column(name = "from_pos")
    private String from;

    @Column(name = "to_pos")
    private String to;

    private boolean castling;
    private boolean promotion;

    @ManyToOne
    private Game game; // Relacionamento com a entidade Game

	public Move(String from, String to, boolean castling, boolean promotion) {
		super();
		this.from = from;
		this.to = to;
		this.castling = castling;
		this.promotion = promotion;
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
}