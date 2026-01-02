package com.chess.chessgame.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.chessgame.model.MovePiece;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.repository.MovePieceRepository;

@Service
public class MovePieceService {

    @Autowired
    private MovePieceRepository movePieceRepository;
    
    public List<MovePiece> getListMovePieceTrue(){
    	
     List<MovePiece> listMovePiece = movePieceRepository.findMovePieceByPieceColorLastMoveTrue(true,PieceColor.BLACK.toString());
    	
     return listMovePiece;
     
    }
	
}
