package com.chess.chessgame.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
//@NoArgsConstructor
@Entity
public class MovePieceDTO {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;
    
    private String fromPiece;
    
    private String toPiece;
   
    private String mov;
   
   
    private String typePiece;
   
    private String colorPiece;
   
    private Integer valuePiece;
   
    private Integer codigoPiece;
   
    private boolean lastMove;
   
    @ElementCollection
    private List<String> moveAttack = new ArrayList<String>();
    
    @ElementCollection
    private List<String> firstMoves = new ArrayList<String>();
    
	public MovePieceDTO(String fromPiece, String toPiece, String mov, String typePiece, String colorPiece,
			Integer valuePiece, Integer codigoPiece, boolean lastMove) {
		super();
		this.fromPiece = fromPiece;
		this.toPiece = toPiece;
		this.mov = mov;
		this.typePiece = typePiece;
		this.colorPiece = colorPiece;
		this.valuePiece = valuePiece;
		this.codigoPiece = codigoPiece;
		this.lastMove = lastMove;
	}
	
	
	

	public MovePieceDTO(Long id, String fromPiece, String toPiece, String mov, String typePiece, String colorPiece,
			Integer valuePiece, Integer codigoPiece, boolean lastMove, List<String> moveAttack,
			List<String> firstMoves) {
		super();
		this.id = id;
		this.fromPiece = fromPiece;
		this.toPiece = toPiece;
		this.mov = mov;
		this.typePiece = typePiece;
		this.colorPiece = colorPiece;
		this.valuePiece = valuePiece;
		this.codigoPiece = codigoPiece;
		this.lastMove = lastMove;
		this.moveAttack = moveAttack;
		this.firstMoves = firstMoves;
	}



	public MovePieceDTO() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFromPiece() {
		return fromPiece;
	}

	public void setFromPiece(String fromPiece) {
		this.fromPiece = fromPiece;
	}

	public String getToPiece() {
		return toPiece;
	}

	public void setToPiece(String toPiece) {
		this.toPiece = toPiece;
	}

	public String getMov() {
		return mov;
	}

	public void setMov(String mov) {
		this.mov = mov;
	}

	public String getTypePiece() {
		return typePiece;
	}

	public void setTypePiece(String typePiece) {
		this.typePiece = typePiece;
	}

	public String getColorPiece() {
		return colorPiece;
	}

	public void setColorPiece(String colorPiece) {
		this.colorPiece = colorPiece;
	}

	public Integer getValuePiece() {
		return valuePiece;
	}

	public void setValuePiece(Integer valuePiece) {
		this.valuePiece = valuePiece;
	}

	public Integer getCodigoPiece() {
		return codigoPiece;
	}

	public void setCodigoPiece(Integer codigoPiece) {
		this.codigoPiece = codigoPiece;
	}

	public boolean isLastMove() {
		return lastMove;
	}

	public void setLastMove(boolean lastMove) {
		this.lastMove = lastMove;
	}

	public List<String> getMoveAttack() {
		return moveAttack;
	}

	public void setMoveAttack(List<String> moveAttack) {
		this.moveAttack = moveAttack;
	}

	public List<String> getFirstMoves() {
		return firstMoves;
	}

	public void setFirstMoves(List<String> firstMoves) {
		this.firstMoves = firstMoves;
	}


    
    
}
