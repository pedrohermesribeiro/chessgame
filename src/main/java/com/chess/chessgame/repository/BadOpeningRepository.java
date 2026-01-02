package com.chess.chessgame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chess.chessgame.model.BadOpenig;

public interface BadOpeningRepository extends JpaRepository<BadOpenig, Long> {
	
    //@Query("SELECT b FROM BadOpenig b WHERE b.gameId = :gameId")
    //List<BadOpenig> findBadOpenigByIdGame(@Param("gameId") Long gameId);
}
