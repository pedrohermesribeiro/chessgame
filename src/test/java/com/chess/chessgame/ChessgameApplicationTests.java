package com.chess.chessgame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.chess.chessgame.repository.BadOpeningRepository;
import com.chess.chessgame.repository.MovePieceRepository;

@SpringBootTest
class ChessgameApplicationTests {

	@MockBean
	private BadOpeningRepository badOpeningRepository;
	
	@MockBean
	private MovePieceRepository movePieceRepository;
	
	@Test
	void contextLoads() {
	}

}
