package com.chess.chessgame.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.chess.chessgame.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
	
    @Query("SELECT g FROM Game g ORDER BY g.id DESC LIMIT 1")
    Game findLastIdGame();
}
