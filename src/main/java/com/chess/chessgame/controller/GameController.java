

package com.chess.chessgame.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chess.chessgame.dto.BoardStateDTO;
import com.chess.chessgame.dto.CastleOptionsDTO;
import com.chess.chessgame.dto.GameDTO;
import com.chess.chessgame.dto.GameRequestDTO;
import com.chess.chessgame.dto.MoveRequest; // Importar a nova classe
import com.chess.chessgame.model.Game;
import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.service.GameService;

@RestController
@RequestMapping("/games")
public class GameController {

    @Autowired
    private GameService gameService;
    
    Map<String, Piece> board = new HashMap<>();
    
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getGameById(@PathVariable Long id) {
        Optional<Game> gameOpt = gameService.getGame(id);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", game.getId());
        response.put("playerWhite", game.getPlayerWhite());
        response.put("playerBlack", game.getPlayerBlack());
        response.put("status", game.getStatus());
        response.put("whiteTurn", game.isWhiteTurn());
        response.put("inCheck", game.isInCheck());
        response.put("checkmate", game.isCheckmate());
        response.put("lastMove", game.getLastMove());
        response.put("board", gameService.deserializeBoardState(game.getBoardState()));
        response.put("whiteKingMoved", game.isWhiteKingMoved());
        response.put("whiteRookA1Moved", game.isWhiteRookA1Moved());
        response.put("whiteRookH1Moved", game.isWhiteRookH1Moved());
        response.put("blackKingMoved", game.isBlackKingMoved());
        response.put("blackRookA8Moved", game.isBlackRookA8Moved());
        response.put("blackRookH8Moved", game.isBlackRookH8Moved());

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public Game createGame(@RequestParam String playerWhite, @RequestParam String playerBlack) {
    	Game game = gameService.createGame(playerWhite, playerBlack);
    	GameRequestDTO gm = new GameRequestDTO(game.getPlayerWhite(),game.getPlayerBlack(),game.getStatus().name(),
    			game.convertToDatabaseColumn(board));
    	
 
        return game;
     }
    
    @PostMapping("/{id}/reset")
    public Game resetGame(@PathVariable Long id) {
        return gameService.resetGame(id);
    }
    
    @GetMapping("/games/{id}")
    public ResponseEntity<Map<String, Object>> getGameState(@PathVariable Long id) {
        Optional<Game> gameOpt = gameService.getGame(id);
        if (gameOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Game game = gameOpt.get();
        Map<String, Object> response = new HashMap<>();
        response.put("id", game.getId());
        response.put("playerWhite", game.getPlayerWhite());
        response.put("playerBlack", game.getPlayerBlack());
        response.put("status", game.getStatus());
        response.put("whiteTurn", game.isWhiteTurn());
        response.put("inCheck", game.isInCheck());
        response.put("checkmate", game.isCheckmate());
        response.put("lastMove", game.getLastMove());
        response.put("board", gameService.deserializeBoardState(game.getBoardState()));
        response.put("whiteKingMoved", game.isWhiteKingMoved());
        response.put("whiteRookA1Moved", game.isWhiteRookA1Moved());
        response.put("whiteRookH1Moved", game.isWhiteRookH1Moved());
        response.put("blackKingMoved", game.isBlackKingMoved());
        response.put("blackRookA8Moved", game.isBlackRookA8Moved());
        response.put("blackRookH8Moved", game.isBlackRookH8Moved());

        return ResponseEntity.ok(response);
    }


    
    @GetMapping("/{id}/castle-options")
    public ResponseEntity<CastleOptionsDTO> getCastleOptions(@PathVariable Long id) {
        Game optionalGame = gameService.getGame(id).get();
        Game game = optionalGame;
        Map<String, Piece> board = gameService.deserializeBoardState(game.getBoardState());
        PieceColor color = game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;

        boolean kingside = gameService.canCastleKingside(board, game, color);
        boolean queenside = gameService.canCastleQueenside(board, game, color);

        return ResponseEntity.ok(new CastleOptionsDTO(kingside, queenside));
    }

    
    @PostMapping("/{id}/move")
    public Game makeMove(@PathVariable Long id, @RequestBody MoveRequest moveRequest) {
        return gameService.makeMove(id, moveRequest.getMoveNotation());
    }
    
    @PostMapping("/{id}/computer-move")
    public ResponseEntity<Game> makeComputerMove(@PathVariable Long id) {
        try {
            Game updatedGame = gameService.makeComputerMove(id);
            return ResponseEntity.ok(updatedGame);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/{id}/smart-computer-move")
    public ResponseEntity<Game> makeSmartComputerMove(@PathVariable Long id) {
        try {
            Game updatedGame = gameService.makeCaptureBasedMove(id); // novo método do serviço
            return ResponseEntity.ok(updatedGame);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    @PostMapping("/{id}/hard-computer-move")
    public ResponseEntity<GameDTO> makeHardMove(@PathVariable Long id) {
        Game game = gameService.makeHardAIMove(id);
        Map<String, Piece> board = gameService.deserializeBoardState(game.getBoardState());
        return ResponseEntity.ok(new GameDTO(game.getId(), game.getPlayerWhite(),game.getPlayerBlack(),
        		game.isWhiteTurn(),game.getStatus(),game.getLastMove(),game.isInCheck(),
        		game.isCheckmate(),board,game.getBoardStateHistory(),game.isWhiteKingMoved(),
        		game.isWhiteRookA1Moved(),game.isWhiteRookH1Moved(),game.isBlackKingMoved(),
        		game.isBlackRookA8Moved(),game.isBlackRookH8Moved()));
    }
    
    @PostMapping("/{gameId}/capture-move")
    public GameDTO makeCaptureMove(@PathVariable Long gameId) {
        return gameService.getGameDTO(gameId);
    }

    @PostMapping("/{id}/defensive-move")
    public ResponseEntity<GameDTO> makeDefensiveMove(@PathVariable Long id) {
        System.out.println("Iniciando makeDefensiveMove para gameId: " + id);
        Game game = gameService.makeDefensiveMove(id);
        GameDTO gameDTO = new GameDTO(game.getId(), game.getPlayerWhite(),game.getPlayerBlack(),
        		game.isWhiteTurn(),game.getStatus(),game.getLastMove(),game.isInCheck(),
        		game.isCheckmate(),board,game.getBoardStateHistory(),game.isWhiteKingMoved(),
        		game.isWhiteRookA1Moved(),game.isWhiteRookH1Moved(),game.isBlackKingMoved(),
        		game.isBlackRookA8Moved(),game.isBlackRookH8Moved());
        System.out.println("GameDTO criado: id=" + gameDTO.getId() + ", lastMove=" + gameDTO.getLastMove());
        return ResponseEntity.ok(gameDTO);
    }

    @PostMapping("/{gameId}/reactive-move")
    public GameDTO makeReactiveMove(@PathVariable Long gameId) {
    	
    	Game game = gameService.makeReactiveMove(gameId);
        Map<String, Piece> board = gameService.desearilizeState(game.getBoardState());
        GameDTO dto = new GameDTO();
        
           dto.setId(game.getId());
           dto.setPlayerWhite(game.getPlayerWhite());
           dto.setPlayerBlack(game.getPlayerBlack());
           dto.setWhiteTurn(game.isWhiteTurn());
           dto.setStatus(game.getStatus());
           dto.setLastMove(game.getLastMove());
           dto.setInCheck(game.isInCheck());
           dto.setCheckmate(game.isCheckmate());
           dto.setBoard(board);
           dto.setBoardStateHistory(game.getBoardStateHistory());
           dto.setWhiteKingMoved(game.isWhiteKingMoved());
           dto.setWhiteRookA1Moved(game.isWhiteRookA1Moved());
           dto. setWhiteRookH1Moved(game.isWhiteRookH1Moved());
           dto.setBlackKingMoved(game.isBlackKingMoved());
           dto.setBlackRookA8Moved(game.isBlackRookA8Moved());
           dto.setBlackRookH8Moved(game.isBlackRookH8Moved());
           
           return dto;
    }


    
    @GetMapping("/{id}/board")
    public ResponseEntity<BoardStateDTO> getBoard(@PathVariable Long id) {
    	Game game = gameService.getGame(id).get();
      	Map<String, Piece> mapPiece = gameService.desearilizeState(game.getBoardState());
        BoardStateDTO board = new BoardStateDTO(mapPiece);
        return ResponseEntity.ok(board);
    }

    @GetMapping
    public ResponseEntity<Game> createGameViaGet(
            @RequestParam String white,
            @RequestParam String black) {
        Game game = gameService.createGame(white, black);
        return ResponseEntity.ok(game);
    }
}