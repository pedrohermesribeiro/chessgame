package com.chess.chessgame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.chessgame.model.Game;

public interface GameRepository extends JpaRepository<Game, Long> {
}
