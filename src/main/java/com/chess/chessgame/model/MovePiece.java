package com.chess.chessgame.model;
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
public class MovePiece {
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
   
    private Integer contMove = 0;
public MovePiece(String fromPiece, String toPiece, String mov, String typePiece, String colorPiece,
	Integer valuePiece, Integer codigoPiece, boolean lastMove, Integer contMove) {
	super();
	this.fromPiece = fromPiece;
	this.toPiece = toPiece;
	this.mov = mov;
	this.typePiece = typePiece;
	this.colorPiece = colorPiece;
	this.valuePiece = valuePiece;
	this.codigoPiece = codigoPiece;
	this.lastMove = lastMove;
	this.contMove = contMove;
	}

	public MovePiece() {
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
	
	public boolean getLastMove() {
		return lastMove;
	}
	
	public void setLastMove(boolean lastMove) {
		this.lastMove = lastMove;
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
	
	public List<String> getMoveAttack() {
		return moveAttack;
	}
	
	public void setMoveAttack(List<String> moveAttack) {
		this.moveAttack = moveAttack;
	}
	
	public Integer getContMove() {
		return contMove;
	}
	
	public void setContMove(Integer contMove) {
		this.contMove = contMove;
	}

	public List<String> getFirstMoves() {
		return firstMoves;
	}

	public void setFirstMoves(List<String> firstMoves) {
		this.firstMoves = firstMoves;
	}
  
	
   
}