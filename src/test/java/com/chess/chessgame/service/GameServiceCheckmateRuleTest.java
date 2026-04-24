package com.chess.chessgame.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Spy;
import org.springframework.test.util.ReflectionTestUtils;

import com.chess.chessgame.dto.GameDTO;
import com.chess.chessgame.model.CheckmatePattern;
import com.chess.chessgame.model.Game;
import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.model.enums.PieceType;
import com.chess.chessgame.repository.GameRepository;

@ExtendWith(MockitoExtension.class)
class GameServiceCheckmateRuleTest {

    @Spy
    private GameService gameService = new GameService();

    @Mock
    private CheckmateService checkmateService;

    @Mock
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gameService, "checkmateService", checkmateService);
        ReflectionTestUtils.setField(gameService, "gameRepository", gameRepository);
    }

    @Test
    void shouldRejectQueenMoveToH4WhenWhiteKnightIsOnF3() {
        Long gameId = 1L;
        doReturn(gameDtoWithBlockingPieces(piece(PieceType.KNIGHT, PieceColor.WHITE), null)).when(gameService).getGameDTO(gameId);
        when(checkmateService.getCheckmatePatterById(2L)).thenReturn(activePattern());

        boolean result = ReflectionTestUtils.invokeMethod(gameService, "makeDectecdTypeCheckMatePastor", gameId, "d8h4");

        assertFalse(result);
        verify(checkmateService).saveIsApplyCheck(2L);
    }

    @Test
    void shouldRejectQueenMoveToH4WhenWhitePawnIsOnG3() {
        Long gameId = 1L;
        doReturn(gameDtoWithBlockingPieces(null, piece(PieceType.PAWN, PieceColor.WHITE))).when(gameService).getGameDTO(gameId);
        when(checkmateService.getCheckmatePatterById(2L)).thenReturn(activePattern());

        boolean result = ReflectionTestUtils.invokeMethod(gameService, "makeDectecdTypeCheckMatePastor", gameId, "d8h4");

        assertFalse(result);
        verify(checkmateService).saveIsApplyCheck(2L);
    }

    @Test
    void shouldAllowQueenMoveToH4WhenNoBlockingPiecesExist() {
        Long gameId = 1L;
        doReturn(gameDtoWithBlockingPieces(null, null)).when(gameService).getGameDTO(gameId);
        when(checkmateService.getCheckmatePatterById(2L)).thenReturn(activePattern());

        boolean result = ReflectionTestUtils.invokeMethod(gameService, "makeDectecdTypeCheckMatePastor", gameId, "d8h4");

        assertTrue(result);
        verify(checkmateService, never()).saveIsApplyCheck(2L);
    }

    @Test
    void shouldForceQueenRetreatWhenWhiteKnightThreatensH4() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "findForcedQueenRetreatMoveFromH4",
            1L,
            boardWithQueenOnH4(piece(PieceType.KNIGHT, PieceColor.WHITE), null)
        );

        assertNotNull(move);
        assertTrue(move.startsWith("h4"));
    }

    @Test
    void shouldForceQueenRetreatWhenWhitePawnThreatensH4() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "findForcedQueenRetreatMoveFromH4",
            1L,
            boardWithQueenOnH4(null, piece(PieceType.PAWN, PieceColor.WHITE))
        );

        assertNotNull(move);
        assertTrue(move.startsWith("h4"));
    }

    @Test
    void shouldNotForceQueenRetreatWhenH4IsNotThreatened() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "findForcedQueenRetreatMoveFromH4",
            1L,
            boardWithQueenOnH4(null, null)
        );

        assertNull(move);
    }

    @Test
    void shouldRejectBookContinuationWhenQueenOnH4IsThreatened() {
        Long gameId = 1L;
        doReturn(gameDtoWithQueenOnH4(piece(PieceType.KNIGHT, PieceColor.WHITE), null)).when(gameService).getGameDTO(gameId);
        when(checkmateService.getCheckmatePatterById(2L)).thenReturn(activePattern());

        boolean result = ReflectionTestUtils.invokeMethod(gameService, "makeDectecdTypeCheckMatePastor", gameId, "f8c5");

        assertFalse(result);
        verify(checkmateService).saveIsApplyCheck(2L);
    }

    @Test
    void shouldPrioritizeQueenRetreatInReactiveMoveAfterG2G3() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);
        game.setWhiteTurn(false);
        game.setBoardState(gameService.serializeBoardState(linkedBoardWithQueenOnH4(null, piece(PieceType.PAWN, PieceColor.WHITE))));

        Game movedGame = new Game();
        movedGame.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            movedGame.setLastMove(invocation.getArgument(1));
            return movedGame;
        }).when(gameService).makeMove(eq(gameId), anyString());

        Game result = gameService.makeReactiveMove(gameId);

        assertNotNull(result.getLastMove());
        assertTrue(result.getLastMove().startsWith("h4"));
        verify(checkmateService).saveIsApplyCheck(2L);
        verify(checkmateService, never()).applyMoveCheckMate(eq(2L), any(GameDTO.class));
        verify(gameService).makeMove(eq(gameId), argThat(move -> move != null && move.startsWith("h4")));
    }

    @Test
    void shouldDefendQueenInReactiveMoveWhenG4IsAttackedByH3() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);
        game.setWhiteTurn(false);
        game.setBoardState(gameService.serializeBoardState(boardWithQueenOnG4UnderAttack()));

        Game movedGame = new Game();
        movedGame.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            movedGame.setLastMove(invocation.getArgument(1));
            return movedGame;
        }).when(gameService).makeMove(eq(gameId), anyString());

        Game result = gameService.makeReactiveMove(gameId);

        assertNotNull(result.getLastMove());
        assertTrue(result.getLastMove().startsWith("g4h3"));
        verify(gameService).makeMove(eq(gameId), eq("g4h3"));
    }

    @Test
    void shouldChooseSafeQueenCaptureInReactiveMoveWhenQueenIsNotUnderAttack() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);
        game.setWhiteTurn(false);
        game.setBoardState(gameService.serializeBoardState(boardWithSafeQueenCaptureAvailable()));

        Game movedGame = new Game();
        movedGame.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            movedGame.setLastMove(invocation.getArgument(1));
            return movedGame;
        }).when(gameService).makeMove(eq(gameId), anyString());

        Game result = gameService.makeReactiveMove(gameId);

        assertNotNull(result.getLastMove());
        assertTrue(result.getLastMove().startsWith("g4h4"));
        verify(gameService).makeMove(eq(gameId), eq("g4h4"));
    }

    @Test
    void shouldIgnoreQueenCapturePriorityWhenBlackKingIsInCheck() {
        Long gameId = 1L;
        Game game = new Game();
        game.setId(gameId);
        game.setWhiteTurn(false);
        game.setBoardState(gameService.serializeBoardState(boardWithKingInCheckAndQueenCaptureAvailable()));

        Game movedGame = new Game();
        movedGame.setId(gameId);

        when(gameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doAnswer(invocation -> {
            movedGame.setLastMove(invocation.getArgument(1));
            return movedGame;
        }).when(gameService).makeMove(eq(gameId), anyString());

        Game result = gameService.makeReactiveMove(gameId);

        assertNotNull(result.getLastMove());
        assertFalse(result.getLastMove().startsWith("g4h4"));
        verify(gameService, never()).makeMove(eq(gameId), eq("g4h4"));
    }

    @Test
    void shouldFindSafeQueenCaptureEvenWhenQueenIsNotUnderAttack() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "findReactiveQueenSafeCaptureMove",
            boardWithSafeQueenCaptureAvailable(),
            java.util.List.of("g4h4", "e8e7")
        );

        assertTrue("g4h4".equals(move));
    }

    @Test
    void shouldNotTriggerReactiveQueenDefenseWhenQueenIsSafe() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "findReactiveQueenDefenseMove",
            boardWithSafeQueenOnG4(),
            java.util.List.of("g4g5", "e8e7")
        );

        assertNull(move);
    }

    @Test
    void shouldDiscardPseudoLegalReactiveMoveCandidate() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "filterReactiveMoveCandidate",
            "h7h6",
            java.util.List.of("h7h5", "g7g6"),
            "best-defense"
        );

        assertNull(move);
    }

    @Test
    void shouldKeepReactiveMoveCandidateWhenItIsLegal() {
        String move = ReflectionTestUtils.invokeMethod(
            gameService,
            "filterReactiveMoveCandidate",
            "h7h5",
            java.util.List.of("h7h5", "g7g6"),
            "best-defense"
        );

        assertTrue("h7h5".equals(move));
    }

    private GameDTO gameDtoWithBlockingPieces(Piece knightF3, Piece pawnG3) {
        Map<String, Piece> board = new HashMap<>();
        board.put("d8", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("f8", piece(PieceType.BISHOP, PieceColor.BLACK));
        if (knightF3 != null) {
            board.put("f3", knightF3);
        }
        if (pawnG3 != null) {
            board.put("g3", pawnG3);
        }

        GameDTO gameDTO = new GameDTO();
        gameDTO.setBoard(board);
        return gameDTO;
    }

    private GameDTO gameDtoWithQueenOnH4(Piece knightF3, Piece pawnG3) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setBoard(linkedBoardWithQueenOnH4(knightF3, pawnG3));
        return gameDTO;
    }

    private Map<String, Piece> boardWithQueenOnH4(Piece knightF3, Piece pawnG3) {
        return linkedBoardWithQueenOnH4(knightF3, pawnG3);
    }

    private Map<String, Piece> boardWithQueenOnG4UnderAttack() {
        Map<String, Piece> board = new LinkedHashMap<>();
        board.put("g4", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", piece(PieceType.KING, PieceColor.BLACK));
        board.put("e1", piece(PieceType.KING, PieceColor.WHITE));
        board.put("h3", piece(PieceType.PAWN, PieceColor.WHITE));
        return board;
    }

    private Map<String, Piece> boardWithSafeQueenOnG4() {
        Map<String, Piece> board = new LinkedHashMap<>();
        board.put("g4", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", piece(PieceType.KING, PieceColor.BLACK));
        board.put("e1", piece(PieceType.KING, PieceColor.WHITE));
        return board;
    }

    private Map<String, Piece> boardWithSafeQueenCaptureAvailable() {
        Map<String, Piece> board = new LinkedHashMap<>();
        board.put("g4", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", piece(PieceType.KING, PieceColor.BLACK));
        board.put("e1", piece(PieceType.KING, PieceColor.WHITE));
        board.put("h4", piece(PieceType.PAWN, PieceColor.WHITE));
        return board;
    }

    private Map<String, Piece> boardWithKingInCheckAndQueenCaptureAvailable() {
        Map<String, Piece> board = new LinkedHashMap<>();
        board.put("g4", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", piece(PieceType.KING, PieceColor.BLACK));
        board.put("e1", piece(PieceType.KING, PieceColor.WHITE));
        board.put("e2", piece(PieceType.ROOK, PieceColor.WHITE));
        board.put("h4", piece(PieceType.PAWN, PieceColor.WHITE));
        return board;
    }

    private Map<String, Piece> linkedBoardWithQueenOnH4(Piece knightF3, Piece pawnG3) {
        Map<String, Piece> board = new LinkedHashMap<>();
        board.put("h4", piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", piece(PieceType.KING, PieceColor.BLACK));
        board.put("e1", piece(PieceType.KING, PieceColor.WHITE));
        if (knightF3 != null) {
            board.put("f3", knightF3);
        }
        if (pawnG3 != null) {
            board.put("g3", pawnG3);
        }
        return board;
    }

    private CheckmatePattern activePattern() {
        CheckmatePattern pattern = new CheckmatePattern();
        pattern.setApply(true);
        return pattern;
    }

    private Piece piece(PieceType type, PieceColor color) {
        return new Piece(type, color, 1, 1);
    }
}
