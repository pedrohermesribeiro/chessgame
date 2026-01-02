package com.chess.chessgame.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.chessgame.model.CheckmatePattern;

public interface CheckmatePatternRepository extends JpaRepository<CheckmatePattern, Long> {
    List<CheckmatePattern> findByWinningColor(String winningColor);
}

