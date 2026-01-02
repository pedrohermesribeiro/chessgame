package com.chess.chessgame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chess.chessgame.model.Move;

public interface MoveRepository extends JpaRepository<Move, Long> {
	
    @Query("SELECT m FROM Move m WHERE m.numId = :id")
    List<Move> findMoveByIdGame(@Param("id") Long id);
	
}
