package com.chess.chessgame.repository;

import com.chess.chessgame.model.Move;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MoveRepository extends JpaRepository<Move, Long> {
}
