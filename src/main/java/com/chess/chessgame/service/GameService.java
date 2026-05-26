package com.chess.chessgame.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.chessgame.dto.GameDTO;
import com.chess.chessgame.model.BadOpenig;
import com.chess.chessgame.model.CheckmatePattern;
import com.chess.chessgame.model.Game;
import com.chess.chessgame.model.Move;
import com.chess.chessgame.model.MovePiece;
import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.GameStatus;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.model.enums.PieceType;
import com.chess.chessgame.repository.BadOpeningRepository;
import com.chess.chessgame.repository.GameRepository;
import com.chess.chessgame.repository.MovePieceRepository;
import com.chess.chessgame.repository.MoveRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private MoveRepository moveRepository;
    
    @Autowired
    private BadOpeningRepository badOpeningRepository;
    
    @Autowired
    private MovePieceRepository movePieceRepository;
    
    @Autowired
    private CheckmateService checkmateService;
    
    @Autowired
    private AIService aiService;

    
    @Autowired
    private StockfishService stockfishService;

    private final Map<String, List<String>> openingBook = new HashMap<>();

    private final Map<String, Integer> transpositionTable = new HashMap<>();
    
    BadOpenig badOpening = new BadOpenig();

    private static final ObjectMapper MULTI_CHAT_MAPPER = new ObjectMapper();
    private static final int MULTI_CHAT_MAX_MESSAGES = 100;
    private static final int MULTI_CHAT_MAX_TEXT_LEN = 280;
    private static final int MULTI_CHAT_MAX_NAME_LEN = 40;
    
    
    @PostConstruct
    public void init() {
        initializeOpeningBook();
        preloadBadOpenings();
        
    }

    private void initializeOpeningBook() {
        openingBook.put("e2e4", Arrays.asList("e7e5", "c7c5", "e7e6", "c7c6")); // Open Game, Sicilian, French, Caro-Kann
        openingBook.put("d2d4", Arrays.asList("d7d5", "g8f6", "e7e6", "c7c5")); // Queen's Gambit, Indian, French, Dutch
        openingBook.put("c2c4", Arrays.asList("e7e5", "c7c5", "g8f6")); // English responses
        // Adicione mais
    }
   
    private void initializeMovePiece(int y) {
     String from = null;
     Piece piece = new Piece();
     PieceColor color = null;
     
     if(y == 0) {
    	 color = PieceColor.WHITE;
     }else if(y == 1) {
    	 color = PieceColor.BLACK;
     }
    
     List<String> piecesTo = new ArrayList<String>();
     for(int i = 1; i < 17; i++) {
     
            switch (i) {
            
            case 1 :
             piece = new Piece(PieceType.ROOK, color ,5,1);
             if(y == 0) {
            	 from = "a1";
            	 piecesTo = Arrays.asList("a2","b1");
             }else if(y == 1) {
            	 from = "a8";
            	 piecesTo = Arrays.asList("a7","b8");
             }
             break;
            case 2:
             piece = new Piece(PieceType.KNIGHT, color,3,1);
             if(y == 0) {
            	 from = "b1";
            	 piecesTo = Arrays.asList("a3","c3");
             }else if(y == 1) {
            	 from = "b8";
            	 piecesTo = Arrays.asList("a6","c6");
             }
             break;
            case 3 :
             piece = new Piece(PieceType.BISHOP, color,3,1);
             if(y == 0) {
            	 from = "c1";
            	 piecesTo = Arrays.asList("b2","d2");
             }else if(y == 1) {
            	 from = "c8";
            	 piecesTo = Arrays.asList("b7","d7");
             }
             break;
            case 4:
             piece = new Piece(PieceType.QUEEN, color,9,0);
             if(y == 0) {
            	 from = "d1";
             	piecesTo = Arrays.asList("c1","c2","d2","e2","e1");
             }else if(y == 1) {
            	 from = "d8";
             	piecesTo = Arrays.asList("c8","c7","d7","e7","e8");
             }
             break;
            case 5 :
             piece = new Piece(PieceType.KING, color,1000,0);
             if(y == 0) {
            	 from = "e1";
            	 piecesTo = Arrays.asList("d1","d2","e2","f2","f1");
             }else if(y == 1) {
            	 from = "e8";
            	 piecesTo = Arrays.asList("d8","d7","e7","f7","f8");
             }
             break;
            case 6:
             piece = new Piece(PieceType.BISHOP, color,3,2);
             if(y == 0) {
            	 from = "f1";
            	 piecesTo = Arrays.asList("e2","g2");
             }else if(y == 1) {
            	 from = "f8";
            	 piecesTo = Arrays.asList("e7","g7");
             }
             break;
            case 7:
             piece = new Piece(PieceType.KNIGHT, color,3,2);
             if(y == 0) {
            	 from = "g1";
            	 piecesTo = Arrays.asList("f3","h3");
             }else if(y == 1) {
            	 from = "g8";
            	 piecesTo = Arrays.asList("f6","h6");
             }
             break;
            case 8:
              piece = new Piece(PieceType.ROOK, color,5,2) ;
             if(y == 0) {
            	 from = "h1";
            	 piecesTo = Arrays.asList("h2","g1");
             }else if(y == 1) {
            	 from = "h8";
            	 piecesTo = Arrays.asList("h7","g8");
             }
              break;
            
            case 9 :
             piece = new Piece(PieceType.PAWN, color,1,1);
             if(y == 0) {
            	 from = "a2";
             }else if(y == 1) {
            	 from = "a7";
             }
             piecesTo = null;
             break;
            case 10:
             piece = new Piece(PieceType.PAWN, color,1,2);
             if(y == 0) {
            	 from = "b2";
             }else if(y == 1) {
            	 from = "b7";
             }
             piecesTo = null;
             break;
            case 11 :
             piece = new Piece(PieceType.PAWN, color,1,3);
             if(y == 0) {
            	 from = "c2";
             }else if(y == 1) {
            	 from = "c7";
             }
             piecesTo = null;
             break;
            case 12 :
             piece = new Piece(PieceType.PAWN, color,1,4);
             if(y == 0) {
            	 from = "d2";
             }else if(y == 1) {
            	 from = "d7";
             }
             piecesTo = null;
             break;
            case 13 :
             piece = new Piece(PieceType.PAWN, color,1,5);
             if(y == 0) {
            	 from = "e2";
             }else if(y == 1) {
            	 from = "e7";
             }
             piecesTo = null;
             break;
            case 14 :
             piece = new Piece(PieceType.PAWN, color,1,6);
             if(y == 0) {
            	 from = "f2";
             }else if(y == 1) {
            	 from = "f7";
             }
             piecesTo = null;
             break;
            case 15 :
             piece = new Piece(PieceType.PAWN, color,1,7);
             if(y == 0) {
            	 from = "g2";
             }else if(y == 1) {
            	 from = "g7";
             }
             piecesTo = null;
             break;
            case 16 :
              piece = new Piece(PieceType.PAWN, color,1,8) ;
             if(y == 0) {
            	 from = "h2";
             }else if(y == 1) {
            	 from = "h7";
             }
              piecesTo = null;
              break;
            }
           
        MovePiece movePiece = new MovePiece();
        movePiece.setFromPiece(from);
        movePiece.setToPiece(null);
        movePiece.setMov(null);
        movePiece.setTypePiece(piece.getType().toString());
        movePiece.setColorPiece(piece.getColor().toString());
        movePiece.setValuePiece(piece.getValuePiece());
        movePiece.setCodigoPiece(piece.getCodigo());
        movePiece.setContMove(0);
        movePiece.setLastMove(true);
        movePiece.setMoveAttack(piecesTo);
        movePiece.setFirstMoves(piecesTo);
        movePieceRepository.save(movePiece);
            
           
     }
    }
   
    public void preloadBadOpenings() {
        // Fool's Mate variation
        //badOpeningRepository.save(new BadOpenig(Arrays.asList("e2e4", "h7h6", "d1f3", "f7f6", "f3h5", "g7g6", "h5g6"), 7,null));
        // Scholar's Mate
        //badOpeningRepository.save(new BadOpenig(Arrays.asList("e2e4", "e7e5", "d1h5", "b8c6", "f1c4", "g8f6", "h5f7"), 7,null));
        // Adicione mais
    }
   
    public List<BadOpenig> findBadOpenigByIdGame(Long gameId) {
     List<BadOpenig> badOpenig = new ArrayList<BadOpenig>();
     //badOpenig = badOpeningRepository.findBadOpenigByIdGame(gameId);
     return badOpenig;
    }
   
    public List<Move> findMoveByIdGame(Long gameId) {
     Optional<Game> game = gameRepository.findById(gameId);
     //game = gameRepository.findById(gameId);
     List<Move> moves = new ArrayList<Move>();
     moves = moveRepository.findMoveByIdGame(game.get().getId());
     return moves;
    }
    
    @Transactional
    public Game createGame(String playerWhite, String playerBlack) {
        movePieceRepository.deleteAll();

        Game game = new Game();
        game.setId(null);
        game.setPlayerWhite(playerWhite);
        game.setPlayerBlack(playerBlack);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setWhiteTurn(true);
        game.setCheckmate(false);
        game.setInCheck(false);
        game.setLastMove(null);
        game.setBlackKingMoved(false);
        game.setBlackRookA8Moved(false);
        game.setBlackRookH8Moved(false);
        game.setWhiteKingMoved(false);
        game.setWhiteRookA1Moved(false);
        game.setWhiteRookH1Moved(false);
        game.setMoves(new ArrayList<>());
        game.setBoardStateHistory(new ArrayList<>()); // <— evitar NPE
        clearMultiLobbyState(game);
        Map<String, Piece> board = initializeBoard();
        game.setBoardState(serializeBoardState(board));

        gameRepository.save(game);

        this.resetCheckMateRepository();
        
        for (int i = 0; i < 2; i++) initializeMovePiece(i);

        return game;
    }

    @Transactional
    public Game requestMultiJoin(Long gameId, String requesterName, String requesterColor) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        game.setMultiJoinRequested(true);
        game.setMultiJoinRequesterName(requesterName);
        game.setMultiJoinRequesterColor(requesterColor);
        game.setMultiJoinApproved(false);
        game.setMultiJoinRejected(false);
        return gameRepository.save(game);
    }

    @Transactional
    public Game approveMultiJoin(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        game.setMultiJoinRequested(false);
        game.setMultiJoinApproved(true);
        game.setMultiJoinRejected(false);
        return gameRepository.save(game);
    }

    @Transactional
    public Game rejectMultiJoin(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        game.setMultiJoinRequested(false);
        game.setMultiJoinApproved(false);
        game.setMultiJoinRejected(true);
        return gameRepository.save(game);
    }

    @Transactional
    public Game closeMultiGame(Long gameId, String reason) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        game.setMultiGameClosed(true);
        game.setMultiCloseReason(reason);
        return gameRepository.save(game);
    }

    private void clearMultiLobbyState(Game game) {
        game.setMultiJoinRequested(false);
        game.setMultiJoinRequesterName(null);
        game.setMultiJoinRequesterColor(null);
        game.setMultiJoinApproved(false);
        game.setMultiJoinRejected(false);
        game.setMultiGameClosed(false);
        game.setMultiCloseReason(null);
        game.setMultiChatJson(null);
        game.setMultiChatRev(0);
    }

    public Map<String, Object> getMultiChat(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        Map<String, Object> out = new HashMap<>();
        out.put("messages", parseMultiChatMessages(game.getMultiChatJson()));
        out.put("rev", game.getMultiChatRev());
        return out;
    }

    @Transactional
    public Map<String, Object> postMultiChatMessage(Long gameId, String color, String name, String text) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));
        if (!game.isMultiJoinApproved() || game.isMultiGameClosed()) {
            throw new IllegalStateException("Chat indisponível para este jogo.");
        }
        if (color == null || (!"WHITE".equalsIgnoreCase(color) && !"BLACK".equalsIgnoreCase(color))) {
            throw new IllegalArgumentException("Cor deve ser WHITE ou BLACK.");
        }
        String normalizedColor = "WHITE".equalsIgnoreCase(color) ? "WHITE" : "BLACK";
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("Mensagem vazia.");
        }
        String n = name == null ? "" : name.trim();
        if (n.length() > MULTI_CHAT_MAX_NAME_LEN) {
            n = n.substring(0, MULTI_CHAT_MAX_NAME_LEN);
        }
        if (t.length() > MULTI_CHAT_MAX_TEXT_LEN) {
            t = t.substring(0, MULTI_CHAT_MAX_TEXT_LEN);
        }
        if (n.isEmpty()) {
            n = "WHITE".equals(normalizedColor)
                    ? (game.getPlayerWhite() != null ? game.getPlayerWhite() : "Brancas")
                    : (game.getPlayerBlack() != null ? game.getPlayerBlack() : "Pretas");
        }

        List<Map<String, Object>> list = parseMultiChatMessages(game.getMultiChatJson());
        while (list.size() >= MULTI_CHAT_MAX_MESSAGES) {
            list.remove(0);
        }
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("ts", System.currentTimeMillis());
        msg.put("color", normalizedColor);
        msg.put("name", n);
        msg.put("text", t);
        list.add(msg);
        try {
            game.setMultiChatJson(MULTI_CHAT_MAPPER.writeValueAsString(list));
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao gravar chat.");
        }
        game.setMultiChatRev(game.getMultiChatRev() + 1);
        gameRepository.save(game);
        return getMultiChat(gameId);
    }

    private List<Map<String, Object>> parseMultiChatMessages(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> parsed = MULTI_CHAT_MAPPER.readValue(json,
                    new TypeReference<ArrayList<Map<String, Object>>>() { });
            return parsed != null ? parsed : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
    
    public void resetCheckMateRepository() {
    	
    	int contPatterns = checkmateService.contCheckmatePatterById();
    	
    	if(contPatterns > 0) {
    		for(int i=0; i < contPatterns; i++) {
    			String val = String.valueOf(i);
    			long index = Long.parseLong(val);
    			CheckmatePattern patterns = checkmateService.getCheckmatePatterById(index);
    			if(patterns != null) {
    				patterns.setApply(true);
    				patterns.setIndexLastMove(0);
    				patterns.setIndexNextMove(0);
    			}
    		}
    		
    	}
    	
    	
    }

  
    public Game resetGame(String playerWhite, String playerBlack) {
        //Game game = gameRepository.findById(id)
                //.orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));
    
     //Game gameLast = new Game();
     //gameLast = gameRepository.findLastIdGame();
        Game game = new Game();
        //Long lastId = gameLast.getId() + 1;
        game.setId(2L);
        game.setPlayerWhite(playerWhite);
        game.setPlayerBlack(playerBlack);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setWhiteTurn(true);
        game.setCheckmate(false);
        game.setInCheck(false);
        game.setLastMove(null);
        game.setBoardState(serializeBoardState(initializeBoard()));
        game.setBlackKingMoved(false);
        game.setBlackRookA8Moved(false);
        game.setBlackRookH8Moved(false);
        game.setWhiteKingMoved(false);
        game.setWhiteRookA1Moved(false);
        game.setWhiteRookH1Moved(false);
        game.setMoves(null);
        Map<String, Piece> board = this.initializeBoard();
        game.setBoardState(this.serializeBoardState(board));
        //gameRepository.save(game);
        System.out.println();
        this.resetCheckMateRepository();
        Game gm = game;
        return gm;
    }
    
    public Optional<Game> getGame(Long id) {
        return gameRepository.findById(id);
    }
    
    private Map<String, Piece> initializeBoard() {
     Map<String, Piece> board = new HashMap<>();
        // Peças brancas
        board.put("a1", new Piece(PieceType.ROOK, PieceColor.WHITE,5,1));
        board.put("b1", new Piece(PieceType.KNIGHT, PieceColor.WHITE,3,1));
        board.put("c1", new Piece(PieceType.BISHOP, PieceColor.WHITE,3,1));
        board.put("d1", new Piece(PieceType.QUEEN, PieceColor.WHITE,9,0));
        board.put("e1", new Piece(PieceType.KING, PieceColor.WHITE,1000,0));
        board.put("f1", new Piece(PieceType.BISHOP, PieceColor.WHITE,3,2));
        board.put("g1", new Piece(PieceType.KNIGHT, PieceColor.WHITE,3,2));
        board.put("h1", new Piece(PieceType.ROOK, PieceColor.WHITE,5,2));
        int cont = 1;
        for (char file = 'a'; file <= 'h'; file++) {
            board.put(file + "2", new Piece(PieceType.PAWN, PieceColor.WHITE,1,cont));
            cont ++;
        }
        // Peças pretas
        board.put("a8", new Piece(PieceType.ROOK, PieceColor.BLACK,5,1));
        board.put("b8", new Piece(PieceType.KNIGHT, PieceColor.BLACK,3,1));
        board.put("c8", new Piece(PieceType.BISHOP, PieceColor.BLACK,3,1));
        board.put("d8", new Piece(PieceType.QUEEN, PieceColor.BLACK,9,0));
        board.put("e8", new Piece(PieceType.KING, PieceColor.BLACK,1000,0));
        board.put("f8", new Piece(PieceType.BISHOP, PieceColor.BLACK,3,2));
        board.put("g8", new Piece(PieceType.KNIGHT, PieceColor.BLACK,3,2));
        board.put("h8", new Piece(PieceType.ROOK, PieceColor.BLACK,5,2));
        cont = 1;
        for (char file = 'a'; file <= 'h'; file++) {
            board.put(file + "7", new Piece(PieceType.PAWN, PieceColor.BLACK,1,cont));
            cont ++;
        }
       
        return board;
    }
   
   
   
    public String serializeBoardState(Map<String, Piece> boardState) {
     Map<String, Piece> board = boardState;
     try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(boardState);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao serializar o estado do tabuleiro", e);
        }
    }
    public Map<String, Piece> deserializeBoardState(String boardState) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(boardState, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Piece>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar o estado do tabuleiro", e);
        }
    }
   
    public Map<String, Piece> desearilizeState(String boardState){
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(boardState, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Piece>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Erro ao desserializar o estado do tabuleiro", e);
        }
    }
   
    public GameDTO getGameDTO(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado: " + id));
       
        if (game == null) {
            System.out.println("Erro: Objeto Game é nulo em getGameDTO");
            throw new IllegalArgumentException("Jogo não pode ser nulo");
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
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
           //dto.setBoardStateHistory(game.getBoardStateHistory());
           dto.setWhiteKingMoved(game.isWhiteKingMoved());
           dto.setWhiteRookA1Moved(game.isWhiteRookA1Moved());
           dto. setWhiteRookH1Moved(game.isWhiteRookH1Moved());
           dto.setBlackKingMoved(game.isBlackKingMoved());
           dto.setBlackRookA8Moved(game.isBlackRookA8Moved());
           dto.setBlackRookH8Moved(game.isBlackRookH8Moved());
       
        System.out.println("GameDTO criado: id=" + dto.getId() + ", lastMove=" + dto.getLastMove());
        return dto;
   
    }
   
   
    public Game makeMove(Long id, String moveNotation) {
    	
        System.out.println("Recebendo movimento: " + moveNotation);
        Optional<Game> gameOpt = gameRepository.findById(id);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Jogo não encontrado: " + id);
        }
        Game game = gameOpt.get();
        //game.setWhiteTurn(!game.isWhiteTurn());

        Move move = parseNotation(moveNotation, game);
       
        List<String> movesBad = new ArrayList<String>();
        movesBad.add(moveNotation);
       
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        Piece piece = board.get(move.getFrom());
        if (piece == null) {
            throw new IllegalArgumentException("Nenhuma peça na posição de origem: " + move.getFrom());
        }
        if (piece.getColor() != (game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK)) {
            throw new IllegalArgumentException("Não é o turno dessa cor");
        }
        PieceColor playerColor = game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;
        PieceColor opponentColor = playerColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        // Verifica se o rei do jogador atual está em xeque antes do movimento
        boolean kingInCheckBefore = isKingInCheck(board, playerColor);
        // Executa o movimento com base no tipo de peça
        int fromRow = Character.getNumericValue(move.getFrom().charAt(1));
        int toRow = Character.getNumericValue(move.getTo().charAt(1));
        char fromCol = move.getFrom().charAt(0);
        char toCol = move.getTo().charAt(0);
        switch (piece.getType()) {
            case PAWN:
                handlePawnMove(game, board, piece, move.getFrom(), move.getTo(), fromRow, toRow, fromCol, toCol, playerColor, moveNotation);
                System.out.println("Notation: " + moveNotation);
                break;
            case KING:
                handleKingMove(game, board, piece, move.getFrom(), move.getTo(), fromRow, toRow, fromCol, toCol, playerColor, opponentColor);
                break;
            case ROOK:
                validateRookMove(board, fromRow, toRow, fromCol, toCol, piece);
                markRookMoved(game, move.getFrom(), playerColor);
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case KNIGHT:
                validateKnightMove(fromRow, toRow, fromCol, toCol, piece, board, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case BISHOP:
                validateBishopMove(board, fromRow, toRow, fromCol, toCol, piece, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case QUEEN:
                validateQueenMove(board, fromRow, toRow, fromCol, toCol, piece, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
        }
       
        // Se for roque, mover a torre também
        if (move.isCastling()) {
            if (move.getTo().equals("g1")) {
                board.put("f1", board.remove("h1")); // roque branco kingside
            } else if (move.getTo().equals("c1")) {
                board.put("d1", board.remove("a1")); // roque branco queenside
            } else if (move.getTo().equals("g8")) {
                board.put("f8", board.remove("h8")); // roque preto kingside
            } else if (move.getTo().equals("c8")) {
                board.put("d8", board.remove("a8")); // roque preto queenside
            }
        }
        // Verifica se o movimento coloca o próprio rei em xeque
        boolean kingInCheckAfter = isKingInCheck(board, playerColor);
        if (!kingInCheckBefore && kingInCheckAfter) {
            throw new IllegalArgumentException("Movimento inválido: coloca o rei em xeque");
        }
        if (kingInCheckBefore && kingInCheckAfter) {
            throw new IllegalArgumentException("Movimento inválido: o rei permanece em xeque");
        }
        // Atualiza o estado do jogo
        game.setBoardState(serializeBoardState(board));
        game.setWhiteTurn(!game.isWhiteTurn());
        game.setLastMove(moveNotation);
        game.getBoardStateHistory().add(game.getBoardState());
        // Verifica xeque no oponente após o movimento
        boolean opponentKingInCheck = isKingInCheck(board, opponentColor);
        game.setInCheck(opponentKingInCheck);
        System.out.println("Rei oponente em cheque: " + opponentKingInCheck);
        badOpening.setMoveSequence(moveNotation);
        // Verifica xeque-mate ou empate
        if (opponentKingInCheck) {
            try {
                boolean isCheckmate = isCheckmate(board, opponentColor);
                System.out.println("Cheque-mate verificado: " + isCheckmate);
                if (isCheckmate) {
                 badOpeningRepository.save(new BadOpenig(badOpening.getMoveSequence(),game.getMoves().size()));
                 System.out.println("Movimentos do jogo: " + game.getMoves().size());
                    game.setCheckmate(true);
                    game.setStatus(GameStatus.CHECKMATE);
                }else {
                 game.setCheckmate(false);
                }
            } catch (Exception e) {
                System.err.println("Erro ao verificar cheque-mate: " + e.getMessage());
                e.printStackTrace();
            }
           
            System.out.println("Último movimento, notation nº 8: " + moveNotation);
        }
         
        int cont = (int) movePieceRepository.count();
        MovePiece movePiece = new MovePiece();
        movePiece.setFromPiece(move.getFrom());
        movePiece.setToPiece(move.getTo());
        movePiece.setMov(moveNotation);
        movePiece.setTypePiece(piece.getType().toString());
        movePiece.setColorPiece(piece.getColor().toString());
        movePiece.setValuePiece(piece.getValuePiece());
        movePiece.setCodigoPiece(piece.getCodigo());
        movePiece.setContMove(cont + 1);
    
        movePiece.setLastMove(true);
        movePieceRepository.save(movePiece);
        
        MovePiece lastMovePieceTo = movePieceRepository.findMovePieceByToEqualsFrom(true,move.getFrom());
        if(lastMovePieceTo != null) {
         lastMovePieceTo.setLastMove(false);
         movePieceRepository.save(lastMovePieceTo);

        }else{
         MovePiece lastMovePieceFrom = movePieceRepository.findMovePieceByToEqualsFromLastMoveTrue(true, move.getFrom());
         if(lastMovePieceFrom != null) {
             lastMovePieceFrom.setLastMove(false);
             movePieceRepository.save(lastMovePieceFrom);
         }
        }
       
        String pieceColor = piece.getColor() == PieceColor.WHITE ? PieceColor.BLACK.toString() : PieceColor.WHITE.toString();
        MovePiece movePieceCaptureTo = movePieceRepository.findMovePieceByToCaptureTo(move.getTo(),true, pieceColor);
        if(movePieceCaptureTo != null) {
         movePieceCaptureTo.setLastMove(false);
         movePieceRepository.save(movePieceCaptureTo);
        }else {
            MovePiece movePieceCaptureFrom = movePieceRepository.findMovePieceByToCaptureFrom(move.getTo(),true, pieceColor);
            if(movePieceCaptureFrom != null) {
                movePieceCaptureFrom.setLastMove(false);
                movePieceRepository.save(movePieceCaptureFrom);
            }
        }
       
      
        addAllPossibleAttack(id, board,move.getTo());
      
        game.getMoves().add(move);
        gameRepository.save(game);
       
       
        List<MovePiece> movePieces = movePieceRepository.findAll();
        List<MovePiece> movePiecesBlack = movePieceRepository.findMovePieceByPieceColorLastMoveTrue(true,PieceColor.BLACK.toString());
        List<MovePiece> movePiecesWhite = movePieceRepository.findMovePieceByPieceColorLastMoveTrue(true,PieceColor.WHITE.toString());
        if(!movePieces.isEmpty()) {
         System.err.println("Total de Registros: " + movePieces.size());
         System.err.println("Total de Registros: " + movePiecesBlack.size());
         System.err.println("Total de Registros: " + movePiecesWhite.size());
        }
       
          
        return game;
    }
  
    private void addAllPossibleAttack(Long gameId, Map<String, Piece> board, String to) {
    	
		 List<MovePiece> pieceWhite = makeListWhitePieces();
		 List<MovePiece> pieceBlack = makeListBlackPieces();
		 int contMovPiece = 0;
		 List<MovePiece> pieceMoves = null;
		 for(int y = 1; y < 3; y++) {
			 pieceMoves = y == 1 ? pieceWhite : pieceBlack;
			 
		     for(MovePiece movePiece : pieceMoves) {
		         List<String> listMoves = null;
		         Piece pieceSelect = selectPiece(movePiece.getTypePiece(),movePiece.getColorPiece(),movePiece.getCodigoPiece());
		         //System.err.println("Peça: " + pieceSelect.getType() + " Color: " + pieceSelect.getColor() + " código: " + pieceSelect.getCodigo());
		         if(pieceSelect != null) {
		             MovePiece movePieceAct = movePieceRepository.findMovePieceByTypePieceLastMoveTrueCodigoPieceColorPiece( true, pieceSelect.getType().toString(), pieceSelect.getCodigo(),pieceSelect.getColor().toString());
		             contMovPiece = movePieceRepository.countPieceByTypeColorAndCodigo(pieceSelect.getType().toString(),pieceSelect.getColor().toString() , pieceSelect.getCodigo());
		             String from = null;
		             if(movePieceAct != null) {
		            	 from = movePieceAct.getToPiece() != null ? movePieceAct.getToPiece() : movePieceAct.getFromPiece();
		                 listMoves = null;
		                 listMoves = getPossibleMovesForPiece(gameId, board, from, pieceSelect);
	                	 movePieceAct.setMoveAttack(listMoves); 

		                 movePieceRepository.save(movePieceAct);
		                
		             }
		         }
		     }
		 }
    
    }
    
    private List<MovePiece> addAllPossibleAttackNextMove(Long gameId, Map<String, Piece> board, String to, List<MovePiece> blackMovPiece) {
    	
		 List<MovePiece> pieceWhite = makeListWhitePieces();
		 List<MovePiece> pieceBlack = blackMovPiece;
		 int contMovPiece = 0;
		 List<MovePiece> pieceMoves = null;
		 List<String> listMoves = null;
		 String from = null;
		 String line = null;
		 Piece pieceSelect = new Piece();
		// List<String> listMoves = null;
		 for(MovePiece black : blackMovPiece) {
	         
	         pieceSelect = selectPiece(black.getTypePiece(),black.getColorPiece(),black.getCodigoPiece());
	         if(pieceSelect != null) {
	             
	             if(black != null) {
	            	 from = black.getToPiece() != null ? black.getToPiece() : black.getFromPiece();
	                 line = from.substring(1,2);
 	                 listMoves = null;
	                 listMoves = getPossibleMovesForPiece(gameId, board, from, pieceSelect);
                	 black.setMoveAttack(listMoves); 

	             }
	         }
	         System.err.println("Peça: " + black.getTypePiece() + " casa: " + from + " line: " + line);
	         for(String mov : black.getMoveAttack()) {
	        	 System.err.println("Posição de attack: " + mov);
	         }
		 }

		 for(MovePiece white : pieceWhite) {
	         listMoves = null;
	         pieceSelect = selectPiece(white.getTypePiece(),white.getColorPiece(),white.getCodigoPiece());
	         if(pieceSelect != null) {
	             from = null;
	             if(white != null) {
	            	 from = white.getToPiece() != null ? white.getToPiece() : white.getFromPiece();
	                 listMoves = null;
	                 listMoves = getPossibleMovesForPiece(gameId, board, from, pieceSelect);
	                 white.setMoveAttack(null);
                	 white.setMoveAttack(listMoves); 
                	 
	             }

	         }
	         System.err.println("Peça: " + white.getTypePiece() + " casa: " + from);
	         for(String mov : white.getMoveAttack()) {
	        	 System.err.println("Posição de attack: " + mov);
	         }
		 }
		
		 return pieceWhite;
    }
   
   
   
    private Piece selectPiece(String pieceType, String color, int codigoPiece) {
        PieceColor pieceColor = "WHITE".equals(color) ? PieceColor.WHITE : PieceColor.BLACK;
        switch (pieceType) {
            case "ROOK":   return new Piece(PieceType.ROOK,   pieceColor,5,codigoPiece);
            case "KNIGHT": return new Piece(PieceType.KNIGHT, pieceColor,3,codigoPiece);
            case "BISHOP": return new Piece(PieceType.BISHOP, pieceColor,3,codigoPiece);
            case "QUEEN":  return new Piece(PieceType.QUEEN,  pieceColor,9,codigoPiece);
            case "KING":   return new Piece(PieceType.KING,   pieceColor,1000,codigoPiece);
            case "PAWN":   return new Piece(PieceType.PAWN,   pieceColor,1,codigoPiece);
            default: throw new IllegalArgumentException("Tipo de peça inválido: " + pieceType);
        }
    }

    
    
    private PieceType parsePromotion(String moveNotation) {
        if (moveNotation != null && moveNotation.length() > 4) {
            char promotionChar = moveNotation.charAt(4);
            switch (Character.toLowerCase(promotionChar)) {
                case 'q': return PieceType.QUEEN;
                case 'r': return PieceType.ROOK;
                case 'b': return PieceType.BISHOP;
                case 'n': return PieceType.KNIGHT;
                default: throw new IllegalArgumentException("Peça de promoção inválida: " + promotionChar);
            }
        }
        // Padrão: promover para rainha se não especificado
        return PieceType.QUEEN;
    }
   
    public boolean canCastleKingside(Map<String, Piece> board, Game game, PieceColor color) {
        if (color == PieceColor.WHITE) {
            if (game.isWhiteKingMoved() || game.isWhiteRookH1Moved()) return false;
            if (board.containsKey("f1") || board.containsKey("g1")) return false;
            if (isKingInCheck(board, color)) return false;
            if (isSquareUnderAttack(board, "f1", PieceColor.BLACK) || isSquareUnderAttack(board, "g1", PieceColor.BLACK))
                return false;
            return true;
        } else {
            if (game.isBlackKingMoved() || game.isBlackRookH8Moved()) return false;
            if (board.containsKey("f8") || board.containsKey("g8")) return false;
            if (isKingInCheck(board, color)) return false;
            if (isSquareUnderAttack(board, "f8", PieceColor.WHITE) || isSquareUnderAttack(board, "g8", PieceColor.WHITE))
                return false;
            return true;
        }
    }
    public boolean canCastleQueenside(Map<String, Piece> board, Game game, PieceColor color) {
        if (color == PieceColor.WHITE) {
            if (game.isWhiteKingMoved() || game.isWhiteRookA1Moved()) return false;
            if (board.containsKey("b1") || board.containsKey("c1") || board.containsKey("d1")) return false;
            if (isKingInCheck(board, color)) return false;
            if (isSquareUnderAttack(board, "c1", PieceColor.BLACK) || isSquareUnderAttack(board, "d1", PieceColor.BLACK))
                return false;
            return true;
        } else {
            if (game.isBlackKingMoved() || game.isBlackRookA8Moved()) return false;
            if (board.containsKey("b8") || board.containsKey("c8") || board.containsKey("d8")) return false;
            if (isKingInCheck(board, color)) return false;
            if (isSquareUnderAttack(board, "c8", PieceColor.WHITE) || isSquareUnderAttack(board, "d8", PieceColor.WHITE))
                return false;
            return true;
        }
    }
   
   
    public Move parseNotation(String moveNotation, Game game) {
        if (moveNotation == null || moveNotation.isEmpty()) {
            throw new IllegalArgumentException("Notação inválida: notação vazia");
        }
        moveNotation = moveNotation.trim();
        String cleanNotation = moveNotation.replace(" ", "");
        // Verifica se é roque
        if (cleanNotation.equals("O-O") || cleanNotation.equals("0-0")) {
            return new Move(game.isWhiteTurn() ? "e1" : "e8", game.isWhiteTurn() ? "g1" : "g8", true, false);
        }
        if (cleanNotation.equals("O-O-O") || cleanNotation.equals("0-0-0")) {
            return new Move(game.isWhiteTurn() ? "e1" : "e8", game.isWhiteTurn() ? "c1" : "c8", true, false);
        }
        String from, to;
        boolean isCapture = cleanNotation.contains("x");
        if (isCapture) {
            String[] parts = cleanNotation.split("x");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Notação de captura inválida: " + moveNotation);
            }
            String originFile = parts[0]; // ex.: "h"
            String destination = parts[1]; // ex.: "g5"
            if (originFile.length() != 1 || !files.contains(originFile)) {
                throw new RuntimeException("Peça inválida na notação: " + originFile);
            }
            if (destination.length() != 2 || !files.contains(destination.substring(0, 1)) || !ranks.contains(destination.substring(1))) {
                throw new IllegalArgumentException("Destino inválido na notação: " + destination);
            }
            Map<String, Piece> board = deserializeBoardState(game.getBoardState());
            PieceColor color = game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;
            int destRank = Integer.parseInt(destination.substring(1));
            int direction = color == PieceColor.WHITE ? 1 : -1;
            String origin = null;
            for (int rank = 1; rank <= 8; rank++) {
                String pos = originFile + rank;
                Piece piece = board.get(pos);
                if (piece != null && piece.getType() == PieceType.PAWN && piece.getColor() == color) {
                    int rankDiff = destRank - rank;
                    if (rankDiff == direction && Math.abs(files.indexOf(destination.substring(0, 1)) - files.indexOf(originFile)) == 1) {
                        origin = pos;
                        break;
                    }
                }
            }
            if (origin == null) {
                throw new IllegalArgumentException("Nenhum peão válido encontrado para a captura: " + moveNotation);
            }
            from = origin;
            to = destination;
        } else {
            if (cleanNotation.length() < 4) {
                throw new IllegalArgumentException("Notação inválida para movimento simples: " + moveNotation);
            }
            from = cleanNotation.substring(0, 2);
            to = cleanNotation.substring(2, 4);
            if (!files.contains(from.substring(0, 1)) || !ranks.contains(from.substring(1)) ||
                !files.contains(to.substring(0, 1)) || !ranks.contains(to.substring(1))) {
                throw new IllegalArgumentException("Posições inválidas na notação: " + moveNotation);
            }
        }
        return new Move(from, to,false,false,game, game.getId());
    }
    // Métodos auxiliares organizados
    private void handleKingMove(Game game, Map<String, Piece> board, Piece movingPiece, String from, String to,
        int fromRow, int toRow, char fromCol, char toCol, PieceColor playerColor,
        PieceColor opponentColor) {
		int colDiff = toCol - fromCol;
		int rowDiff = Math.abs(toRow - fromRow);
		if (Math.abs(colDiff) == 2 && fromRow == toRow) { // Roque
		boolean isKingside = colDiff > 0;
		String rookFrom = isKingside ? (playerColor == PieceColor.WHITE ? "h1" : "h8")
		: (playerColor == PieceColor.WHITE ? "a1" : "a8");
		String rookTo = isKingside ? (playerColor == PieceColor.WHITE ? "f1" : "f8")
		: (playerColor == PieceColor.WHITE ? "d1" : "d8");
		boolean kingMoved = playerColor == PieceColor.WHITE ? game.isWhiteKingMoved() : game.isBlackKingMoved();
		boolean rookMoved = playerColor == PieceColor.WHITE
		? (isKingside ? game.isWhiteRookH1Moved() : game.isWhiteRookA1Moved())
		: (isKingside ? game.isBlackRookH8Moved() : game.isBlackRookA8Moved());
		if (!from.equals(playerColor == PieceColor.WHITE ? "e1" : "e8")) {
			throw new RuntimeException("Posição inicial inválida para roque");
		}
		if (kingMoved || rookMoved) {
			throw new RuntimeException("Roque inválido: rei ou torre já se moveram");
		}
		if (isKingInCheck(board, playerColor)) {
			throw new RuntimeException("Roque inválido: rei está em xeque");
		}
		if (!board.containsKey(rookFrom) || board.get(rookFrom).getType() != PieceType.ROOK) {
			throw new RuntimeException("Roque inválido: torre não está na posição esperada");
		}
		int step = isKingside ? 1 : -1;
		for (char c = (char) (fromCol + step); c != toCol + step; c += step) {
			String pos = c + String.valueOf(fromRow);
			if (board.containsKey(pos) && !pos.equals(to)) {
				throw new RuntimeException("Roque inválido: caminho obstruído");
			}
			if (isSquareUnderAttack(board, pos, opponentColor)) {
				throw new RuntimeException("Roque inválido: casa sob ataque");
			}
		}
		Piece rook = board.remove(rookFrom);
		board.put(rookTo, rook);
		board.put(to, movingPiece);
		board.remove(from);
		if (playerColor == PieceColor.WHITE) {
			game.setWhiteKingMoved(true);
			if (isKingside)
				game.setWhiteRookH1Moved(true);
			else
				game.setWhiteRookA1Moved(true);
			} else {
				game.setBlackKingMoved(true);
				if (isKingside)
					game.setBlackRookH8Moved(true);
				else
					game.setBlackRookA8Moved(true);
			}
			} else { // Movimento normal do rei
			if (rowDiff > 1 || Math.abs(colDiff) > 1) {
				throw new RuntimeException("O rei só pode se mover uma casa em qualquer direção");
			}
			if (board.containsKey(to) && board.get(to).getColor() == movingPiece.getColor()) {
				throw new RuntimeException("O rei não pode capturar peça da mesma cor");
			}
			if (playerColor == PieceColor.WHITE)
				game.setWhiteKingMoved(true);
			else
				game.setBlackKingMoved(true);
				executeMove(board, from, to, movingPiece, playerColor);
			}
		}
   
    private void handlePawnMove(Game game, Map<String, Piece> board, Piece movingPiece, String from, String to,
            int fromRow, int toRow, char fromCol, char toCol, PieceColor playerColor, String moveNotation) {
        int direction = playerColor == PieceColor.WHITE ? 1 : -1;
        int startRow = playerColor == PieceColor.WHITE ? 2 : 7;
        boolean isCapture = board.containsKey(to);
       
        System.out.println("Processando movimento de peão: " + from + " -> " + to + ", notação: " + moveNotation);
       
        if (fromCol == toCol) { // Movimento reto
            if (isCapture) {
                throw new RuntimeException("Peão não pode capturar em linha reta");
            }
            if (toRow - fromRow == direction) {
                // Avanço de 1 casa
            } else if (fromRow == startRow && toRow - fromRow == 2 * direction) {
                String intermediate = fromCol + String.valueOf(fromRow + direction);
                if (board.containsKey(intermediate)) {
                    throw new RuntimeException("Peão não pode pular sobre peças");
                }
            } else {
                throw new RuntimeException("Movimento inválido de peão");
            }
        } else if (Math.abs(fromCol - toCol) == 1 && toRow - fromRow == direction) { // Diagonal
            if (board.containsKey(to)) { // Captura normal
                if (board.get(to).getColor() == movingPiece.getColor()) {
                    throw new RuntimeException("Peão não pode capturar peça da mesma cor");
                }
            } else { // Possível en passant
                String lastMove = game.getLastMove();
                System.out.println("Verificando en passant. Último movimento: " + lastMove);
                if (lastMove != null && lastMove.length() >= 4) {
                    String lastFrom = lastMove.substring(0, 2);
                    String lastTo = lastMove.substring(2, 4);
                    int lastFromRow = Character.getNumericValue(lastFrom.charAt(1));
                    int lastToRow = Character.getNumericValue(lastTo.charAt(1));
                    char lastToCol = lastTo.charAt(0);
                    String enPassantTarget = toCol + String.valueOf(fromRow);
                   
                    System.out.println("enPassantTarget: " + enPassantTarget + ", lastTo: " + lastTo + ", lastFrom: " + lastFrom);
                    if (board.containsKey(enPassantTarget) &&
                        board.get(enPassantTarget).getType() == PieceType.PAWN &&
                        board.get(enPassantTarget).getColor() != playerColor &&
                        lastTo.equals(enPassantTarget) &&
                        Math.abs(lastToRow - lastFromRow) == 2 &&
                        lastToCol == toCol &&
                        (playerColor == PieceColor.WHITE ? fromRow == 5 : fromRow == 4)) {
                        System.out.println("En passant confirmado. Removendo peão em: " + enPassantTarget);
                        board.remove(enPassantTarget); // Remove o peão capturado
                    } else {
                        System.out.println("En passant inválido. Condições não atendidas.");
                        throw new RuntimeException("Movimento diagonal inválido: captura ou en passant não permitido");
                    }
                } else {
                    System.out.println("En passant inválido. Nenhum movimento anterior.");
                    throw new RuntimeException("Movimento diagonal inválido: nenhum movimento anterior para en passant");
                }
            }
        } else {
            throw new RuntimeException("Movimento inválido de peão");
        }
       
        // Promoção
        if (playerColor == PieceColor.WHITE && toRow == 8 || playerColor == PieceColor.BLACK && toRow == 1) {
            PieceType promotedType = parsePromotion(moveNotation); // Usa a notação do movimento atual
            int pieceValue;
            int codigo = 0;
            MovePiece movPiece = null;
            switch (promotedType) {
                case QUEEN:
                    pieceValue = 9;
                    movPiece = movePieceRepository.findMaxCodigoValuePieceByLineAndColor(playerColor.toString(),true,PieceType.QUEEN.toString());
                    codigo = movPiece != null ? movPiece.getValuePiece() + 1 : 1;
                    break;
                case ROOK:
                    pieceValue = 5;
                    movPiece = movePieceRepository.findMaxCodigoValuePieceByLineAndColor(playerColor.toString(),true,PieceType.ROOK.toString());
                    codigo = movPiece != null ? movPiece.getValuePiece() + 1 : 1;
                    break;
                case KNIGHT:
                    pieceValue = 3;
                    movPiece = movePieceRepository.findMaxCodigoValuePieceByLineAndColor(playerColor.toString(),true,PieceType.KNIGHT.toString());
                    codigo = movPiece != null ? movPiece.getValuePiece() + 1 : 1;
                    break;
                case BISHOP:
                    pieceValue = 3;
                    movPiece = movePieceRepository.findMaxCodigoValuePieceByLineAndColor(playerColor.toString(),true,PieceType.BISHOP.toString());
                    codigo = movPiece != null ? movPiece.getValuePiece() + 1 : 1;
                    break;
                default:
                    throw new IllegalArgumentException("Peça de promoção inválida: " + promotedType);
            }
            movingPiece = new Piece(promotedType, playerColor, pieceValue, codigo);
            System.out.println("Peão promovido para: " + promotedType + " em " + to);
        }
       
        executeMove(board, from, to, movingPiece, playerColor);
        System.out.println("Tabuleiro após movimento: " + board);
    }
   
   
    private void validateRookMove(Map<String, Piece> board, int fromRow, int toRow, char fromCol, char toCol, Piece movingPiece) {
        boolean isHorizontal = fromRow == toRow;
        boolean isVertical = fromCol == toCol;
        if (!(isHorizontal || isVertical)) {
            throw new RuntimeException("Movimento inválido da torre. Só pode mover em linha reta.");
        }
        checkPathClear(board, fromRow, toRow, fromCol, toCol, isHorizontal ? "horizontal" : "vertical", movingPiece);
    }
    private void validateKnightMove(int fromRow, int toRow, char fromCol, char toCol, Piece movingPiece, Map<String, Piece> board, String to) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        if (!((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2))) {
            throw new RuntimeException("Movimento inválido do cavalo");
        }
        if (board.containsKey(to) && board.get(to).getColor() == movingPiece.getColor()) {
            throw new RuntimeException("Cavalo não pode capturar peça da mesma cor");
        }
    }
    private void validateBishopMove(Map<String, Piece> board, int fromRow, int toRow, char fromCol, char toCol, Piece movingPiece, String to) {
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        if (Math.abs(rowDiff) != Math.abs(colDiff)) {
            throw new RuntimeException("Movimento inválido do bispo. Deve ser em diagonal.");
        }
        checkPathClear(board, fromRow, toRow, fromCol, toCol, "diagonal", movingPiece);
    }
    private void validateQueenMove(Map<String, Piece> board, int fromRow, int toRow, char fromCol, char toCol, Piece movingPiece, String to) {
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        boolean isHorizontal = rowDiff == 0 && colDiff != 0;
        boolean isVertical = colDiff == 0 && rowDiff != 0;
        boolean isDiagonal = Math.abs(rowDiff) == Math.abs(colDiff) && rowDiff != 0;
        if (!(isHorizontal || isVertical || isDiagonal)) {
            throw new RuntimeException("Movimento inválido da rainha. Deve ser em linha reta ou diagonal.");
        }
        checkPathClear(board, fromRow, toRow, fromCol, toCol, isHorizontal ? "horizontal" : isVertical ? "vertical" : "diagonal", movingPiece);
    }
    private void checkPathClear(Map<String, Piece> board, int fromRow, int toRow, char fromCol, char toCol, String direction, Piece movingPiece) {
        if (direction.equals("horizontal")) {
            int step = toCol > fromCol ? 1 : -1;
            for (int c = fromCol + step; c != toCol; c += step) {
                String pos = (char) c + String.valueOf(fromRow);
                if (board.containsKey(pos)) {
                    throw new RuntimeException(movingPiece.getType() + " não pode pular sobre peças");
                }
            }
        } else if (direction.equals("vertical")) {
            int step = toRow > fromRow ? 1 : -1;
            for (int r = fromRow + step; r != toRow; r += step) {
                String pos = String.valueOf(fromCol) + r;
                if (board.containsKey(pos)) {
                    throw new RuntimeException(movingPiece.getType() + " não pode pular sobre peças");
                }
            }
        } else { // diagonal
            int rowStep = toRow > fromRow ? 1 : -1;
            int colStep = toCol > fromCol ? 1 : -1;
            int r = fromRow + rowStep;
            int c = fromCol + colStep;
            while (r != toRow && c != toCol) {
                String pos = (char) c + String.valueOf(r);
                if (board.containsKey(pos)) {
                    throw new RuntimeException(movingPiece.getType() + " não pode pular sobre peças");
                }
                r += rowStep;
                c += colStep;
            }
        }
        String to = toCol + String.valueOf(toRow);
        if (board.containsKey(to) && board.get(to).getColor() == movingPiece.getColor()) {
            throw new RuntimeException(movingPiece.getType() + " não pode capturar peça da mesma cor");
        }
    }
    private void markRookMoved(Game game, String from, PieceColor playerColor) {
        if (playerColor == PieceColor.WHITE) {
            if (from.equals("a1")) game.setWhiteRookA1Moved(true);
            else if (from.equals("h1")) game.setWhiteRookH1Moved(true);
        } else {
            if (from.equals("a8")) game.setBlackRookA8Moved(true);
            else if (from.equals("h8")) game.setBlackRookH8Moved(true);
        }
    }
    private void executeMove(Map<String, Piece> board, String from, String to, Piece movingPiece, PieceColor playerColor) {
        Piece target = board.get(to);
        board.put(to, movingPiece);
        board.remove(from);
        if (isKingInCheck(board, playerColor)) {
            board.remove(to);
            board.put(from, movingPiece);
            if (target != null) board.put(to, target);
            throw new RuntimeException("Movimento inválido: coloca o próprio rei em xeque");
        }
    }
    private void updateGameState(Game game, Map<String, Piece> board, String notation, PieceColor opponentColor, ObjectMapper mapper) throws Exception {
        String newBoardState = mapper.writeValueAsString(board);
        game.setBoardState(newBoardState);
        game.setLastMove(notation);
        String positionKey = newBoardState + game.isWhiteTurn() +
                            game.isWhiteKingMoved() + game.isWhiteRookA1Moved() +
                            game.isWhiteRookH1Moved() + game.isBlackKingMoved() +
                            game.isBlackRookA8Moved() + game.isBlackRookH8Moved();
        game.getBoardStateHistory().add(positionKey);
        long repetitionCount = game.getBoardStateHistory().stream()
            .filter(state -> state.equals(positionKey))
            .count();
        if (repetitionCount >= 3) {
            game.setStatus(GameStatus.DRAW); // Corrigido para usar enum
        } else if (isKingInCheck(board, opponentColor)) {
            if (isCheckmate(board, opponentColor)) {
                game.setStatus(GameStatus.CHECKMATE); // Corrigido para usar enum
                game.setWhiteTurn(!game.isWhiteTurn());
            }
        } else if (isStalemate(board, opponentColor)) {
            game.setStatus(GameStatus.DRAW); // Corrigido para usar enum
        }
        if (game.getStatus() != GameStatus.CHECKMATE && game.getStatus() != GameStatus.DRAW) {
            game.setWhiteTurn(!game.isWhiteTurn());
        }
    }
   
   
    private boolean isKingInCheck(Map<String, Piece> board, PieceColor kingColor) {
        String kingPosition = null;
        // 1. Encontrar a posição atual do rei da cor fornecida
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            if(piece != null) {
                if (piece.getType() == PieceType.KING && piece.getColor() == kingColor) {
                    kingPosition = entry.getKey();
                    break;
                }
            }
        }
        if (kingPosition == null) return false;
        PieceColor opponentColor = kingColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        // 2. Verificar se alguma peça do oponente pode atingir a posição do rei
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            String from = entry.getKey();
            Piece piece = entry.getValue();
            if(piece != null) {
                if (piece.getColor() == opponentColor) {
                    // ⚠️ Aqui usamos a cor real da peça para verificar o movimento
                    if (isMoveLegal(board, from, kingPosition, piece.getColor())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
   
   
    private boolean isPathClear(Map<String, Piece> board, String from, String to, boolean isStraight) {
        int fromRow = Character.getNumericValue(from.charAt(1));
        int toRow = Character.getNumericValue(to.charAt(1));
        char fromCol = from.charAt(0);
        char toCol = to.charAt(0);
        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;
        if (isStraight) {
            if (rowDiff == 0) { // Horizontal
                int step = colDiff > 0 ? 1 : -1;
                for (int c = fromCol + step; c != toCol; c += step) {
                    String pos = (char) c + String.valueOf(fromRow);
                    if (board.containsKey(pos)) {
                        return false;
                    }
                }
            } else { // Vertical
                int step = rowDiff > 0 ? 1 : -1;
                for (int r = fromRow + step; r != toRow; r += step) {
                    String pos = String.valueOf(fromCol) + r;
                    if (board.containsKey(pos)) {
                        return false;
                    }
                }
            }
        } else { // Diagonal
            int rowStep = rowDiff > 0 ? 1 : -1;
            int colStep = colDiff > 0 ? 1 : -1;
            int r = fromRow + rowStep;
            int c = fromCol + colStep;
            while (r != toRow && c != toCol) {
                String pos = (char) c + String.valueOf(r);
                if (board.containsKey(pos)) {
                    return false;
                }
                r += rowStep;
                c += colStep;
            }
        }
        return true;
    }
   
    private boolean isCheckmate(Map<String, Piece> board, PieceColor kinColor) {
        // Verificar se o rei está em cheque
        if (!isKingInCheck(board, kinColor)) {
            System.out.println("Não é cheque-mate: rei de " + kinColor + " não está em cheque");
            return false;
        }
        System.out.println("Verificando cheque-mate para " + kinColor);
        // Encontrar a posição do rei
        String kingPosition = null;
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            if (piece.getType() == PieceType.KING && piece.getColor() == kinColor) {
                kingPosition = entry.getKey();
                break;
            }
        }
        if (kingPosition == null) {
            System.err.println("Erro: Rei de " + kinColor + " não encontrado");
            return false;
        }
        // Verificar movimentos possíveis para todas as peças do jogador
        for (Map.Entry<String, Piece> fromEntry : board.entrySet()) {
            Piece piece = fromEntry.getValue();
            if (piece.getColor() != kinColor) {
                continue; // Pular peças do oponente
            }
            String from = fromEntry.getKey();
            // Obter movimentos legais diretamente, se possível
            List<String> possibleMoves = getPossibleMoves(board, from, piece, kinColor);
            System.out.println("Testando peça: " + piece.getType() + " em " + from + ", movimentos possíveis: " + possibleMoves.size());
            for (String to : possibleMoves) {
                // Simular movimento
                Map<String, Piece> tempBoard = new HashMap<>(board);
                Piece captured = tempBoard.get(to);
                tempBoard.put(to, tempBoard.remove(from));
                // Verificar se o rei sai do cheque
                boolean stillInCheck = isKingInCheck(tempBoard, kinColor);
                System.out.println("Movimento simulado: " + from + " -> " + to + ", ainda em cheque: " + stillInCheck);
                if (!stillInCheck) {
                    System.out.println("Escapatória encontrada: " + from + " -> " + to);
                    return false;
                }
            }
        }
        System.out.println("Cheque-mate confirmado para " + kinColor);
        return true;
    }
   
    private List<String> getPossibleMoves(Map<String, Piece> board, String from, Piece piece, PieceColor color) {
        List<String> moves = new ArrayList<>();
        PieceType type = piece.getType();
        int fromCol = from.charAt(0) - 'a';
        int fromRow = Character.getNumericValue(from.charAt(1)) - 1;
        System.out.println("Calculando movimentos para " + type + " em " + from);
        if (type == PieceType.PAWN) {
            int direction = (color == PieceColor.WHITE) ? 1 : -1;
            int startRow = (color == PieceColor.WHITE) ? 1 : 6;
            // Movimento simples
            String to = "" + from.charAt(0) + (fromRow + direction + 1);
            if (!board.containsKey(to)) {
                moves.add(to);
            }
            // Movimento duplo
            if (fromRow == startRow) {
                to = "" + from.charAt(0) + (fromRow + 2 * direction + 1);
                String intermediate = "" + from.charAt(0) + (fromRow + direction + 1);
                if (!board.containsKey(intermediate) && !board.containsKey(to)) {
                    moves.add(to);
                }
            }
            // Capturas
            for (int colOffset : new int[]{-1, 1}) {
                char toCol = (char) (from.charAt(0) + colOffset);
                if (toCol >= 'a' && toCol <= 'h') {
                    to = "" + toCol + (fromRow + direction + 1);
                    Piece target = board.get(to);
                    if (target != null && target.getColor() != color) {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.KING) {
            int[][] offsets = {{-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}};
            for (int[] offset : offsets) {
                char toCol = (char) (from.charAt(0) + offset[0]);
                int toRow = fromRow + offset[1] + 1;
                if (toCol >= 'a' && toCol <= 'h' && toRow >= 1 && toRow <= 8) {
                    String to = "" + toCol + toRow;
                    Piece target = board.get(to);
                    if ((target == null || target.getColor() != color)) {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.BISHOP) {
            int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] dir : directions) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (board.containsKey(to)) {
                        if (board.get(to).getColor() != color) moves.add(to);
                        break;
                    } else {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.QUEEN) {
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
            for (int[] dir : directions) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (board.containsKey(to)) {
                        if (board.get(to).getColor() != color) moves.add(to);
                        break;
                    } else {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.KNIGHT) {
            int[][] knightOffsets = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
            for (int[] offset : knightOffsets) {
                char toCol = (char) (from.charAt(0) + offset[0]);
                int toRow = fromRow + offset[1] + 1;
                if (toCol >= 'a' && toCol <= 'h' && toRow >= 1 && toRow <= 8) {
                    String to = "" + toCol + toRow;
                    if (!board.containsKey(to) || board.get(to).getColor() != color) {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.ROOK) {
            int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : directions) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (board.containsKey(to)) {
                        if (board.get(to).getColor() != color) moves.add(to);
                        break;
                    } else {
                        moves.add(to);
                    }
                }
            }
        }
        return moves;
    }
   
    private boolean isMoveLegal(Map<String, Piece> board, String from, String to, PieceColor playerColor) {
        Piece movingPiece = board.get(from);
        if (movingPiece == null || movingPiece.getColor() != playerColor) {
            return false;
        }
        try {
            // Copia o tabuleiro para simulação
            Map<String, Piece> tempBoard = new HashMap<>(board);
            Piece target = tempBoard.get(to);
            // Validação básica para cada tipo de peça (reutiliza lógica do makeMove)
            if (movingPiece.getType() == PieceType.PAWN) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                int direction = movingPiece.getColor() == PieceColor.WHITE ? 1 : -1;
                int startRow = movingPiece.getColor() == PieceColor.WHITE ? 2 : 7;
                boolean isCapture = tempBoard.containsKey(to);
                if (fromCol == toCol) {
                    if (isCapture) return false;
                    if (toRow - fromRow == direction) return true;
                    if (fromRow == startRow && toRow - fromRow == 2 * direction) {
                        String intermediate = fromCol + String.valueOf(fromRow + direction);
                        return !tempBoard.containsKey(intermediate);
                    }
                } else if (Math.abs(fromCol - toCol) == 1 && toRow - fromRow == direction) {
                 return isCapture && target != null && target.getColor() != movingPiece.getColor();
                }
                return false;
            } else if (movingPiece.getType() == PieceType.ROOK) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                boolean isHorizontal = fromRow == toRow;
                boolean isVertical = fromCol == toCol;
                if (!(isHorizontal || isVertical)) return false;
                return isPathClear(tempBoard, from, to, true) &&
                       (!tempBoard.containsKey(to) || tempBoard.get(to).getColor() != movingPiece.getColor());
            } else if (movingPiece.getType() == PieceType.KNIGHT) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                int rowDiff = Math.abs(toRow - fromRow);
                int colDiff = Math.abs(toCol - fromCol);
                boolean isValidMove = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
                return isValidMove && (!tempBoard.containsKey(to) || tempBoard.get(to).getColor() != movingPiece.getColor());
            } else if (movingPiece.getType() == PieceType.BISHOP) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                if (Math.abs(toRow - fromRow) != Math.abs(toCol - fromCol)) return false;
                return isPathClear(tempBoard, from, to, false) &&
                       (!tempBoard.containsKey(to) || tempBoard.get(to).getColor() != movingPiece.getColor());
            } else if (movingPiece.getType() == PieceType.QUEEN) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                int rowDiff = toRow - fromRow;
                int colDiff = toCol - fromCol;
                boolean isHorizontal = rowDiff == 0 && colDiff != 0;
                boolean isVertical = colDiff == 0 && rowDiff != 0;
                boolean isDiagonal = Math.abs(rowDiff) == Math.abs(colDiff) && rowDiff != 0;
                if (!(isHorizontal || isVertical || isDiagonal)) return false;
                return isPathClear(tempBoard, from, to, isHorizontal || isVertical) &&
                       (!tempBoard.containsKey(to) || tempBoard.get(to).getColor() != movingPiece.getColor());
            } else if (movingPiece.getType() == PieceType.KING) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                int toRow = Character.getNumericValue(to.charAt(1));
                char fromCol = from.charAt(0);
                char toCol = to.charAt(0);
                int rowDiff = Math.abs(toRow - fromRow);
                int colDiff = Math.abs(toCol - fromCol);
                return (rowDiff <= 1 && colDiff <= 1) &&
                       (!tempBoard.containsKey(to) || tempBoard.get(to).getColor() != movingPiece.getColor());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
   
   
    private boolean isStalemate(Map<String, Piece> board, PieceColor playerColor) {
        if (isKingInCheck(board, playerColor)) {
            return false;
        }
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            String from = entry.getKey();
            if (piece.getColor() == playerColor) {
                for (char col = 'a'; col <= 'h'; col++) {
                    for (int row = 1; row <= 8; row++) {
                        String to = col + String.valueOf(row);
                        if (!from.equals(to) && isMoveLegal(board, from, to, playerColor)) {
                            Map<String, Piece> tempBoard = new HashMap<>(board);
                            tempBoard.put(to, tempBoard.remove(from));
                            if (!isKingInCheck(tempBoard, playerColor)) {
                                return false; // Há um movimento legal
                            }
                        }
                    }
                }
            }
        }
        return true; // Nenhum movimento legal disponível
    }
   
   
    private boolean isSquareUnderAttack(Map<String, Piece> board, String square, PieceColor attackerColor) {
        int squareRow = Character.getNumericValue(square.charAt(1));
        char squareCol = square.charAt(0);
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            String from = entry.getKey();
            if (piece.getColor() == attackerColor) {
                int fromRow = Character.getNumericValue(from.charAt(1));
                char fromCol = from.charAt(0);
                if (piece.getType() == PieceType.PAWN) {
                    int direction = piece.getColor() == PieceColor.WHITE ? 1 : -1;
                    if (Math.abs(fromCol - squareCol) == 1 && squareRow - fromRow == direction) {
                        return true;
                    }
                } else if (piece.getType() == PieceType.ROOK) {
                    if (fromRow == squareRow || fromCol == squareCol) {
                        if (isPathClear(board, from, square, true)) return true;
                    }
                } else if (piece.getType() == PieceType.KNIGHT) {
                    int rowDiff = Math.abs(squareRow - fromRow);
                    int colDiff = Math.abs(squareCol - fromCol);
                    if ((rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)) {
                        return true;
                    }
                } else if (piece.getType() == PieceType.BISHOP) {
                    if (Math.abs(squareRow - fromRow) == Math.abs(squareCol - fromCol)) {
                        if (isPathClear(board, from, square, false)) return true;
                    }
                } else if (piece.getType() == PieceType.QUEEN) {
                    boolean isStraight = fromRow == squareRow || fromCol == squareCol;
                    boolean isDiagonal = Math.abs(squareRow - fromRow) == Math.abs(squareCol - fromCol);
                    if (isStraight || isDiagonal) {
                        if (isPathClear(board, from, square, isStraight)) return true;
                    }
                } else if (piece.getType() == PieceType.KING) {
                    int rowDiff = Math.abs(squareRow - fromRow);
                    int colDiff = Math.abs(squareCol - fromCol);
                    if (rowDiff <= 1 && colDiff <= 1) return true;
                }
            }
        }
        return false;
    }
   
    private static final List<String> files = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h");
    private static final List<String> ranks = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8");
   
   
   
   
   
    public Game makeComputerMove(Long id) {
        Optional<Game> gameOpt = gameRepository.findById(id);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Jogo não encontrado: " + id);
        }
        Game game = gameOpt.get();
        if (game.isWhiteTurn()) {
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        List<String> possibleMoves = getAllPossibleMoves(id,board, PieceColor.BLACK);
        if (possibleMoves.isEmpty()) {
            throw new IllegalArgumentException("Nenhum movimento disponível para as pretas!");
        }
        // Escolhe um movimento aleatório (IA simples)
        Random random = new Random();
        String moveNotation = possibleMoves.get(random.nextInt(possibleMoves.size()));
        // Executa o movimento
        return makeMove(id, moveNotation);
    }
   
    private List<String> getAllPossibleMoves(Long gameId, Map<String, Piece> board, PieceColor color) {
        List<String> allMoves = new ArrayList<>();
        System.out.println("Gerando movimentos possíveis para " + color);
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            String from = entry.getKey();
            Piece piece = entry.getValue();
            if (piece == null || piece.getColor() != color) {
                continue;
            }
            System.out.println("Verificando movimentos para peça " + piece.getType() + " em " + from);
            List<String> moves = getPossibleMovesForPiece(gameId, board, from, piece);
            for (String to : moves) {
                String notation = generateMoveNotation(from, to, piece, board);
                if (notation != null && isValidNotation(from) && isValidNotation(to)) {
                    // Verifica se o movimento é legal (não deixa o rei em xeque)
                    Map<String, Piece> tempBoard = new HashMap<>(board);
                    tempBoard.put(to, tempBoard.get(from));
                    tempBoard.remove(from);
                    if (!isKingInCheck(tempBoard, color)) {
                        allMoves.add(notation);
                        System.out.println("Movimento válido adicionado: " + notation);
                    } else {
                        System.out.println("Movimento descartado (deixa rei em xeque): " + notation);
                    }
                } else {
                    System.out.println("Movimento inválido descartado: " + from + to);
                }
            }
        }
        System.out.println("Total de movimentos possíveis: " + allMoves.size());
        return allMoves;
    }
   
    private List<String> getPossibleMovesForPiece(Long gameId, Map<String, Piece> board, String from, Piece piece) {
        List<String> moves = new ArrayList<>();
        int fromCol = from.charAt(0) - 'a'; // Índice da coluna (0 = 'a', 7 = 'h')
        int fromRow = Character.getNumericValue(from.charAt(1)); // Fileira (1 a 8)
        PieceColor color = piece.getColor();
        int direction = color == PieceColor.WHITE ? 1 : -1;
        switch (piece.getType()) {
            case PAWN:
                // Movimento de uma casa
                String oneStep = String.valueOf((char) ('a' + fromCol)) + (fromRow + direction);
                if (!board.containsKey(oneStep) && fromRow + direction <= 8 && fromRow + direction >= 1) {
                    moves.add(oneStep);
                }
                // Movimento de duas casas
                if ((color == PieceColor.BLACK && fromRow == 7) || (color == PieceColor.WHITE && fromRow == 2)) {
                    String twoStep = String.valueOf((char) ('a' + fromCol)) + (fromRow + 2 * direction);
                    String intermediate = String.valueOf((char) ('a' + fromCol)) + (fromRow + direction);
                    if (!board.containsKey(intermediate) && !board.containsKey(twoStep)) {
                        moves.add(twoStep);
                    }
                }
                // Capturas
                for (int colOffset : new int[]{-1, 1}) {
                    int newCol = fromCol + colOffset;
                    if (newCol >= 0 && newCol < 8) { // Verifica limites de coluna
                        String capture = String.valueOf((char) ('a' + newCol)) + (fromRow + direction);
                        if (board.containsKey(capture) && board.get(capture).getColor() != color) {
                            moves.add(capture);
                        }
                    }
                }
                // En passant (mantido como está, mas verifique se está correto)
                String lastMove = getLastMove(gameId);
                if (lastMove != null && lastMove.length() >= 4) {
                    String lastFrom = lastMove.substring(0, 2);
                    String lastTo = lastMove.substring(2, 4);
                    int lastFromRow = Character.getNumericValue(lastFrom.charAt(1));
                    int lastToRow = Character.getNumericValue(lastTo.charAt(1));
                    char lastToCol = lastTo.charAt(0);
                    if (Math.abs(lastFromRow - lastToRow) == 2 && board.containsKey(lastTo) &&
                        board.get(lastTo).getType() == PieceType.PAWN && board.get(lastTo).getColor() != color) {
                        int enPassantRow = color == PieceColor.BLACK ? 3 : 6;
                        if (fromRow == (color == PieceColor.BLACK ? 4 : 5)) {
                            for (int colOffset : new int[]{-1, 1}) {
                                int newCol = fromCol + colOffset;
                                if (newCol >= 0 && newCol < 8 && lastTo.equals(String.valueOf((char) ('a' + newCol)) + fromRow)) {
                                    String enPassant = String.valueOf((char) ('a' + newCol)) + enPassantRow;
                                    moves.add(enPassant);
                                }
                            }
                        }
                    }
                }
                break;
            case KNIGHT:
                int[][] knightMoves = {{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2}, {1, -2}, {1, 2}, {2, -1}, {2, 1}};
                for (int[] move : knightMoves) {
                    int newRow = fromRow + move[0];
                    int newCol = fromCol + move[1];
                    if (newRow >= 1 && newRow <= 8 && newCol >= 0 && newCol < 8) { // Limite de coluna corrigido
                        String pos = String.valueOf((char) ('a' + newCol)) + newRow;
                        if (!board.containsKey(pos) || board.get(pos).getColor() != color) {
                            moves.add(pos);
                        }
                    }
                }
                break;
            case KING:
                for (int r = -1; r <= 1; r++) {
                    for (int c = -1; c <= 1; c++) {
                        if (r == 0 && c == 0) continue;
                        int newRow = fromRow + r;
                        int newCol = fromCol + c;
                        if (newRow >= 1 && newRow <= 8 && newCol >= 0 && newCol < 8) { // Limite de coluna corrigido
                            String pos = String.valueOf((char) ('a' + newCol)) + newRow;
                            if (!board.containsKey(pos) || board.get(pos).getColor() != color) {
                                moves.add(pos);
                            }
                        }
                    }
                }
                break;
            case ROOK:
            case BISHOP:
            case QUEEN:
                int[][] directions = piece.getType() == PieceType.ROOK ? new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}} :
                                    piece.getType() == PieceType.BISHOP ? new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}} :
                                    new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};
                for (int[] dir : directions) {
                    int newRow = fromRow;
                    int newCol = fromCol;
                    while (true) {
                        newRow += dir[0];
                        newCol += dir[1];
                        if (newRow < 1 || newRow > 8 || newCol < 0 || newCol >= 8) break; // Corrigido: newCol >= 8
                        String pos = String.valueOf((char) ('a' + newCol)) + newRow;
                        if (!board.containsKey(pos)) {
                            moves.add(pos);
                        } else {
                            if (board.get(pos).getColor() != color) moves.add(pos);
                            break;
                        }
                    }
                }
                break;
        }
        // Filtra movimentos que não colocam o rei em xeque
        List<String> legalMoves = new ArrayList<>();
        for (String to : moves) {
            Map<String, Piece> tempBoard = new HashMap<>(board);
            tempBoard.put(to, tempBoard.remove(from));
            if (!isKingInCheck(tempBoard, color)) {
                legalMoves.add(to);
            }
        }
        return legalMoves;
    }
   
    private String generateMoveNotation(String from, String to, Piece piece, Map<String, Piece> board) {
        if (!isValidNotation(from) || !isValidNotation(to)) {
            return null; // Retorna null para movimentos inválidos
        }
        if (piece.getType() == PieceType.PAWN && from.charAt(0) != to.charAt(0) && !board.containsKey(to)) {
            return from.charAt(0) + "x" + to; // En passant ou captura
        }
        return from + to; // Movimento normal
    }
    private String getLastMove(Long gameId) {
        // Método auxiliar para obter o último movimento do jogo
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        return gameOpt.map(Game::getLastMove).orElse(null);
    }
   
   
    public Game makeCaptureBasedMove(Long id) {
        Optional<Game> gameOpt = gameRepository.findById(id);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Jogo não encontrado: " + id);
        }
        Game game = gameOpt.get();
        if (game.isWhiteTurn()) {
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        boolean isInCheck = isKingInCheck(board, PieceColor.BLACK);
        List<String> allMoves = getAllPossibleMoves(id, board, PieceColor.BLACK);
        // Verifica xeque-mate ou afogamento
        if (allMoves.isEmpty()) {
            if (isInCheck) {
                game.setStatus(GameStatus.WHITE_WINS); // Xeque-mate
            } else {
                game.setStatus(GameStatus.DRAW); // Afogamento
            }
            gameRepository.save(game);
            return game;
        }
        String bestMove = null;
        int highestCaptureValue = -1;
        List<String> validMoves = new ArrayList<>();
        // Filtra jogadas válidas e prioriza capturas
        for (String move : allMoves) {
            // Simula a jogada
            Map<String, Piece> tempBoard = new HashMap<>(board);
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            Piece piece = tempBoard.get(from);
            tempBoard.put(to, piece);
            if (board.containsKey(to) && board.get(to).getColor() == PieceColor.WHITE) {
                tempBoard.remove(to); // Remove peça capturada
            }
            tempBoard.remove(from);
            // Verifica se a jogada é válida (não deixa o rei em xeque)
            if (!isKingInCheck(tempBoard, PieceColor.BLACK)) {
                validMoves.add(move);
                // Avalia captura
                Piece target = board.get(to);
                if (target != null && target.getColor() == PieceColor.WHITE) {
                    int value = getPieceValue(target);
                    if (value > highestCaptureValue || (value == highestCaptureValue && new Random().nextBoolean())) {
                        highestCaptureValue = value;
                        bestMove = move;
                    }
                }
            }
        }
        // Se está em xeque, apenas movimentos que resolvem o xeque são válidos
        if (isInCheck && !validMoves.isEmpty()) {
            List<String> checkResolvingMoves = new ArrayList<>();
            for (String move : validMoves) {
                Map<String, Piece> tempBoard = new HashMap<>(board);
                String from = move.substring(0, 2);
                String to = move.substring(2, 4);
                Piece piece = tempBoard.get(from);
                tempBoard.put(to, piece);
                if (board.containsKey(to) && board.get(to).getColor() == PieceColor.WHITE) {
                    tempBoard.remove(to);
                }
                tempBoard.remove(from);
                if (!isKingInCheck(tempBoard, PieceColor.BLACK)) {
                    checkResolvingMoves.add(move);
                }
            }
            validMoves = checkResolvingMoves;
        }
        // Escolhe o melhor movimento
        if (bestMove == null && !validMoves.isEmpty()) {
            Random rand = new Random();
            System.out.printf("Melhores Jogadas",validMoves);
            bestMove = validMoves.get(rand.nextInt(validMoves.size()));
        }
        if (bestMove == null) {
            if (isInCheck) {
                game.setStatus(GameStatus.WHITE_WINS); // Xeque-mate
            } else {
                game.setStatus(GameStatus.DRAW); // Afogamento
            }
            gameRepository.save(game);
            return game;
        }
        return makeMove(id, bestMove);
    }
       
   
    private int getPieceValue(Piece piece) {
        return switch (piece.getType()) {
            case PAWN -> 1;
            case KNIGHT, BISHOP -> 3;
            case ROOK -> 5;
            case QUEEN -> 9;
            case KING -> 1000;
        };
    }
    public Game makeHardAIMove(Long gameId) {
        System.out.println("Iniciando makeHardAIMove para gameId: " + gameId);
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            System.out.println("Jogo não encontrado: " + gameId);
            throw new IllegalArgumentException("Jogo não encontrado: " + gameId);
        }
        Game game = gameOpt.get();
        if (game.isWhiteTurn()) {
            System.out.println("Não é a vez das pretas!");
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        boolean isInCheck = isKingInCheck(board, PieceColor.BLACK);
        List<String> moves = getAllPossibleMoves(gameId, board, PieceColor.BLACK);
        if (moves.isEmpty()) {
            System.out.println("Nenhum movimento disponível para as pretas!");
            if (isInCheck) {
                game.setStatus(GameStatus.WHITE_WINS);
            } else {
                game.setStatus(GameStatus.DRAW);
            }
            return gameRepository.save(game);
        }
        // Obter sequência atual
       
        List<String> currentSequence = new ArrayList<>();
        for (Move m : game.getMoves()) {
            currentSequence.add(m.getFrom() + m.getTo());
        }
        // Filtrar movimentos que levam a aberturas ruins
        moves = moves.stream()
            .filter(move -> !isBadOpening(currentSequence, move))
            .toList();
        System.out.println("Sequência ruim: " + currentSequence + " " + moves + " " + "out" + game.getMoves().size());
        if (moves.isEmpty()) {
         System.out.println("Sequência ruim: " + currentSequence + " " + moves + " " + "int" + game.getMoves().size());
            moves = getAllPossibleMoves(gameId, board, PieceColor.BLACK); // Fallback
        }
        // Usar opening book nos primeiros 5 movimentos
        if(game.getMoves().size() < 10 && game.getMoves().size() > 4 && game.getLastMove() != null) {
        List<BadOpenig> badOpening = new ArrayList<BadOpenig>();
        badOpening = badOpeningRepository.findAll();
        int cont = 0;
        for (BadOpenig bad : badOpening) {
         for(String mov : bad.getMoveSequence()) {
         if(mov.contains(game.getLastMove())){
         cont ++;
         }
         }
         if(cont > 5) {
         if(!game.getBoardState().contains("b8a6")) {
         return makeMove(gameId,"b8a6");
         }else if(!game.getBoardState().contains("g8h6")) {
         return makeMove(gameId,"g8h6");
         }
         }
         }
        }
        if (game.getMoves().size() < 10 && game.getLastMove() != null && openingBook.containsKey(game.getLastMove())) {
            List<String> responses = openingBook.get(game.getLastMove());
            Random rand = new Random();
            String selected = responses.get(rand.nextInt(responses.size()));
            if (moves.contains(selected)) {
                System.out.println("Resposta do book: " + selected);
                return makeMove(gameId, selected);
            }
        }
        // Prosseguir com minimax e avaliação de capturas
        String bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int depth = 3;
        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            // Avaliar captura
            int captureScore = evaluateCapture(board, from, to);
            if (captureScore < 0) continue;
            Game clonedGame = cloneGame(game);
            applyMove(clonedGame, move, board);
            int score = minimax(clonedGame, depth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, false) + captureScore;
            undoMove(clonedGame, move, board, board.get(from), from, board.get(to), to); // Use undoMove
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }
        if (bestMove == null) {
            bestMove = moves.get(new Random().nextInt(moves.size()));
        }
        return makeMove(gameId, bestMove);
    }
    // Avaliação de captura
    private int evaluateCapture(Map<String, Piece> board, String from, String to) {
        Piece movingPiece = board.get(from);
        Piece target = board.get(to);
        if (target == null) return 0;
        int captureValue = target.getValuePiece() - movingPiece.getValuePiece();
        if (captureValue < 0) return captureValue;
        PieceColor opponentColor = movingPiece.getColor() == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        if (isSquareUnderAttack(board, to, opponentColor)) {
            captureValue -= movingPiece.getValuePiece(); // Penaliza perda potencial
        }
        return captureValue;
    }
    // ... (rest of your existing code for minimax, evaluateBoard, etc.)
   /* private List<String> getAllPossibleMoves(Long gameId, Map<String, Piece> board, PieceColor color) {
        List<String> allMoves = new ArrayList<>();
        // ... (código existente)
        Collections.shuffle(allMoves); // Randomiza para variedade
        return allMoves;
    }*/
    // Penalize early f/g/h pawn moves in evaluateBoard if <5 moves and not necessary
    public int evaluateBoard(Game game, PieceColor color) {
        int score = 0;
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        PieceColor opponentColor = (color == PieceColor.BLACK) ? PieceColor.WHITE : PieceColor.BLACK;
        // Material
        for (Piece piece : board.values()) {
            if (piece != null) {
                score += (piece.getColor() == color) ? piece.getValuePiece() : -piece.getValuePiece();
            }
        }
        // Controle centro
        String[] centralSquares = {"d4", "d5", "e4", "e5"};
        for (String square : centralSquares) {
            Piece piece = board.get(square);
            if (piece != null) {
                score += (piece.getColor() == color) ? 10 : -10;
            }
        }
        // Segurança rei
        String kingPos = findKingPosition(board, color);
        if (kingPos != null) {
            score += evaluateKingSafety(board, kingPos, color);
        }
        // Penalize early f/g/h pawn moves for black if game.moves <5
        if (game.getMoves().size() < 5 && color == PieceColor.BLACK) {
            if (!board.containsKey("f7") || !board.containsKey("g7") || !board.containsKey("h7")) {
                score -= 50; // Penalize weakening king's side
            }
        }
        return score;
    }
   
    public Game cloneGame(Game originalGame) {
        Game clone = new Game();
        clone.setId(originalGame.getId());
        clone.setPlayerWhite(originalGame.getPlayerWhite());
        clone.setPlayerBlack(originalGame.getPlayerBlack());
        clone.setStatus(originalGame.getStatus());
        clone.setWhiteTurn(originalGame.isWhiteTurn());
        clone.setLastMove(originalGame.getLastMove());
        clone.setInCheck(originalGame.isInCheck());
        clone.setCheckmate(originalGame.isCheckmate());
       
        // Copiar o tabuleiro (deep copy)
        Map<String, Piece> clonedBoard = new HashMap<>();
        Map<String, Piece> board = new HashMap<>();
        board = desearilizeState(originalGame.getBoardState());
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            clonedBoard.put(entry.getKey(), entry.getValue() != null ? (Piece) entry.getValue() : null);
        }
        clone.setBoardState(serializeBoardState(clonedBoard));
        // Clonar flags (se existirem)
        clone.setWhiteKingMoved(originalGame.isWhiteKingMoved());
        clone.setBlackKingMoved(originalGame.isBlackKingMoved());
        clone.setWhiteRookA1Moved(originalGame.isWhiteRookA1Moved());
        clone.setWhiteRookH1Moved(originalGame.isWhiteRookH1Moved());
        clone.setBlackRookA8Moved(originalGame.isBlackRookA8Moved());
        clone.setBlackRookH8Moved(originalGame.isBlackRookH8Moved());
        return clone;
    }

    private void validateChatGPTHardMove(Game game, String moveNotation) {
        Game validationGame = cloneGame(game);

        try {
            validateMoveOnSnapshot(validationGame, moveNotation);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(
                "A IA sugeriu um lance inválido: " + moveNotation + ". " + e.getMessage(),
                e
            );
        }
    }

    private void validateMoveOnSnapshot(Game game, String moveNotation) {
        Move move = parseNotation(moveNotation, game);
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        Piece piece = board.get(move.getFrom());

        if (piece == null) {
            throw new IllegalArgumentException("Nenhuma peça na posição de origem: " + move.getFrom());
        }
        if (piece.getColor() != (game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK)) {
            throw new IllegalArgumentException("Não é o turno dessa cor");
        }

        PieceColor playerColor = game.isWhiteTurn() ? PieceColor.WHITE : PieceColor.BLACK;
        PieceColor opponentColor = playerColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        boolean kingInCheckBefore = isKingInCheck(board, playerColor);

        int fromRow = Character.getNumericValue(move.getFrom().charAt(1));
        int toRow = Character.getNumericValue(move.getTo().charAt(1));
        char fromCol = move.getFrom().charAt(0);
        char toCol = move.getTo().charAt(0);

        switch (piece.getType()) {
            case PAWN:
                handlePawnMove(game, board, piece, move.getFrom(), move.getTo(), fromRow, toRow, fromCol, toCol,
                        playerColor, moveNotation);
                break;
            case KING:
                handleKingMove(game, board, piece, move.getFrom(), move.getTo(), fromRow, toRow, fromCol, toCol,
                        playerColor, opponentColor);
                break;
            case ROOK:
                validateRookMove(board, fromRow, toRow, fromCol, toCol, piece);
                markRookMoved(game, move.getFrom(), playerColor);
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case KNIGHT:
                validateKnightMove(fromRow, toRow, fromCol, toCol, piece, board, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case BISHOP:
                validateBishopMove(board, fromRow, toRow, fromCol, toCol, piece, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
            case QUEEN:
                validateQueenMove(board, fromRow, toRow, fromCol, toCol, piece, move.getTo());
                executeMove(board, move.getFrom(), move.getTo(), piece, playerColor);
                break;
        }

        if (move.isCastling()) {
            if (move.getTo().equals("g1")) {
                board.put("f1", board.remove("h1"));
            } else if (move.getTo().equals("c1")) {
                board.put("d1", board.remove("a1"));
            } else if (move.getTo().equals("g8")) {
                board.put("f8", board.remove("h8"));
            } else if (move.getTo().equals("c8")) {
                board.put("d8", board.remove("a8"));
            }
        }

        boolean kingInCheckAfter = isKingInCheck(board, playerColor);
        if (!kingInCheckBefore && kingInCheckAfter) {
            throw new IllegalArgumentException("Movimento inválido: coloca o rei em xeque");
        }
        if (kingInCheckBefore && kingInCheckAfter) {
            throw new IllegalArgumentException("Movimento inválido: o rei permanece em xeque");
        }
    }
   
    public void applyMove(Game game, String moveNotation, Map<String, Piece> board) {
        Move move = parseNotation(moveNotation, game);
        String from = move.getFrom();
        String to = move.getTo();
        Piece originalPiece = board.get(from);
        Piece capturedPiece = board.get(to);
        if (originalPiece == null) {
            throw new IllegalArgumentException("Nenhuma peça na posição de origem: " + from);
        }
        board.put(to, originalPiece);
        board.remove(from);
        game.setBoardState(serializeBoardState(board));
        game.setWhiteTurn(!game.isWhiteTurn());
        game.setLastMove(moveNotation);
        if (originalPiece.getType() == PieceType.KING) {
            if (originalPiece.getColor() == PieceColor.WHITE) {
                game.setWhiteKingMoved(true);
            } else {
                game.setBlackKingMoved(true);
            }
        } else if (originalPiece.getType() == PieceType.ROOK) {
            markRookMoved(game, from, originalPiece.getColor());
        }
    }
   
    public int minimax(Game game, int depth, int alpha, int beta, boolean maximizingPlayer) {
        String positionKey = game.getBoardState() + game.isWhiteTurn();
        if (transpositionTable.containsKey(positionKey) && depth <= 0) {
            return transpositionTable.get(positionKey);
        }
        if (depth == 0 || game.getStatus() != GameStatus.IN_PROGRESS) {
            int score = evaluateBoard(game, PieceColor.BLACK);
            transpositionTable.put(positionKey, score);
            return score;
        }
        PieceColor color = maximizingPlayer ? PieceColor.BLACK : PieceColor.WHITE;
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        List<String> moves = getAllPossibleMoves(game.getId(), board, color);
        moves.sort((move1, move2) -> {
            String to1 = move1.substring(2, 4);
            String to2 = move2.substring(2, 4);
            int score1 = board.containsKey(to1) ? board.get(to1).getValuePiece() : 0;
            int score2 = board.containsKey(to2) ? board.get(to2).getValuePiece() : 0;
            if (isKingInCheck(board, color)) {
                Map<String, Piece> tempBoard1 = new HashMap<>(board);
                Map<String, Piece> tempBoard2 = new HashMap<>(board);
                String from1 = move1.substring(0, 2);
                String from2 = move2.substring(0, 2);
                tempBoard1.put(to1, tempBoard1.get(from1));
                tempBoard1.remove(from1);
                tempBoard2.put(to2, tempBoard2.get(from2));
                tempBoard2.remove(from2);
                boolean resolvesCheck1 = !isKingInCheck(tempBoard1, color);
                boolean resolvesCheck2 = !isKingInCheck(tempBoard2, color);
                if (resolvesCheck1 && !resolvesCheck2) return -1;
                if (!resolvesCheck1 && resolvesCheck2) return 1;
            }
            return score2 - score1;
        });
        if (maximizingPlayer) {
            int maxEval = Integer.MIN_VALUE;
            for (String move : moves) {
                String from = move.substring(0, 2);
                String to = move.substring(2, 4);
                Piece originalPiece = board.get(from);
                Piece capturedPiece = board.get(to);
                applyMove(game, move, board);
                int eval = minimax(game, depth - 1, alpha, beta, false);
                undoMove(game, move, board, originalPiece, from, capturedPiece, to);
               
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);
                if (beta <= alpha) break;
            }
            transpositionTable.put(positionKey, maxEval);
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (String move : moves) {
                String from = move.substring(0, 2);
                String to = move.substring(2, 4);
                Piece originalPiece = board.get(from);
                Piece capturedPiece = board.get(to);
                applyMove(game, move, board);
                int eval = minimax(game, depth - 1, alpha, beta, true);
                undoMove(game, move, board, originalPiece, from, capturedPiece, to);
               
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);
                if (beta <= alpha) break;
            }
            transpositionTable.put(positionKey, minEval);
            return minEval;
        }
    }
   
    private String findKingPosition(Map<String, Piece> board, PieceColor color) {
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            Piece piece = entry.getValue();
            if (piece != null && piece.getType() == PieceType.KING && piece.getColor() == color) {
                return entry.getKey();
            }
        }
        return null;
    }
   
    public void undoMove(Game game, String moveNotation, Map<String, Piece> board, Piece originalPiece, String from, Piece capturedPiece, String to) {
        Move move = parseNotation(moveNotation, game);
        board.put(from, originalPiece);
        if (capturedPiece != null) {
            board.put(to, capturedPiece);
        } else {
            board.remove(to);
        }
        game.setBoardState(serializeBoardState(board));
        game.setWhiteTurn(!game.isWhiteTurn());
        game.setLastMove(null); // Simplificação, ajustar conforme necessário
    }
   
    private int evaluateKingSafety(Map<String, Piece> board, String kingPos, PieceColor color) {
        int safetyScore = 0;
        int kingRow = Character.getNumericValue(kingPos.charAt(1));
        char kingCol = kingPos.charAt(0);
        // Penalizar rei no centro
        if (kingRow >= 3 && kingRow <= 6 && kingCol >= 'c' && kingCol <= 'f') {
            safetyScore -= 20;
        }
        // Bônus por peões protegendo o rei
        int direction = color == PieceColor.WHITE ? -1 : 1;
        for (int colOffset : new int[]{-1, 0, 1}) {
            char col = (char) (kingCol + colOffset);
            if (col >= 'a' && col <= 'h') {
                String pos = col + String.valueOf(kingRow + direction);
                Piece piece = board.get(pos);
                if (piece != null && piece.getType() == PieceType.PAWN && piece.getColor() == color) {
                    safetyScore += 15;
                }
            }
        }
        return safetyScore;
    }
   
    private boolean isBadOpening(List<String> currentSequence, String move) {
        List<BadOpenig> badOpenings = badOpeningRepository.findAll();
        for (BadOpenig bad : badOpenings) {
            List<String> badSeq = bad.getMoveSequence();
            if (currentSequence.size() >= badSeq.size() && currentSequence.subList(0, badSeq.size()).equals(badSeq)) {
             System.out.println("Sequência ruim: " + move + bad.getId());
                return true;
            }
        }
        return false;
    }
   
    // Novo método auxiliar para validar notação
    private boolean isValidNotation(String pos) {
        if (pos == null || pos.length() != 2) return false;
        String col = pos.substring(0, 1);
        String row = pos.substring(1);
        return files.contains(col) && ranks.contains(row);
    }
   
    // Endpoint reativo (combina captura e defesa)
    public Game makeReactiveMove(Long gameId) {
        System.out.println("Iniciando makeReactiveMove para gameId: " + gameId);
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            System.out.println("Jogo não encontrado: " + gameId);
            throw new IllegalArgumentException("Jogo não encontrado: " + gameId);
        }
        Game game = gameOpt.get();
        if (game.isWhiteTurn()) {
            System.out.println("Não é a vez das pretas!");
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        List<String> moves = getAllPossibleMoves(gameId, board, PieceColor.BLACK);
        boolean inCheck = isKingInCheck(board, PieceColor.BLACK);
        if (moves.isEmpty()) {
            System.out.println("Nenhum movimento disponível para as pretas!");
            if (inCheck) {
                game.setStatus(GameStatus.WHITE_WINS);
            } else {
                game.setStatus(GameStatus.DRAW);
            }
            return gameRepository.save(game);
        }

        if (inCheck) {
            System.out.println("Rei preto em xeque, forçando jogada defensiva...");
            String defensiveMove = selectDefensiveMove(gameId, board, moves);
            if (defensiveMove != null) {
                System.out.println("Defesa selecionada para xeque: " + defensiveMove);
                Game updatedGame = makeMove(gameId, defensiveMove);
                return gameRepository.save(updatedGame);
            }

            System.out.println("Nenhuma jogada para escapar do xeque, xeque-mate!");
            game.setStatus(GameStatus.WHITE_WINS);
            return gameRepository.save(game);
        }
        
        if(!game.isWhiteTurn() && !inCheck) {
        	Long index = 2L;
            GameDTO gameDTO = this.getGameDTO(gameId);
            boolean queenMustRetreat = isQueenThreatenedOnH4(gameDTO.getBoard());
            String queenRetreatMove = findForcedQueenRetreatMoveFromH4(gameId, gameDTO.getBoard());
            if (queenRetreatMove != null) {
            	checkmateService.saveIsApplyCheck(index);
            	System.err.println("Retirada forçada da dama em h4 selecionada: " + queenRetreatMove + " estagio 14");
            	Game updatedGame = makeMove(gameId, queenRetreatMove);
            	return gameRepository.save(updatedGame);
            }

            if (queenMustRetreat) {
            	checkmateService.saveIsApplyCheck(index);
            	System.err.println("Book de checkmate interrompido: dama preta em h4 ameaçada estagio 14");
            } else {
                String bookMove = filterReactiveMoveCandidate(
                        checkmateService.applyMoveCheckMate(index, gameDTO),
                        moves,
                        "checkmate-book");
                boolean typeCheck = makeDectecdTypeCheckMatePastor(gameId,bookMove);
                System.err.println("Preparação para checkmate simples selecionado próxima jogada: " + bookMove + " estagio 15");
                if (bookMove != null && typeCheck == true) {
                    System.err.println("Preparação para checkmate simples selecionado: " + bookMove + " estagio 15");
                    Game updatedGame = makeMove(gameId, bookMove);
                    return gameRepository.save(updatedGame);
                }
            }
        }

        if (!inCheck) {
            String queenSafeCaptureMove = findReactiveQueenSafeCaptureMove(board, moves);
            if (queenSafeCaptureMove != null) {
                System.err.println("Captura segura da dama preta selecionada no reactive-move: " + queenSafeCaptureMove);
                Game updatedGame = makeMove(gameId, queenSafeCaptureMove);
                return gameRepository.save(updatedGame);
            }

            String queenDefenseMove = findReactiveQueenDefenseMove(board, moves);
            if (queenDefenseMove != null) {
                System.err.println("Defesa geral da dama preta selecionada no reactive-move: " + queenDefenseMove);
                Game updatedGame = makeMove(gameId, queenDefenseMove);
                return gameRepository.save(updatedGame);
            }

            String rookDefenseMove = findReactiveRookDefenseMove(board, moves);
            if (rookDefenseMove != null) {
                System.err.println("Defesa de torre preta selecionada no reactive-move: " + rookDefenseMove);
                Game updatedGame = makeMove(gameId, rookDefenseMove);
                return gameRepository.save(updatedGame);
            }

            String profitableCaptureMove = findProfitableOrEqualBlackCaptureMove(board, moves);
            if (profitableCaptureMove != null) {
                System.err.println("Captura vantajosa/equivalente selecionada no reactive-move: " + profitableCaptureMove);
                Game updatedGame = makeMove(gameId, profitableCaptureMove);
                return gameRepository.save(updatedGame);
            }
        }

        
        String capture = filterReactiveMoveCandidate(makeListAttackblack(gameId), moves, "simple-capture");
        System.err.println("Defesa simples selecionada: " + capture + " Estagio 12");
        if(capture != null && !inCheck) {
        	
            System.err.println("Attack simples selecionado: " + capture + " estagio 13");
            Game updatedGame = makeMove(gameId, capture);
            return gameRepository.save(updatedGame);
        }
                        
        String attack = filterReactiveMoveCandidate(makeListCaptureCounterattack(), moves, "counterattack");
        System.err.println("Defesa simples selecionada: " + attack + " Estagio 07");
        if(attack != null && !inCheck) {
        	
            System.err.println("Defesa simples selecionada por fator de contraAttack: " + attack + " estagio 08");
            Game updatedGame = makeMove(gameId, attack);
            return gameRepository.save(updatedGame);
        }
        
        String blackDefense = filterReactiveMoveCandidate(makeListAttackWhite(gameId), moves, "black-defense");
        //System.err.println("Defesa simples selecionada: " + blackDefense + " Estagio 10");
        if(blackDefense != null && !inCheck) {
        	
            System.err.println("Defesa simples selecionada por peças black sob attack: " + blackDefense + " estagio 11");
            Game updatedGame = makeMove(gameId, blackDefense);
            return gameRepository.save(updatedGame);
        }
        
        String knightDefense = filterReactiveMoveCandidate(makeMoveBlackPiecesKnight(), moves, "knight-defense");
        System.err.println("Defesa simples selecionada por movimentação do cavalo: " + knightDefense + " Estagio 05");
        if(knightDefense != null && !inCheck) {
        	
            System.err.println("Defesa simples selecionada: " + knightDefense + " estagio 06");
            Game updatedGame = makeMove(gameId, knightDefense);
            return gameRepository.save(updatedGame);
        }
        
        String bestDefense = filterReactiveMoveCandidate(makeMoveBlackPieces(gameId), moves, "best-defense");
        System.err.println("Defesa simples selecionada pelo valor Min da peça: " + bestDefense + " Estagio 09");
        if(bestDefense != null && !inCheck) {
        	
            System.err.println("Defesa simples selecionada: " + bestDefense + " estagio 10");
            Game updatedGame = makeMove(gameId, bestDefense);
            return gameRepository.save(updatedGame);
        }
               
        // Fallback: usa hard-computer-move
        System.out.println("Usando hard-computer-move como fallback...");
       
        //Verificar se é a mesma tentativa de checkmate
        String newMove = filterReactiveMoveCandidate(makeNewMoveDetectCheckMate(gameId,board), moves, "checkmate-detect");
        if(newMove != null) {
         Game updateGame = makeMove(gameId,newMove);
         return updateGame;
        }
       
        // Tenta uma captura segura
        String captureMove = null;
        String captureNew = null;
        try {
            System.out.println("Tentando captura segura...");
            captureMove = selectCaptureMove(gameId, board, moves);
            System.out.println("Captura selecionada: " + captureMove);
            //String from = captureMove.substring(0, 2);
           /* String to = null;
            if(captureMove != null) {
            	to = captureMove.substring(2, 4);
            }
            captureNew = makeListCaptureCounterattack(to);
            if(captureNew != null && captureMove != null) {
                System.out.println("Captura selecionada: " + captureNew);
                Game updatedGame = makeMove(gameId, captureNew);
                return gameRepository.save(updatedGame);
            }else*/
            if(captureMove != null){
                System.out.println("Captura selecionada: " + captureMove);
                Game updatedGame = makeMove(gameId, captureMove);
                return gameRepository.save(updatedGame);
            }
        } catch (Exception e) {
            System.out.println("Nenhuma captura segura disponível: " + e.getMessage());
        }
        // Tenta uma jogada defensiva
        String defensiveMove = null;
        try {
            System.out.println("Tentando jogada defensiva...");
            defensiveMove = selectDefensiveMove(gameId, board, moves);
            if (defensiveMove != null) {
                System.out.println("Defesa selecionada: " + defensiveMove);
                Game updatedGame = makeMove(gameId, defensiveMove);
                return gameRepository.save(updatedGame);
            }
        } catch (Exception e) {
            System.out.println("Nenhuma jogada defensiva necessária: " + e.getMessage());
        }
        // Fallback: usa hard-computer-move
        System.out.println("Usando hard-computer-move como fallback...");
        Game hardGame = makeHardAIMove(gameId);
        System.out.println("Movimento hard realizado: " + hardGame.getLastMove());
        return gameRepository.save(hardGame);
    }
    
    @SuppressWarnings("unused")
	private boolean makeDectecdTypeCheckMatePastor(Long gameId, String notation) {
    	if (notation == null || notation.length() < 4) {
    		return false;
    	}
    	String to = notation.substring(2, 4);
    	GameDTO gameDTO = this.getGameDTO(gameId);
    	Long index = 2L;
    	CheckmatePattern patterns = checkmateService.getCheckmatePatterById(index);
    	if(patterns != null) {
    		if(patterns.isApply() == false) {
    			System.err.println("Movimento checkmate iniciado, próximo movmento estagio 00: " + notation);
    			return false;
    		}
    	}
        Map<String, Piece> board = gameDTO.getBoard();
        if (isQueenThreatenedOnH4(board)) {
        	checkmateService.saveIsApplyCheck(index);
        	return false;
        }
        if (shouldBlockQueenMoveToH4(notation, board)) {
        	checkmateService.saveIsApplyCheck(index);
        	return false;
        }
        
        boolean isContinue = true;
        Piece piecePawn = new Piece();
        Piece pieceQueen = new Piece();
        Piece pieceQueen2 = new Piece();
        Piece pieceBishop = new Piece();
        piecePawn = board.get("e2");
        pieceQueen = board.get("d8"); 
        pieceBishop = board.get("f8");
        System.err.println("Movimento checkmate iniciado, próximo movmento estagio 01: " + notation);
        
        if(piecePawn != null) {
        	if(piecePawn.getColor() == PieceColor.BLACK && piecePawn.getType() == PieceType.PAWN) {
        		isContinue = true;
        	}
        }else {
        	piecePawn = board.get("e5");
        	if(piecePawn != null) {
            	if(piecePawn.getColor() == PieceColor.BLACK && piecePawn.getType() == PieceType.PAWN) {
            		isContinue = true;
            	}
        	}
        }
        if(pieceQueen != null && isContinue == true) {
        	if(pieceQueen.getColor() == PieceColor.BLACK && pieceQueen.getType() == PieceType.QUEEN) {
        		isContinue = true;
        	}
        }else {
        	pieceQueen = board.get("h4"); 
        	 if(pieceQueen != null) {
             	if(pieceQueen.getColor() == PieceColor.BLACK && pieceQueen.getType() == PieceType.QUEEN) {
             		isContinue = true;
             	}else {
             		isContinue = false;
             		checkmateService.saveIsApplyCheck(index);
             		return false;
             	}
             }
        }
        if(pieceBishop != null && isContinue == true) {
        	if(pieceBishop.getColor() == PieceColor.BLACK && pieceBishop.getType() == PieceType.BISHOP) {
        		isContinue = true;
        	}
        }else {
        	pieceBishop = board.get("c5");
        	if(pieceBishop != null) {
            	if(pieceBishop.getColor() == PieceColor.BLACK && pieceBishop.getType() == PieceType.BISHOP) {
            		isContinue = true;
            	}else {
            		isContinue = false;
            		checkmateService.saveIsApplyCheck(index);
            		return false;
            	}
            }
        }
        if(isContinue == true) {
        	if(pieceQueen2 != null) {
            	if(pieceQueen2.getColor() == PieceColor.BLACK) {
            		checkmateService.saveIsApplyCheck(index);
            		return false;
            	}else {
            		System.err.println("Movimento checkmate iniciado, próximo movmento estagio 02: " + notation);
            		return true;
            	}	
        	}else {
        		System.err.println("Movimento checkmate iniciado, próximo movmento estagio 02: " + notation);
        		return true;
        	}

        }

   
        return false;
    }

    private boolean shouldBlockQueenMoveToH4(String notation, Map<String, Piece> board) {
    	if (board == null || notation == null || !notation.startsWith("d8h4")) {
    		return false;
    	}
    	return hasWhiteThreatAgainstQueenOnH4(board);
    }

    private String filterReactiveMoveCandidate(String moveNotation, List<String> legalMoves, String source) {
    	if (moveNotation == null || legalMoves == null) {
    		return moveNotation;
    	}

    	if (legalMoves.contains(moveNotation)) {
    		return moveNotation;
    	}

    	System.err.println("Descartando jogada pseudo-legal no reactive-move: " + moveNotation + " origem: " + source);
    	return null;
    }

    private String findReactiveQueenSafeCaptureMove(Map<String, Piece> board, List<String> moves) {
    	String queenSquare = findBlackQueenSquare(board);
    	if (queenSquare == null) {
    		return null;
    	}

    	String bestMove = null;
    	int bestScore = Integer.MIN_VALUE;
    	for (String moveNotation : moves) {
    		if (moveNotation == null || moveNotation.length() < 4) {
    			continue;
    		}

    		String from = moveNotation.substring(0, 2);
    		String to = moveNotation.substring(2, 4);
    		Piece movingPiece = board.get(from);
    		Piece targetPiece = board.get(to);
    		if (!queenSquare.equals(from) || movingPiece == null || movingPiece.getType() != PieceType.QUEEN
    				|| movingPiece.getColor() != PieceColor.BLACK || targetPiece == null
    				|| targetPiece.getColor() != PieceColor.WHITE) {
    			continue;
    		}

    		Map<String, Piece> tempBoard = deepCopyBoard(board);
    		tempBoard.put(to, tempBoard.remove(from));
    		if (isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
    			continue;
    		}

    		int exchangeScore = staticExchangeEval(board, from, to);
    		if (exchangeScore < 0) {
    			continue;
    		}

    		int score = targetPiece.getValuePiece() * 100 + exchangeScore;
    		if (score > bestScore) {
    			bestScore = score;
    			bestMove = moveNotation;
    		}
    	}

    	return bestMove;
    }

    private String findReactiveQueenDefenseMove(Map<String, Piece> board, List<String> moves) {
    	String queenSquare = findBlackQueenSquare(board);
    	if (queenSquare == null || !isSquareUnderAttack(board, queenSquare, PieceColor.WHITE)) {
    		return null;
    	}

    	String bestMove = null;
    	int bestScore = Integer.MIN_VALUE;
    	for (String moveNotation : moves) {
    		if (moveNotation == null || moveNotation.length() < 4) {
    			continue;
    		}

    		String from = moveNotation.substring(0, 2);
    		String to = moveNotation.substring(2, 4);
    		Piece movingPiece = board.get(from);
    		if (movingPiece == null || movingPiece.getColor() != PieceColor.BLACK) {
    			continue;
    		}

    		Map<String, Piece> tempBoard = deepCopyBoard(board);
    		tempBoard.put(to, tempBoard.remove(from));

    		String queenSquareAfterMove = queenSquare.equals(from) ? to : queenSquare;
    		Piece queenAfterMove = tempBoard.get(queenSquareAfterMove);
    		if (queenAfterMove == null || queenAfterMove.getColor() != PieceColor.BLACK
    				|| queenAfterMove.getType() != PieceType.QUEEN) {
    			continue;
    		}

    		if (isSquareUnderAttack(tempBoard, queenSquareAfterMove, PieceColor.WHITE)) {
    			continue;
    		}

    		int score = from.equals(queenSquare) ? 1000 : 500;
    		Piece capturedPiece = board.get(to);
    		if (capturedPiece != null && capturedPiece.getColor() == PieceColor.WHITE) {
    			score += capturedPiece.getValuePiece() * 20;
    		}

    		if (score > bestScore) {
    			bestScore = score;
    			bestMove = moveNotation;
    		}
    	}

    	return bestMove;
    }

    private String findReactiveRookDefenseMove(Map<String, Piece> board, List<String> moves) {
    	if (board == null || moves == null || moves.isEmpty()) {
    		return null;
    	}

    	String bestMove = null;
    	int bestScore = Integer.MIN_VALUE;
    	for (Map.Entry<String, Piece> entry : board.entrySet()) {
    		String rookSquare = entry.getKey();
    		Piece rook = entry.getValue();
    		if (rook == null || rook.getColor() != PieceColor.BLACK || rook.getType() != PieceType.ROOK
    				|| !isSquareUnderAttack(board, rookSquare, PieceColor.WHITE)) {
    			continue;
    		}

    		for (String moveNotation : moves) {
    			if (moveNotation == null || moveNotation.length() < 4 || !moveNotation.startsWith(rookSquare)) {
    				continue;
    			}

    			String from = moveNotation.substring(0, 2);
    			String to = moveNotation.substring(2, 4);
    			Piece movingPiece = board.get(from);
    			if (movingPiece == null || movingPiece.getColor() != PieceColor.BLACK
    					|| movingPiece.getType() != PieceType.ROOK) {
    				continue;
    			}

    			Map<String, Piece> tempBoard = deepCopyBoard(board);
    			tempBoard.put(to, tempBoard.remove(from));
    			if (isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
    				continue;
    			}

    			int score = 1000;
    			Piece capturedPiece = board.get(to);
    			if (capturedPiece != null && capturedPiece.getColor() == PieceColor.WHITE) {
    				score += capturedPiece.getValuePiece() * 20;
    			}

    			if (score > bestScore) {
    				bestScore = score;
    				bestMove = moveNotation;
    			}
    		}
    	}

    	return bestMove;
    }

    private String findProfitableOrEqualBlackCaptureMove(Map<String, Piece> board, List<String> legalMoves) {
        if (board == null || legalMoves == null || legalMoves.isEmpty()) {
            return null;
        }

        String bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        int bestCapturedValue = Integer.MIN_VALUE;
        int bestAttackerValue = Integer.MAX_VALUE;

        for (String moveNotation : legalMoves) {
            if (moveNotation == null || moveNotation.length() < 4) {
                continue;
            }

            String from = moveNotation.substring(0, 2);
            String to = moveNotation.substring(2, 4);
            Piece attacker = board.get(from);
            Piece target = board.get(to);
            if (attacker == null || attacker.getColor() != PieceColor.BLACK || target == null
                    || target.getColor() != PieceColor.WHITE) {
                continue;
            }

            Map<String, Piece> tempBoard = deepCopyBoard(board);
            tempBoard.put(to, tempBoard.remove(from));
            if (isKingInCheck(tempBoard, PieceColor.BLACK)) {
                continue;
            }

            boolean canBeRecaptured = isSquareUnderAttack(tempBoard, to, PieceColor.WHITE);
            int capturedValue = target.getValuePiece();
            int attackerValue = attacker.getValuePiece();
            int score = canBeRecaptured ? capturedValue - attackerValue : capturedValue;
            if (score < 0) {
                continue;
            }

            if (score > bestScore
                    || (score == bestScore && capturedValue > bestCapturedValue)
                    || (score == bestScore && capturedValue == bestCapturedValue && attackerValue < bestAttackerValue)) {
                bestScore = score;
                bestCapturedValue = capturedValue;
                bestAttackerValue = attackerValue;
                bestMove = moveNotation;
            }
        }

        return bestMove;
    }

    private String findForcedQueenRetreatMoveFromH4(Long gameId, Map<String, Piece> board) {
    	if (!isQueenThreatenedOnH4(board)) {
    		return null;
    	}

    	Piece queen = board.get("h4");
    	if (queen == null || queen.getColor() != PieceColor.BLACK || queen.getType() != PieceType.QUEEN) {
    		return null;
    	}

    	String fallbackRetreatMove = null;
    	List<String> legalMoves = getAllPossibleMoves(gameId, board, PieceColor.BLACK);
    	for (String moveNotation : legalMoves) {
    		if (moveNotation == null || !moveNotation.startsWith("h4")) {
    			continue;
    		}
    		if (fallbackRetreatMove == null) {
    			fallbackRetreatMove = moveNotation;
    		}

    		String to = moveNotation.substring(2, 4);
    		Map<String, Piece> tempBoard = deepCopyBoard(board);
    		tempBoard.put(to, tempBoard.remove("h4"));
    		if (!isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
    			return moveNotation;
    		}
    	}

    	return fallbackRetreatMove;
    }

    private boolean isQueenThreatenedOnH4(Map<String, Piece> board) {
    	if (board == null) {
    		return false;
    	}

    	Piece queenH4 = board.get("h4");
    	if (queenH4 == null || queenH4.getColor() != PieceColor.BLACK || queenH4.getType() != PieceType.QUEEN) {
    		return false;
    	}

    	return hasWhiteThreatAgainstQueenOnH4(board);
    }

    private boolean hasWhiteThreatAgainstQueenOnH4(Map<String, Piece> board) {
    	if (board == null) {
    		return false;
    	}

    	Piece knightF3 = board.get("f3");
    	if (knightF3 != null && knightF3.getColor() == PieceColor.WHITE && knightF3.getType() == PieceType.KNIGHT) {
    		return true;
    	}

    	Piece pawnG3 = board.get("g3");
    	return pawnG3 != null && pawnG3.getColor() == PieceColor.WHITE && pawnG3.getType() == PieceType.PAWN;
    }

    private String findBlackQueenSquare(Map<String, Piece> board) {
    	if (board == null) {
    		return null;
    	}

    	for (Map.Entry<String, Piece> entry : board.entrySet()) {
    		Piece piece = entry.getValue();
    		if (piece != null && piece.getColor() == PieceColor.BLACK && piece.getType() == PieceType.QUEEN) {
    			return entry.getKey();
    		}
    	}

    	return null;
    }
   
    private String makeNewMoveDetectCheckMate(Long gameId,Map<String,Piece> board) {
        System.out.println("Iniciando makeDefensiveMove para gameId: " + gameId);
        List<BadOpenig> badOpenig = badOpeningRepository.findAll();
        List<MovePiece> movesPiece = movePieceRepository.findAll();
        if(badOpenig.size() > 0 && movesPiece.size() > 2 && movesPiece.size() < 12) {
            if(this.makePlayCheckMate(movesPiece,badOpenig)) {
             String niceMove = "";
             List<String > movsPossible = this.getAllPossibleMoves(gameId, board, PieceColor.BLACK);
	             for(MovePiece mov : movesPiece ) {
		             for(String newMov : movsPossible) {
			             if(!newMov.equals(mov.getMov())) {
			            	 niceMove = newMov;
			             	return newMov;
			             }
		             }
	             }
               
            }
            return null;
        }
        return null;
    }
   
    private String selectDefensiveMove(Long gameId, Map<String, Piece> board, List<String> moves) {
        System.out.println("Selecionando jogada defensiva para gameId: " + gameId);
        String bestDefense = null;
        int bestDefenseScore = Integer.MIN_VALUE;
        boolean isInCheck = isKingInCheck(board, PieceColor.BLACK);
        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            if (!board.containsKey(from) || board.get(from).getColor() != PieceColor.BLACK) {
                System.out.println("Ignorando movimento inválido: " + move);
                continue;
            }
            Map<String, Piece> tempBoard = new HashMap<>(board);
            Piece capturedPiece = tempBoard.get(to);
            tempBoard.put(to, tempBoard.get(from));
            tempBoard.remove(from);
            int defenseScore = 0;
            if (isInCheck && !isKingInCheck(tempBoard, PieceColor.BLACK)) {
                defenseScore += 1000; // Prioridade máxima para resolver xeque
                System.out.println("Movimento resolve xeque: " + move);
            }
            if (capturedPiece != null && capturedPiece.getColor() == PieceColor.WHITE) {
                defenseScore += capturedPiece.getValuePiece() * 10;
            }
            List<String> threatenedSquares = new ArrayList<>();
            for (Map.Entry<String, Piece> entry : board.entrySet()) {
                if (entry.getValue().getColor() == PieceColor.BLACK && isSquareUnderAttack(board, entry.getKey(), PieceColor.WHITE)) {
                    threatenedSquares.add(entry.getKey());
                }
            }
            for (String threatened : threatenedSquares) {
                if (!isSquareUnderAttack(tempBoard, threatened, PieceColor.WHITE)) {
                    defenseScore += board.get(threatened).getValuePiece() * 2;
                }
            }
            if (defenseScore > bestDefenseScore) {
                bestDefenseScore = defenseScore;
                //bestDefense = makeListCaptureCounterattack(to);
                
                //if(bestDefense != null) {
                	//bestDefense = null;
                	bestDefense = move;
                	//bestDefense = makeListCaptureCounterattack(to);
                	System.err.println("Defesa candidata: " + move + ", score: " + defenseScore + " Estagio 01: ");
                //}
                
            }
        }
        if (bestDefense != null) {
            System.out.println("Defesa selecionada: " + bestDefense + ", score: " + bestDefenseScore);
        } else {
            System.out.println("Nenhuma defesa selecionada.");
        }
        return bestDefense;
    }
    
    
    private String newSelectDefensiveMove(Long gameId, Map<String, Piece> board, List<String> moves) {
        System.out.println("Selecionando jogada defensiva para gameId: " + gameId);
        String bestDefense = null;
        int bestDefenseScore = Integer.MIN_VALUE;
        boolean isInCheck = isKingInCheck(board, PieceColor.BLACK);
        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            if (!board.containsKey(from) || board.get(from).getColor() != PieceColor.BLACK) {
                System.out.println("Ignorando movimento inválido: " + move);
                continue;
            }
            Map<String, Piece> tempBoard = new HashMap<>(board);
            Piece capturedPiece = tempBoard.get(to);
            tempBoard.put(to, tempBoard.get(from));
            tempBoard.remove(from);
            

            int defenseScore = 0;
            if (isInCheck && !isKingInCheck(tempBoard, PieceColor.BLACK)) {
                defenseScore += 1000; // Prioridade máxima para resolver xeque
                System.out.println("Movimento resolve xeque: " + move);
            }
            if (capturedPiece != null && capturedPiece.getColor() == PieceColor.WHITE) {
                defenseScore += capturedPiece.getValuePiece() * 10;
            }
            List<String> threatenedSquares = new ArrayList<>();
            for (Map.Entry<String, Piece> entry : board.entrySet()) {
                if (entry.getValue().getColor() == PieceColor.BLACK && isSquareUnderAttack(board, entry.getKey(), PieceColor.WHITE)) {
                    threatenedSquares.add(entry.getKey());
                }
            }
            for (String threatened : threatenedSquares) {
                if (!isSquareUnderAttack(tempBoard, threatened, PieceColor.WHITE)) {
                    defenseScore += board.get(threatened).getValuePiece() * 2;
                }
            }
            if (defenseScore > bestDefenseScore) {
                bestDefenseScore = defenseScore;
                bestDefense = move;
                System.out.println("Defesa candidata: " + move + ", score: " + defenseScore);
            }
        }
        if (bestDefense != null) {
            System.out.println("Defesa selecionada: " + bestDefense + ", score: " + bestDefenseScore);
        } else {
            System.out.println("Nenhuma defesa selecionada.");
        }
        return bestDefense;
    }
    
    
    
   
    private String newSelectCaptureMove(Long gameId, Map<String, Piece> board, List<String> moves) {
        System.out.println("Selecionando captura segura para gameId: " + gameId);
        String bestCapture = null;
        int bestCaptureValue = -1;
        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            if (!board.containsKey(from) || board.get(from).getColor() != PieceColor.BLACK) {
                System.out.println("Ignorando movimento inválido: " + move);
                continue;
            }
            if (board.containsKey(to) && board.get(to).getColor() == PieceColor.WHITE) {
                int captureValue = board.get(to).getValuePiece();
                Map<String, Piece> tempBoard = new HashMap<>(board);
                tempBoard.put(to, tempBoard.get(from));
                tempBoard.remove(from);
                if (!isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
                    if (captureValue > bestCaptureValue) {
                        bestCaptureValue = captureValue;
                        bestCapture = move;
                        System.out.println("Captura candidata: " + move + ", valor: " + captureValue);
                    }
                }
            }
        }
        return bestCapture;
    }
   
    // Função completada: Prever se uma captura levará a perda de uma peça de maior valor
    private String makeListCaptureCounterattack() {
        String bestMove = null;
        int bigValue = 1100;  // Valor alto inicial para comparação (ex.: rei = 1000)

        Move mov = null;
        MovePiece blackPieceTo = null;
        List<String> listMove = new ArrayList<String>();
        int valuePiece = 0;
        
        // Passo 1: Obter peças brancas ativas
        List<MovePiece> whitePieces = makeListWhitePieces();
        if (whitePieces.isEmpty()) {
            return null;  // Sem peças brancas, nada a capturar
        }
        
        System.err.println("Total peças brancas: " + whitePieces.size() + " Estágio 1");
            
        // Passo 2: Obter possíveis capturas pretas nessa posição
        List<MovePiece> captures = makeListCapture();  // Ajuste para focar em whiteTo se necessário
        
        System.err.println("Total peças pretas com possibilidades de capturas : " + captures.size() + " Estágio 2 ");
        
        if (captures.isEmpty()) {
            System.err.println("Não têm nenhuma opção de captura");
            return null;
        }
            
        for (MovePiece blackCap : captures) {
            String blackToCapture = (blackCap.getToPiece() != null) ? blackCap.getToPiece() : blackCap.getFromPiece();
           
            // Passo 3: Verificar se há counterattack na posição após captura
            //boolean contraAttack = makeListCounterAttack(captures, blackToCapture, to);
            
            for(String blackPossibleMov : blackCap.getMoveAttack()) {
            	blackPieceTo = movePieceRepository.findValuePieceFromPieceNew(blackPossibleMov, true);
            	
                // Passo 3: Verificar se há counterattack na posição após captura
                boolean contraAttack = makeListCounterAttack(blackPossibleMov);
       	
            	if(blackPieceTo != null) {
            		valuePiece = blackPieceTo.getValuePiece();
                    if (blackCap.getValuePiece() <= valuePiece && contraAttack == true) {
                         bestMove = blackToCapture + blackPossibleMov;  // Sugere a captura se valer a pena
                        System.err.println("Vale capturar a peça da casa: " + blackPieceTo.getTypePiece() + " Estágio 1 " + " e perder a peça: " + blackCap.getTypePiece() + " na casa: " + blackToCapture);
                    } else if(contraAttack == false) {
                    	if(blackCap.getValuePiece() < valuePiece || valuePiece == 0) {
                    		listMove.add(blackToCapture + blackPossibleMov);
                        	mov = new Move();
                        	valuePiece = blackCap.getValuePiece();
                    	}
                    	System.err.println("Não tem contraAttack: Estágio 1 "  + " e perder a peça: " + blackCap.getTypePiece() + " na casa: " + blackToCapture + " HaveContraAttack: " + contraAttack);
                    }                    		
                }
            }
        }
        if(bestMove == null && !listMove.isEmpty()) {
    		bestMove = listMove.getFirst();
    	}
        return bestMove;
    }
    
 // Substitui predictLossAfterCapture por SEE real
    private int staticExchangeEval(Map<String, Piece> board, String from, String to) {
        Piece attacker = board.get(from);
        Piece target = board.get(to);
        if (attacker == null || target == null) return Integer.MIN_VALUE/2;

        // Valor inicial: capturado - atacante
        int balance = target.getValuePiece() - attacker.getValuePiece();

        // Simula captura
        Map<String, Piece> tmp = deepCopyBoard(board);
        tmp.put(to, clonePiece(attacker));
        tmp.remove(from);

        // Lista atacantes do oponente
        PieceColor opponent = attacker.getColor() == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        List<Piece> opponentAttackers = getAttackersOnSquare(tmp, to, opponent);

        if (!opponentAttackers.isEmpty()) {
            // O recapturador mais barato ataca primeiro
            opponentAttackers.sort(Comparator.comparingInt(Piece::getValuePiece));
            Piece recaptor = opponentAttackers.get(0);
            balance -= attacker.getValuePiece(); // perda potencial do atacante
        }

        return balance;
    }

    private String selectCaptureMove(Long gameId, Map<String, Piece> board, List<String> moves) {
        String bestCapture = null;
        int bestScore = Integer.MIN_VALUE;

        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);

            if (!board.containsKey(from)) continue;
            Piece moving = board.get(from);
            Piece target = board.get(to);

            if (moving == null || target == null || target.getColor() != PieceColor.WHITE) continue;

            int score = staticExchangeEval(board, from, to);

            // Pequeno bônus se dá cheque
            Map<String, Piece> tmp = deepCopyBoard(board);
            tmp.put(to, clonePiece(moving));
            tmp.remove(from);
            if (isKingInCheck(tmp, PieceColor.WHITE)) score += 20;

            if (score > bestScore) {
                bestScore = score;
                bestCapture = move;
            }
        }

        return bestCapture;
    }

    // Helpers (já sugeri antes)
    private Map<String, Piece> deepCopyBoard(Map<String, Piece> board) {
        Map<String, Piece> copy = new HashMap<>();
        for (Map.Entry<String, Piece> e : board.entrySet()) {
            copy.put(e.getKey(), e.getValue() != null ? clonePiece(e.getValue()) : null);
        }
        return copy;
    }
    private Piece clonePiece(Piece p) {
        return new Piece(p.getType(), p.getColor(), p.getValuePiece(), p.getCodigo());
    }
    private List<Piece> getAttackersOnSquare(Map<String, Piece> board, String square, PieceColor color) {
        List<Piece> attackers = new ArrayList<>();
        for (Map.Entry<String, Piece> e : board.entrySet()) {
            Piece p = e.getValue();
            if (p == null || p.getColor() != color) continue;
            if (isMoveLegal(board, e.getKey(), square, color)) {
                attackers.add(p);
            }
        }
        return attackers;
    }


    // Novo método: Simular perda após captura
    /*private boolean predictLossAfterCapture(Map<String, Piece> board, String from, String to, PieceColor playerColor) {
        Piece movingPiece = board.get(from);
        Piece target = board.get(to);
        
        if (target == null) {
            return false;  // Não é captura
        }
        
        // Simular a captura
        Map<String, Piece> tempBoard = new HashMap<>(board);
        tempBoard.put(to, movingPiece);
        tempBoard.remove(from);
        
        // Verificar se a nova posição (to) está sob ataque do oponente
        PieceColor opponentColor = playerColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;
        if (isSquareUnderAttack(tempBoard, to, opponentColor)) {
            // Verificar se o oponente pode capturar a peça de maior valor
            List<MovePiece> opponentAttacks = movePieceRepository.findPiecesAttackingPosition(opponentColor.toString(), to, true);
            for (MovePiece attack : opponentAttacks) {
                if (attack.getValuePiece() < movingPiece.getValuePiece()) {  // Oponente usa peça menor para capturar maior
                    return true;  // Previsão de perda
                }
            }
        }
        
        return false;  // Captura segura
    }*/

    // Integrar na selectCaptureMove para usar a previsão
   /* private String selectCaptureMove(Long gameId, Map<String, Piece> board, List<String> moves) {
        System.out.println("Selecionando captura segura para gameId: " + gameId);
        String bestCapture = null;
        int bestCaptureValue = -1;
        
        for (String move : moves) {
            String from = move.substring(0, 2);
            String to = move.substring(2, 4);
            if (!board.containsKey(from) || board.get(from).getColor() != PieceColor.BLACK) {
                System.out.println("Ignorando movimento inválido: " + move);
                continue;
            }
            if (board.containsKey(to) && board.get(to).getColor() == PieceColor.WHITE) {
                int captureValue = board.get(to).getValuePiece();
                // Prever se a captura leva a perda
                if (predictLossAfterCapture(board, from, to, PieceColor.BLACK)) {
                    System.out.println("Captura arriscada (previsão de perda): " + move + ", pulando...");
                    continue;  // Evita capturas que levam a perda de peça maior
                }
                
                Map<String, Piece> tempBoard = new HashMap<>(board);
                tempBoard.put(to, tempBoard.get(from));
                tempBoard.remove(from);
                bestCapture = makeListCaptureCounterattack();
                /*if (!isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
                    if (captureValue > bestCaptureValue) {
                        bestCaptureValue = captureValue;
                        bestCapture = move;
                        System.out.println("Captura candidata segura: " + move + ", valor: " + captureValue);
                    }
                }*/
            /*}
        }
        return bestCapture;
    }*/

    //Lista as peças brancas ativas
    private List<MovePiece> makeListWhitePieces(){
        List<MovePiece> whitePieces = movePieceRepository.findMovePieceByPieceColorLastMoveTrue(true,PieceColor.WHITE.toString());
       return whitePieces;
    }
    
    //Lista as peças pretas ativas
    private List<MovePiece> makeListBlackPieces(){
        List<MovePiece> blackPieces = movePieceRepository.findMovePieceByPieceColorLastMoveTrue(true,PieceColor.BLACK.toString());
       return blackPieces;
    }
    
    //Função que recebe a próxima jogada das peças pretas, atualiza o tabuleiro e o repository MovePiece
    //retorna a listagem com os movimentos ativos(atualiza o getPossibleMoves de cada peça) conseguindo
    //prever as reações das peças brancas opós a concretização da jogada.
    private List<MovePiece> getNewMovePiece(String notation, Piece piece, Map<String, Piece> tempBoard, Long gameId) {
    	
    	System.err.println("Próximo movimento: " + notation);
    	List<MovePiece> movPieceBlack = makeListBlackPieces();
    	MovePiece movPiece = new MovePiece();
    	List<MovePiece> newMovPieceBlack = null;
        String from = notation.substring(0, 2);
        String to = notation.substring(2, 4);
    	movPiece.setCodigoPiece(piece.getCodigo());
    	movPiece.setColorPiece(piece.getColor().toString());
    	movPiece.setToPiece(to);
    	movPiece.setFromPiece(from);
    	movPiece.setTypePiece(piece.getType().toString());
    	movPiece.setValuePiece(piece.getValuePiece());
    	movPiece.setFirstMoves(null);
    	movPiece.setLastMove(true);

    	List<String> listMoves = null;
        tempBoard.put(to, tempBoard.get(from));
        tempBoard.remove(from);
        String toMovePiece = movPiece.getToPiece() != null ? movPiece.getToPiece() : movPiece.getFromPiece();
        listMoves = getPossibleMovesForPiece(gameId, tempBoard, toMovePiece, piece);
   	 	movPiece.setMoveAttack(listMoves); 
   	 	movPieceBlack.add(movPiece);
        newMovPieceBlack = addAllPossibleAttackNextMove(gameId, tempBoard, to, movPieceBlack);
         
    	return  newMovPieceBlack; 
    	
    }
    
    //Movimentos simples de peças ativas
    //Movimenta as peças por linha
    //Estrategia de defesa
    @SuppressWarnings("unused")
	private String makeMoveBlackPieces(Long gameId){
        String movPieceFirst = null;
        List<MovePiece> movPiece = null;
        String movPieceFrom = null;
        String movPieceTo = null;

        int cont = 7;
        String linha = String.valueOf(cont);
        while(cont != 1) {
             movPiece = movePieceRepository.findMinValuePieceByLineAndColor(linha, PieceColor.BLACK.toString(), true) ;
            if(!movPiece.isEmpty() && movPiece.getFirst().getMoveAttack().size() > 0) {
            	System.err.println(" Próxima jogada total opções " + " estagio 03 linha: " + linha + " peça: " + movPiece.getFirst().getTypePiece() + " " + movPiece.getFirst().getMoveAttack().getFirst());
            	movPieceTo = movPiece.getFirst().getMoveAttack().getFirst(); 
                movPieceFrom = movPiece.getFirst().getToPiece() != null ? movPiece.getFirst().getToPiece() : movPiece.getFirst().getFromPiece(); 
            	movPieceFirst = movPieceFrom + movPieceTo;
            	cont = 1;
            	return movPieceFirst;
            }
            cont --;
            linha = String.valueOf(cont);

        }  

        if(!movPiece.isEmpty() && movPieceFirst != null ) {
    		boolean counterAttack = isNextMoveCounterAttack(gameId, movPieceFirst, movPiece.getFirst());
            if(counterAttack) {
            	movPieceFirst = null;
        	}
        }

        return movPieceFirst;
     }
    
    //Movimentos simples de peças ativas ativas
    private String makeMoveBlackPiecesKnight(){
        List<MovePiece> blackPieces = makeListBlackPieces();
        List<MovePiece> positionAttack = null;
        String movPieceFirst = null;
        List<MovePiece> movPiece = null;
        String movPieceFrom = null;
        if(blackPieces.isEmpty()) {
        	return null;
        }
        int cont = 8;
        String linha = String.valueOf(cont);
        while(cont != 1) {
             movPiece = movePieceRepository.findMinValuePieceByLineAndColorAndType(linha, PieceColor.BLACK.toString(), true,PieceType.KNIGHT.toString()) ;
            if(!movPiece.isEmpty() && movPiece.getFirst().getMoveAttack().size() > 0) {
            	System.err.println(" Próxima jogada total opções " + blackPieces.size() + " estagio 03 linha: " + linha + " peça: " +movPiece.getFirst().getTypePiece());
                movPieceFrom = movPiece.getFirst().getToPiece() != null ? movPiece.getFirst().getToPiece() : movPiece.getFirst().getFromPiece(); 
                String movPieceTo = movPiece.getFirst().getMoveAttack().getFirst();
                //Verifca se a posição onde vai está sob Attack.
                 if(!isPositionAtack(movPieceTo)) {
                	movPieceFirst = movPieceFrom + movPieceTo;
                	cont = 1;
                	continue;   	
                }

            }
            cont --;
            linha = String.valueOf(cont);

        }

        System.err.println(" Próxima jogada total opções " + blackPieces.size() + " estagio 02 ");
        return movPieceFirst;
     }
       
    private List<MovePiece> makeListCapture(){
	    String toWhite = null;
	    List<MovePiece> captures = null;
	    List<MovePiece> whitePieces = makeListWhitePieces();
	    if(!whitePieces.isEmpty()) {
	     for(MovePiece whitePiece : whitePieces) {
	    	 toWhite = whitePiece.getToPiece() != null ? whitePiece.getToPiece() : whitePiece.getFromPiece();
	         captures = movePieceRepository.findMovePieceByPieceColorInMoveAttckLastMoveTrue(true, PieceColor.BLACK.toString(),toWhite);
	       }
	     }
	    return captures;
    }
   
    private boolean makeListCounterAttack( String to ){
	     String toCapture = null;
	     List<MovePiece> capture = makeListCapture();
	     boolean contraAttack = false;
	     for(MovePiece cap : capture) {
	    	 toCapture = cap.getToPiece() != null ? cap.getToPiece() : cap.getFromPiece();

		     if(cap.getMoveAttack().contains(to)) {
	    	 	 contraAttack = true;
	             return contraAttack;
		     }
		}
	     return contraAttack;
    }
    
    //Retorna true testando se a captura é em peões com contraAttack de casas não inicializadas 
    //ou se a peça do que está no To é supeior a peça que será perdida.
    private boolean makeListCounterAttackNextMove( String to, List<MovePiece> movPieceWhite, MovePiece movPieceBlack ){
	     String toCapture = null;
	     String movTo = to.substring(2, 4);
	     boolean contraAttack = false;
	     for(MovePiece cap : movPieceWhite) {
	    	 toCapture = cap.getToPiece() != null ? cap.getToPiece() : cap.getFromPiece();
	    	 if(cap.getTypePiece() == PieceType.PAWN.toString() && cap.getColorPiece() == PieceColor.WHITE.toString() && toCapture.equals(movTo)
	    			 || cap.getTypePiece() == PieceType.PAWN.toString() && cap.getColorPiece() == PieceColor.BLACK.toString() && toCapture.equals(movTo)) {
	    		 contraAttack = isPieceNeverMove(toCapture, cap.getColorPiece());
	    		 return contraAttack;
	    	 }
		     if(cap.getMoveAttack().contains(movTo)) {
		    	 MovePiece movPiece = movePieceRepository.findValuePieceFromPieceByColor(movTo, true, PieceColor.WHITE.toString());
		    	 if(movPiece != null) {
		    		 if(movPiece.getValuePiece() < movPieceBlack.getValuePiece()) {
		    			 contraAttack = true;
		    		 }
		    	 }
	             return contraAttack;
		     }    		 
		}
	     return contraAttack;
   }
    
    
    public boolean makeListCounterAttackNextMove(Map<String, Piece> board, String from, String to) {
        Piece attacker = board.get(from);
        if (attacker == null || attacker.getColor() != PieceColor.BLACK) {
            return false; // só peças pretas
        }

        // alvo da jogada
        Piece target = board.get(to);
        if (target == null || target.getColor() == PieceColor.BLACK) {
            return false; // precisa ter peça branca no destino
        }

        // calcula saldo da troca com SEE
        int balance = staticExchangeEval(board, from, to);

        // se positivo ou neutro, vale a pena contra-atacar
        return balance >= 0 ? true : false;
    }

    
    
    
    //Procura as peças pretas que estejam sobre mira das brancas
    //Objetivo retirar peças que estejam no caminho(attackList)
    //Após selecionar a casa sob attack, verifica se a saída tem contraAttack
    private String makeListAttackWhite(Long gameId){
    	
    	 Game gameOpt = gameRepository.findById(gameId).get();
    	 if(!gameOpt.isWhiteTurn()) {
    		 return null;
    	 }
	     String toBlack = null;
	     List<MovePiece> piecesWhite = makeListWhitePieces();
	     List<MovePiece> piecesBlack = makeListBlackPieces();
	     MovePiece bestMovePiece = null;
	     String moveNice = null;
	     int valuePiece = 0;
	     for(MovePiece white : piecesWhite) {
	    	 for(MovePiece black : piecesBlack) {
		    	 toBlack = black.getToPiece() != null ? black.getToPiece() : black.getFromPiece();
	 
			     if(white.getMoveAttack().contains(toBlack) && black.getMoveAttack().size() > 0) {
			    	 System.err.println("Peça na posição: ");
			    	 //if(black.getValuePiece() > valuePiece) {
			    		 //String bestMove = moveIsBest(black);
			    		 //if(bestMove != null) {
			    			 //moveNice = bestMove;
			    		 //}else {
			    			 moveNice = toBlack + white.getMoveAttack().getFirst();  
			    		 //}
		    			 
		    			 valuePiece = black.getValuePiece(); 
		    			 bestMovePiece = black;
			    	 //}
	    	 
			     }
	    	 }
	
		}
	     
	    // if(bestMovePiece == null) {
	    	 //return null;
	     //}

        //if(isNextMoveCounterAttack(gameId, moveNice, bestMovePiece)) {
        	//moveNice = null;
        //} 	 
     
	     return moveNice;
   }
    
    //Atualiza o board e uma lista de MovePiece Black e White com os contraAttack de cada peça
    //No teste de contraAttack verifica se a peça já movimentou para testar um contraAttack das peças anteriores. 
    private boolean isNextMoveCounterAttack(Long gameId, String moveNice, MovePiece bestMovePiece) {
    	boolean nextMoveBest = false;
    	String from = moveNice.substring(0, 2);
    	String to = moveNice.substring(2, 4);
        Piece piece = new Piece();
        Optional<Game> game = gameRepository.findById(gameId);
        Map<String, Piece> tempBoard = deserializeBoardState(game.get().getBoardState());
        piece = selectPiece(bestMovePiece.getTypePiece(), bestMovePiece.getColorPiece(), bestMovePiece.getCodigoPiece());
        List<MovePiece> listMovPiece = getNewMovePiece( moveNice,  piece, tempBoard, gameId);
        nextMoveBest =  makeListCounterAttackNextMove(moveNice, listMovPiece, bestMovePiece);
        //nextMoveBest = makeListCounterAttackNextMove( tempBoard, from, to);
    	
    	return nextMoveBest;
    }
    
    
    
    //Verifica as possíveis movimentações das peças pretas incluindo capturas 
    //Em caso de captura, testa se tem contraAttack incluindo de peças sem movimento nas casas anteriores.
    //Em caso de captura avalia se a peça à perder é inferior a captura.
    private String makeListAttackblack(Long gameId){
	     String toWhite = null;
	     String toBlack = null;
	     List<MovePiece> piecesWhite = makeListWhitePieces();
	     List<MovePiece> piecesBlack = makeListBlackPieces();
	     String moveNice = null;
	     int valuePiece = 0;
	     for(MovePiece black : piecesBlack) {
	    	 toBlack = black.getToPiece() != null ? black.getToPiece() : black.getFromPiece();
	    	 for(MovePiece white : piecesWhite) {
		    	 toWhite = white.getToPiece() != null ? white.getToPiece() : white.getFromPiece();
  			     if(black.getMoveAttack().contains(toWhite) && black.getMoveAttack().size() > 0) {
			    	 if(white.getValuePiece() > valuePiece) {
			    		 String bestMove = moveIsBest(white);
			    		 moveNice = toBlack + toWhite; 
			    		 valuePiece = white.getValuePiece();
		    			 System.err.println("Não sabe se tem contra-attack: " + black.getTypePiece() + " Estagio 1 casa: " + toWhite);
		    			 /*if(makeListCounterAttackNextMove(moveNice,piecesWhite)) {
		    				 System.err.println("Tem contra-attack: " + black.getTypePiece() + " Estagio 2");
		    				 if((white.getValuePiece() < black.getValuePiece())) {
		    					moveNice = null; 
		    				 }
		    			 }*/
			    		if(isNextMoveCounterAttack(gameId, moveNice, black)) {
		    				 System.err.println("Tem contra-attack: " + black.getTypePiece() + " Estagio 2");
		    				 if((white.getValuePiece() < black.getValuePiece())) {
		    					return moveNice = null; 
		    				 }
			    		}
			    	 }
	    	 
			     }
	    	 }

		}
	     return moveNice;
  }
    
   //Como as capturas são feitas aleatórias, é feito um teste para verificar se a captura de peças
    //que ainda não se movimentaram e tem a lista de contraAttack nula
    //Verifica as três casas anteiores a peça capturada(anteior atraz,casa atraz, posterior atraz)
   private boolean isPieceNeverMove(String to, String pieceColor) {
	boolean isCounterAttack = false;
   	int linha = pieceColor == PieceColor.WHITE.toString() ? Integer.parseInt(to.substring(1, 2))- 1 : Integer.parseInt(to.substring(1, 2)) + 1;
   	String lin = String.valueOf(linha);
   	String color = pieceColor == PieceColor.WHITE.toString() ? PieceColor.WHITE.toString() : PieceColor.BLACK.toString(); 
   	char letra = Character.toUpperCase(to.charAt(0));
   	char colAnt = '\u0000';
   	char colPos = '\u0000';
   	char col = '\u0000';
   	int colunaAnt = 0;
   	int colunaPos = 0;
   	int coluna = 0;
   	
   	coluna = letra - 'A' + 1;
   	if(coluna > 1  && coluna <= 8 ) {
   		colunaAnt = coluna - 1;
   		colAnt = (char) Character.toLowerCase('A' + colunaAnt - 1);
   	}
   	if(coluna >= 1 && coluna < 8 ) {
   		colunaPos = coluna + 1;
   		colPos = (char) Character.toLowerCase('A' + colunaPos - 1);
   	}
   	if(coluna >= 1 && coluna <= 8) {
   		col = (char) Character.toLowerCase('A' + coluna - 1);
   	}
   	String fromAnt = null;
   	String fromPos = null;
   	String from = null;
   	MovePiece movPiece = null;
   	MovePiece movPieceAnt = null;
   	MovePiece movPiecePos = null;
   	System.err.println("colAnt: "  + colAnt + "colPos: "  + colPos +  "col: "  + col +" Estagio 00 na casa: " + " PieceColor: " + " tem contraAttack linha: " + linha);
   	if(colAnt != '\u0000' && isCounterAttack == false) {
       	fromAnt =  String.valueOf(colAnt);
       	fromAnt = fromAnt + lin;
       	System.err.println("movPieceAnt: "  + " Estagio 00 na casa: " + from + " PieceColor: " + " tem contraAttack linha: " + lin);
       	movPieceAnt = movePieceRepository.findPieceByFromPieceAndColorAndLastMove( fromAnt, color, true);
       	if(movPieceAnt != null) {
       		System.err.println("movPieceAnt: " + movPieceAnt.getTypePiece() + " Estagio 01 na casa: " + fromAnt + " PieceColor: " + movPieceAnt.getColorPiece() + " tem contraAttack linha: " + lin); 
       		if(movPieceAnt.getFirstMoves().contains(to)) {
       			isCounterAttack = true;
       			System.err.println("movPieceAnt: " + movPieceAnt.getTypePiece() + " Estagio 02 na casa: " + fromAnt + " PieceColor: " + movPieceAnt.getColorPiece() + " tem contraAttack linha: " + lin); 
       		}
       		
       	}
       	
   	}

   	if(colPos != '\u0000' && isCounterAttack == false) {
       	fromPos =  String.valueOf(colPos);
       	fromPos = fromPos + lin;
       	movPiecePos = movePieceRepository.findPieceByFromPieceAndColorAndLastMove( fromPos, color, true);
       	if(movPiecePos != null) {
       		System.err.println("movPiecePos: " + movPiecePos.getTypePiece() + " Estagio 03 na casa: " + fromPos + " PieceColor: " + movPiecePos.getColorPiece() + " tem contraAttack linha: " + lin); 
       		if(movPiecePos.getFirstMoves().contains(to)) {
       			isCounterAttack = true;
       			System.err.println("movPieceAnt: " + movPiecePos.getTypePiece() + " Estagio 04 na casa: " + fromPos + " PieceColor: " + movPiecePos.getColorPiece() + " tem contraAttack linha: " + lin); 
       		}
       	}
       	
   	}
   	
   	if(col != '\u0000' && isCounterAttack == false) {
       	from =  String.valueOf(col);
       	from = from + lin;
       	System.err.println("movPiece: "  + " Estagio 00 na casa: " + from + " PieceColor: " + " tem contraAttack linha: " + lin);
       	movPiece = movePieceRepository.findPieceByFromPieceAndColorAndLastMove( from, color, true);
       	if(movPiece != null) {
       		System.err.println("movPieceAnt: " + movPiece.getTypePiece() + " Estagio 05 na casa: " + from + " PieceColor: " + movPiece.getColorPiece() + " tem contraAttack linha: " + lin); 
       		if(movPiece.getFirstMoves().contains(to)) {
       			isCounterAttack = true;
       			System.err.println("movPieceAnt: " + movPiece.getTypePiece() + " Estagio 06 na casa: " + from + " PieceColor: " + movPiece.getColorPiece() + " tem contraAttack linha: " + lin);
       		}
       	}
       	
   	}
  	
   	return isCounterAttack;
   }
    
   //No caso de mais de uma captura, avalia a peça de maior value e retorna a casa para captura
   private String moveIsBest(MovePiece movPiece) {
	   
	   MovePiece movePiece = movPiece;
	   MovePiece movePieceBest = null;
	   String move = null;
	   int pieceValue = 0;
	   String pieceColor = movPiece.getColorPiece() == PieceColor.BLACK.toString() ? PieceColor.WHITE.toString() : PieceColor.BLACK.toString();
	   
	   for(String mov : movePiece.getMoveAttack()) {
		   movePieceBest = movePieceRepository.findValuePieceFromPieceByColor(mov, true, pieceColor);
		   if(movePieceBest != null) {
			   if(movePieceBest.getValuePiece() > pieceValue) {
				   String movePieceBestTo = movePieceBest.getToPiece() != null ? movePieceBest.getToPiece() : movePieceBest.getFromPiece();
				   move = movePieceBestTo + mov;
			   }
		   }
	   }
	   
	   
	   return move;
   }
    
   //Faz um select para verificar se o próximo movimento está na lista de attack das peças White
   private boolean isPositionAtack(String to) {
	   List<MovePiece> positionAttack = positionAttack = movePieceRepository.findMovePieceByPieceColorInMoveAttckLastMoveTrue(true, PieceColor.WHITE.toString(), to);
	   if(positionAttack.isEmpty()) {
		   return false;
	   }
	   return true;
   }
   
   //Testar se os movimentos de iniciar são iguais a check-mate anteriores
   //Os check-mate com poucos movimentos são gravados em repository
   public boolean makePlayCheckMate(List<MovePiece> movesPiece,List<BadOpenig> movesBad) {
        int cont = 0;
        System.err.println("Total de sequências bad: " + cont);;
        if(movesBad.size() > 0 && movesPiece.size() > 0) {
	        for(BadOpenig moves : movesBad) {
		         for (MovePiece movPiece : movesPiece){
		             if(moves.getMoveSequence().contains(movPiece.getMov())){
		             cont ++;
		             }
		         };
	         }
	         if(cont > 2) {
	        	 return true;
	         }
        }
        return false;
    }
   
   //Em caso das jogadas selecionas seja nulas o computador recorre as jogadas 
   //com avaliação de pontuação para optar a de menor risco.
    public Game makeDefensiveMove(Long gameId) {
        System.out.println("Iniciando makeDefensiveMove para gameId: " + gameId);
        List<BadOpenig> badOpenig = badOpeningRepository != null ? badOpeningRepository.findAll() : new ArrayList<>();
        List<MovePiece> movesPiece = movePieceRepository != null ? movePieceRepository.findAll() : new ArrayList<>();
        System.err.println("Total de Registros movePiece: " + movesPiece.size() + " Total BadOpening: " + badOpenig.size());
        Optional<Game> gameOpt = gameRepository.findById(gameId);
        if (gameOpt.isEmpty()) {
            System.out.println("Jogo não encontrado: " + gameId);
            throw new IllegalArgumentException("Jogo não encontrado: " + gameId);
        }
        Game game = gameOpt.get();
        if (game.isWhiteTurn()) {
            System.out.println("Não é a vez das pretas!");
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }
       
        if(movesPiece.size() > 4 && movesPiece.size() < 12 && badOpenig.size() > 0) {
        }
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        List<String> moves = getAllPossibleMoves(gameId, board, PieceColor.BLACK);
        boolean blackInCheck = isKingInCheck(board, PieceColor.BLACK);
        if (moves.isEmpty()) {
            System.out.println("Nenhum movimento disponível para as pretas!");
            game.setStatus(blackInCheck ? GameStatus.WHITE_WINS : GameStatus.DRAW);
            return gameRepository.save(game);
        }

        if (blackInCheck) {
            String kingDefense = selectDefensiveMove(gameId, board, moves);
            if (kingDefense != null) {
                System.out.println("Defesa do rei preto selecionada: " + kingDefense);
                return makeMove(gameId, kingDefense);
            }
            System.out.println("Xeque-mate detectado!");
            game.setStatus(GameStatus.WHITE_WINS);
            return gameRepository.save(game);
        }

        Optional<CheckmatePattern> activePlan = checkmateService.findActiveCurrentCheckmatePlan();
        boolean finalPlannedMove = activePlan.map(this::isFinalCurrentCheckmateMove).orElse(false);
        if (!finalPlannedMove) {
            String priorityDefense = findCheckmatePriorityDefenseMove(board, moves);
            if (priorityDefense != null) {
                System.out.println("Prioridade defensiva selecionada antes do plano de checkmate: " + priorityDefense);
                return makeMove(gameId, priorityDefense);
            }
        }

        if (activePlan.isPresent()) {
            Game plannedMove = tryExecuteCurrentCheckmatePlan(gameId, board, moves, activePlan.get());
            if (plannedMove != null) {
                return plannedMove;
            }

            List<String> replannedMoves = findCurrentCheckmatePlanMoves(gameId, board);
            if (!replannedMoves.isEmpty()) {
                CheckmatePattern replanned = checkmateService.saveCurrentCheckmatePlan(replannedMoves);
                Game replannedMove = tryExecuteCurrentCheckmatePlan(gameId, board, moves, replanned);
                if (replannedMove != null) {
                    return replannedMove;
                }
            }

            checkmateService.abortCurrentCheckmatePlan();
        } else if (countPiecesByColor(board, PieceColor.WHITE) <= 12) {
            List<String> plannedMoves = findCurrentCheckmatePlanMoves(gameId, board);
            if (!plannedMoves.isEmpty()) {
                CheckmatePattern pattern = checkmateService.saveCurrentCheckmatePlan(plannedMoves);
                Game plannedMove = tryExecuteCurrentCheckmatePlan(gameId, board, moves, pattern);
                if (plannedMove != null) {
                    return plannedMove;
                }
                checkmateService.abortCurrentCheckmatePlan();
            }
        }

        String bestDefense = selectDefensiveMove(gameId, board, moves);
        if (bestDefense != null) {
            System.out.println("Defesa selecionada: " + bestDefense);
            Game updatedGame = makeMove(gameId, bestDefense);
            System.err.println("Movimento defensivo aplicado: " + updatedGame.getLastMove());
            return updatedGame;
        }

        System.out.println("Usando hard-computer-move como fallback...");
        Game hardGame = makeHardAIMove(gameId);
        System.out.println("Movimento hard aplicado: " + hardGame.getLastMove());
        return hardGame;
    }

    private String findCheckmatePriorityDefenseMove(Map<String, Piece> board, List<String> moves) {
        String queenDefense = findReactiveQueenDefenseMove(board, moves);
        if (queenDefense != null) {
            return queenDefense;
        }

        String rookDefense = findReactiveRookDefenseMove(board, moves);
        if (rookDefense != null) {
            return rookDefense;
        }

        String knightDefense = findReactivePieceDefenseMove(board, moves, PieceType.KNIGHT);
        if (knightDefense != null) {
            return knightDefense;
        }

        return findReactivePieceDefenseMove(board, moves, PieceType.BISHOP);
    }

    private String findReactivePieceDefenseMove(Map<String, Piece> board, List<String> moves, PieceType pieceType) {
        if (board == null || moves == null || moves.isEmpty()) {
            return null;
        }

        String bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            String threatenedSquare = entry.getKey();
            Piece threatenedPiece = entry.getValue();
            if (threatenedPiece == null || threatenedPiece.getColor() != PieceColor.BLACK
                    || threatenedPiece.getType() != pieceType
                    || !isSquareUnderAttack(board, threatenedSquare, PieceColor.WHITE)) {
                continue;
            }

            for (String moveNotation : moves) {
                if (moveNotation == null || moveNotation.length() < 4 || !moveNotation.startsWith(threatenedSquare)) {
                    continue;
                }

                String to = moveNotation.substring(2, 4);
                Map<String, Piece> tempBoard = deepCopyBoard(board);
                tempBoard.put(to, tempBoard.remove(threatenedSquare));
                if (isSquareUnderAttack(tempBoard, to, PieceColor.WHITE)) {
                    continue;
                }

                int score = 1000;
                Piece capturedPiece = board.get(to);
                if (capturedPiece != null && capturedPiece.getColor() == PieceColor.WHITE) {
                    score += capturedPiece.getValuePiece() * 20;
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = moveNotation;
                }
            }
        }

        return bestMove;
    }

    private Game tryExecuteCurrentCheckmatePlan(Long gameId, Map<String, Piece> board, List<String> legalMoves,
            CheckmatePattern pattern) {
        if (!isCurrentCheckmatePlanCompatible(gameId, board, legalMoves, pattern)) {
            System.err.println("Plano checkmate-atual incompatível com o tabuleiro atual.");
            return null;
        }

        String moveNotation = nextMoveFromPattern(pattern);
        Game updatedGame = makeMove(gameId, moveNotation);
        checkmateService.advanceCurrentCheckmatePlan();

        int nextIndex = pattern.getIndexNextMove() == null ? 1 : pattern.getIndexNextMove() + 1;
        if (pattern.getMoves() == null || nextIndex >= pattern.getMoves().size()) {
            if (!updatedGame.isCheckmate()) {
                checkmateService.abortCurrentCheckmatePlan();
            } else {
                checkmateService.abortCurrentCheckmatePlan();
            }
        }

        return updatedGame;
    }

    private boolean isCurrentCheckmatePlanCompatible(Long gameId, Map<String, Piece> board, List<String> legalMoves,
            CheckmatePattern pattern) {
        String nextMove = nextMoveFromPattern(pattern);
        if (nextMove == null || nextMove.length() < 4 || legalMoves == null || !legalMoves.contains(nextMove)) {
            return false;
        }

        return canApplyPlannedSequence(gameId, board, remainingMoves(pattern));
    }

    private boolean isFinalCurrentCheckmateMove(CheckmatePattern pattern) {
        if (pattern == null || pattern.getMoves() == null || pattern.getMoves().isEmpty()) {
            return false;
        }
        int index = pattern.getIndexNextMove() == null ? 0 : pattern.getIndexNextMove();
        return index >= pattern.getMoves().size() - 1;
    }

    private String nextMoveFromPattern(CheckmatePattern pattern) {
        if (pattern == null || pattern.getMoves() == null || pattern.getMoves().isEmpty()) {
            return null;
        }
        int index = pattern.getIndexNextMove() == null ? 0 : pattern.getIndexNextMove();
        if (index < 0 || index >= pattern.getMoves().size()) {
            return null;
        }
        return pattern.getMoves().get(index);
    }

    private List<String> remainingMoves(CheckmatePattern pattern) {
        List<String> remaining = new ArrayList<>();
        if (pattern == null || pattern.getMoves() == null) {
            return remaining;
        }
        int index = pattern.getIndexNextMove() == null ? 0 : pattern.getIndexNextMove();
        for (int i = index; i < pattern.getMoves().size(); i++) {
            remaining.add(pattern.getMoves().get(i));
        }
        return remaining;
    }

    private boolean canApplyPlannedSequence(Long gameId, Map<String, Piece> board, List<String> plannedMoves) {
        Map<String, Piece> tempBoard = deepCopyBoard(board);
        for (String moveNotation : plannedMoves) {
            List<String> legalMoves = getAllPossibleMoves(gameId, tempBoard, PieceColor.BLACK);
            if (!legalMoves.contains(moveNotation) || !applyMoveOnBoard(tempBoard, moveNotation)) {
                return false;
            }
        }
        return true;
    }

    private List<String> findCurrentCheckmatePlanMoves(Long gameId, Map<String, Piece> board) {
        List<String> sequence = searchCheckmateSequence(gameId, deepCopyBoard(board), new ArrayList<>(), 3);
        return sequence == null ? new ArrayList<>() : sequence;
    }

    private List<String> searchCheckmateSequence(Long gameId, Map<String, Piece> board, List<String> prefix, int depthLeft) {
        if (depthLeft == 0) {
            return null;
        }

        List<String> moves = getAllPossibleMoves(gameId, board, PieceColor.BLACK);
        moves.sort((left, right) -> Integer.compare(checkmateAttackerPriority(board, right), checkmateAttackerPriority(board, left)));
        for (String moveNotation : moves) {
            Map<String, Piece> tempBoard = deepCopyBoard(board);
            if (!applyMoveOnBoard(tempBoard, moveNotation)) {
                continue;
            }

            List<String> candidate = new ArrayList<>(prefix);
            candidate.add(moveNotation);
            if (isKingInCheck(tempBoard, PieceColor.WHITE) && isCheckmate(tempBoard, PieceColor.WHITE)) {
                return candidate;
            }

            List<String> deeper = searchCheckmateSequence(gameId, tempBoard, candidate, depthLeft - 1);
            if (deeper != null) {
                return deeper;
            }
        }

        return null;
    }

    private int checkmateAttackerPriority(Map<String, Piece> board, String moveNotation) {
        if (moveNotation == null || moveNotation.length() < 4) {
            return 0;
        }
        Piece piece = board.get(moveNotation.substring(0, 2));
        if (piece == null || piece.getColor() != PieceColor.BLACK) {
            return 0;
        }
        return switch (piece.getType()) {
            case QUEEN -> 400;
            case ROOK -> 300;
            case BISHOP -> 200;
            case KNIGHT -> 100;
            default -> 10;
        };
    }

    private int countPiecesByColor(Map<String, Piece> board, PieceColor color) {
        int count = 0;
        for (Piece piece : board.values()) {
            if (piece != null && piece.getColor() == color) {
                count++;
            }
        }
        return count;
    }

    private boolean applyMoveOnBoard(Map<String, Piece> board, String moveNotation) {
        if (board == null || moveNotation == null || moveNotation.length() < 4) {
            return false;
        }
        String from = moveNotation.substring(0, 2);
        String to = moveNotation.substring(2, 4);
        Piece movingPiece = board.get(from);
        Piece targetPiece = board.get(to);
        if (movingPiece == null || movingPiece.getColor() != PieceColor.BLACK
                || (targetPiece != null && targetPiece.getColor() == PieceColor.BLACK)) {
            return false;
        }
        board.put(to, movingPiece);
        board.remove(from);
        return !isKingInCheck(board, PieceColor.BLACK);
    }
    
    /*@Transactional
    public Game makeAIMoveO3(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        String boardState = game.getBoardState();
        String lastMove = game.getLastMove();

        // 1. IA calcula
        String aiMove = aiService.getAIMoveO3(boardState, lastMove);

        System.out.println("IA o3-mini sugeriu o movimento: " + aiMove);

        // 2. Reutiliza mecanismo atual de movimento
        return makeMove(id, aiMove);
    }*/
    
    public Game makeO3MiniMove(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));

        String sideToMove = game.isWhiteTurn() ? "WHITE" : "BLACK";
        String boardJson = game.getBoardState();

        // Add context for better AI performance
        List<Move> movesList = findMoveByIdGame(gameId);
        StringBuilder historyBuilder = new StringBuilder();
        for (Move m : movesList) {
            if (m.getFrom() != null && m.getTo() != null) {
                historyBuilder.append(m.getFrom()).append(m.getTo()).append(" ");
            }
        }
        String moveHistory = historyBuilder.toString().trim();

        Map<String, Piece> board = deserializeBoardState(boardJson);
        String fen = generateFEN(board, game.isWhiteTurn());
        boolean inCheck = game.isInCheck();

        System.out.println("O3-Mini context - History length: " + moveHistory.length() + ", FEN: " + fen);

        String moveNotation = aiService.suggestMove(boardJson, sideToMove, moveHistory, fen, inCheck);

        // Reaproveita tua lógica atual de movimento:
        return makeMove(gameId, moveNotation);
    }

    public Game makeChatGPTHardMove(Long gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + gameId));

        if (game.isWhiteTurn()) {
            throw new IllegalArgumentException("Não é a vez das pretas!");
        }

        // Add rich context for better AI decisions
        List<Move> movesList = findMoveByIdGame(gameId);
        StringBuilder historyBuilder = new StringBuilder();
        for (Move m : movesList) {
            if (m.getFrom() != null && m.getTo() != null) {
                historyBuilder.append(m.getFrom()).append(m.getTo()).append(" ");
            }
        }
        String moveHistory = historyBuilder.toString().trim();

        Map<String, Piece> board = deserializeBoardState(game.getBoardState());
        String fen = generateFEN(board, game.isWhiteTurn());  // false for black's turn
        boolean inCheck = game.isInCheck();

        System.out.println("ChatGPT-Hard context - History: " + (moveHistory.isEmpty() ? "none" : moveHistory.substring(0, Math.min(50, moveHistory.length()))) +
                          ", FEN: " + fen + ", InCheck: " + inCheck);

        String moveNotation = aiService.suggestHardMove(game.getBoardState(), "BLACK", moveHistory, fen, inCheck);
        validateChatGPTHardMove(game, moveNotation);
        return makeMove(gameId, moveNotation);
    }
    
    public String generateFEN(Map<String, Piece> board, boolean whiteToMove) {

        StringBuilder fen = new StringBuilder();

        for (int rank = 8; rank >= 1; rank--) {
            int emptyCount = 0;

            for (char file = 'a'; file <= 'h'; file++) {
                String square = "" + file + rank;

                if (!board.containsKey(square)) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }

                    Piece p = board.get(square);
                    fen.append(pieceToFen(p));
                }
            }

            if (emptyCount > 0)
                fen.append(emptyCount);

            if (rank > 1)
                fen.append('/');
        }

        // Turno
        fen.append(" ");
        fen.append(whiteToMove ? "w" : "b");

        // Sem roque por enquanto (pode habilitar depois)
        fen.append(" - - 0 1");

        return fen.toString();
    }

    private String pieceToFen(Piece p) {
        String letter = switch (p.getType()) {
            case KING -> "k";
            case QUEEN -> "q";
            case ROOK -> "r";
            case BISHOP -> "b";
            case KNIGHT -> "n";
            case PAWN -> "p";
        };

        return p.getColor() == PieceColor.WHITE
                ? letter.toUpperCase()
                : letter;
    }
    
    
    
    public Game makeStockfishMove(Long gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jogo não encontrado"));

        // 1 — desserializa o boardState atual
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());

        // 2 — gera o FEN correto
        String fen = generateFEN(board, game.isWhiteTurn());
        System.out.println("FEN enviado ao Stockfish: " + fen);

        // 3 — chama o engine
        String bestMove = stockfishService.getBestMove(fen, 14);
        System.out.println("Stockfish sugeriu: " + bestMove);

        // 4 — aplica esse movimento usando seu motor já existente
        makeMove(gameId, bestMove);

        // 5 — retorna o jogo atualizado
        return gameRepository.findById(gameId).get();
    }




}
