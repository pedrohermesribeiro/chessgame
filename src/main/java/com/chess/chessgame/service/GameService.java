package com.chess.chessgame.service;

import java.sql.Clob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chess.chessgame.dto.GameDTO;
import com.chess.chessgame.model.Game;
import com.chess.chessgame.model.Move;
import com.chess.chessgame.model.Piece;
import com.chess.chessgame.model.enums.GameStatus;
import com.chess.chessgame.model.enums.PieceColor;
import com.chess.chessgame.model.enums.PieceType;
import com.chess.chessgame.repository.GameRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;
    
    private  Clob boardTest = null;
    
    private String stringBoard = null;
    
    

    public Game createGame(String playerWhite, String playerBlack) {
        Game game = new Game();
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
        
        Map<String, Piece> board = this.initializeBoard();
        game.setBoardState(this.serializeBoardState(board));
        gameRepository.save(game);
        Game gm = game;
        return gm;
    }
    
    public Game resetGame(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Jogo não encontrado: " + id));
        //Game game = new Game();
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setWhiteTurn(true);
        game.setCheckmate(false);
        game.setInCheck(false);
        game.setLastMove(null);
        game.setBoardState(serializeBoardState(initializeBoard()));
        game.setBoardStateHistory(new ArrayList<>());
        game.setBlackKingMoved(false);
        game.setBlackRookA8Moved(false);
        game.setBlackRookH8Moved(false);
        game.setWhiteKingMoved(false);
        game.setWhiteRookA1Moved(false);
        game.setWhiteRookH1Moved(false);
        game.setMoves(new ArrayList<>());
        return gameRepository.save(game);
    }

    public Optional<Game> getGame(Long id) {
        return gameRepository.findById(id);
    }

    private  Map<String, Piece> initializeBoard() {
    	Map<String, Piece> board = new HashMap<>();
        // Peças brancas
        board.put("a1", new Piece(PieceType.ROOK, PieceColor.WHITE));
        board.put("b1", new Piece(PieceType.KNIGHT, PieceColor.WHITE));
        board.put("c1", new Piece(PieceType.BISHOP, PieceColor.WHITE));
        board.put("d1", new Piece(PieceType.QUEEN, PieceColor.WHITE));
        board.put("e1", new Piece(PieceType.KING, PieceColor.WHITE));
        board.put("f1", new Piece(PieceType.BISHOP, PieceColor.WHITE));
        board.put("g1", new Piece(PieceType.KNIGHT, PieceColor.WHITE));
        board.put("h1", new Piece(PieceType.ROOK, PieceColor.WHITE));
        for (char file = 'a'; file <= 'h'; file++) {
            board.put(file + "2", new Piece(PieceType.PAWN, PieceColor.WHITE));
        }
        // Peças pretas
        board.put("a8", new Piece(PieceType.ROOK, PieceColor.BLACK));
        board.put("b8", new Piece(PieceType.KNIGHT, PieceColor.BLACK));
        board.put("c8", new Piece(PieceType.BISHOP, PieceColor.BLACK));
        board.put("d8", new Piece(PieceType.QUEEN, PieceColor.BLACK));
        board.put("e8", new Piece(PieceType.KING, PieceColor.BLACK));
        board.put("f8", new Piece(PieceType.BISHOP, PieceColor.BLACK));
        board.put("g8", new Piece(PieceType.KNIGHT, PieceColor.BLACK));
        board.put("h8", new Piece(PieceType.ROOK, PieceColor.BLACK));
        for (char file = 'a'; file <= 'h'; file++) {
            board.put(file + "7", new Piece(PieceType.PAWN, PieceColor.BLACK));
        }
        
        return board;
    }
    
    
    
    private String serializeBoardState(Map<String, Piece> boardState) {
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
        
        Map<String, Piece> board = deserializeBoardState(game.getBoardState());

        return new GameDTO(
            game.getId(),
            game.getPlayerWhite(),
            game.getPlayerBlack(),
            game.isWhiteTurn(),
            game.getStatus(),
            game.getLastMove(),
            game.isInCheck(),
            game.isCheckmate(),
            board,
            game.getBoardStateHistory());
    }

    
    public Game makeMove(Long id, String moveNotation) {
        Optional<Game> gameOpt = gameRepository.findById(id);
        if (gameOpt.isEmpty()) {
            throw new IllegalArgumentException("Jogo não encontrado: " + id);
        }
        Game game = gameOpt.get();

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

        // Verifica se o rei do jogador atual está em xeque antes do movimento
        boolean kingInCheckBefore = isKingInCheck(board, playerColor);

        // Executa o movimento com base no tipo de peça
        int fromRow = Character.getNumericValue(move.getFrom().charAt(1));
        int toRow = Character.getNumericValue(move.getTo().charAt(1));
        char fromCol = move.getFrom().charAt(0);
        char toCol = move.getTo().charAt(0);

        switch (piece.getType()) {
            case PAWN:
                handlePawnMove(game, board, piece, move.getFrom(), move.getTo(), fromRow, toRow, fromCol, toCol, playerColor);
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

        // Verifica xeque-mate ou empate
        if (opponentKingInCheck) {
            try {
                boolean isCheckmate = isCheckmate(board, opponentColor);
                System.out.println("Cheque-mate verificado: " + isCheckmate);
                if (isCheckmate) {
                    game.setCheckmate(true);
                    game.setStatus(GameStatus.CHECKMATE);
                }
            } catch (Exception e) {
                System.err.println("Erro ao verificar cheque-mate: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("Resultado: checkmate=" + game.isCheckmate() + ", status=" + game.getStatus());
        return gameRepository.save(game);
    }
       
       
    private PieceType parsePromotion(String moveNotation) {
        if (moveNotation.length() > 4) {
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

        return new Move(from, to, false, false);
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
            int fromRow, int toRow, char fromCol, char toCol, PieceColor playerColor) {
			int direction = playerColor == PieceColor.WHITE ? 1 : -1;
			int startRow = playerColor == PieceColor.WHITE ? 2 : 7;
			boolean isCapture = board.containsKey(to);
			
			System.out.println("Processando movimento de peão: " + from + " -> " + to);
			
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
			            (playerColor == PieceColor.WHITE ? fromRow == 5 : fromRow == 4)) { // Verifica a fileira correta
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
				movingPiece = new Piece(PieceType.QUEEN, playerColor);
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
            if (piece.getType() == PieceType.KING && piece.getColor() == kingColor) {
                kingPosition = entry.getKey();
                break;
            }
        }

        if (kingPosition == null) return false;

        PieceColor opponentColor = kingColor == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE;

        // 2. Verificar se alguma peça do oponente pode atingir a posição do rei
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            String from = entry.getKey();
            Piece piece = entry.getValue();

            if (piece.getColor() == opponentColor) {
                // ⚠️ Aqui usamos a cor real da peça para verificar o movimento
                if (isMoveLegal(board, from, kingPosition, piece.getColor())) {
                    return true;
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
            if (entry.getValue().getType() == PieceType.KING && entry.getValue().getColor() == kinColor) {
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
            if (!board.containsKey(to) && isMoveLegal(board, from, to, color)) {
                moves.add(to);
            }
            // Movimento duplo
            if (fromRow == startRow) {
                to = "" + from.charAt(0) + (fromRow + 2 * direction + 1);
                String intermediate = "" + from.charAt(0) + (fromRow + direction + 1);
                if (!board.containsKey(intermediate) && !board.containsKey(to) && isMoveLegal(board, from, to, color)) {
                    moves.add(to);
                }
            }
            // Capturas
            for (int colOffset : new int[]{-1, 1}) {
                char toCol = (char) (from.charAt(0) + colOffset);
                if (toCol >= 'a' && toCol <= 'h') {
                    to = "" + toCol + (fromRow + direction + 1);
                    Piece target = board.get(to);
                    if (target != null && target.getColor() != color && isMoveLegal(board, from, to, color)) {
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
                    if ((target == null || target.getColor() != color) && isMoveLegal(board, from, to, color)) {
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
                    if (isMoveLegal(board, from, to, color)) {
                        moves.add(to);
                    }
                    if (board.containsKey(to)) {
                        break;
                    }
                }
            }
        } else if (type == PieceType.QUEEN) {
            // Movimentos como torre
            int[][] straightDirections = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : straightDirections) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (isMoveLegal(board, from, to, color)) {
                        moves.add(to);
                    }
                    if (board.containsKey(to)) {
                        break;
                    }
                }
            }
            // Movimentos como bispo
            int[][] diagonalDirections = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] dir : diagonalDirections) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (isMoveLegal(board, from, to, color)) {
                        moves.add(to);
                    }
                    if (board.containsKey(to)) {
                        break;
                    }
                }
            }
        } else if (type == PieceType.KNIGHT) {
            // Movimentos em "L": (±2, ±1) e (±1, ±2)
            int[][] knightOffsets = {
                {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
                {1, -2}, {1, 2}, {2, -1}, {2, 1}
            };
            for (int[] offset : knightOffsets) {
                char toCol = (char) (from.charAt(0) + offset[0]);
                int toRow = fromRow + offset[1] + 1;
                if (toCol >= 'a' && toCol <= 'h' && toRow >= 1 && toRow <= 8) {
                    String to = "" + toCol + toRow;
                    Piece target = board.get(to);
                    if ((target == null || target.getColor() != color) && isMoveLegal(board, from, to, color)) {
                        moves.add(to);
                    }
                }
            }
        } else if (type == PieceType.ROOK) {
            // Movimentos em linhas retas: cima, baixo, esquerda, direita
            int[][] straightDirections = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
            for (int[] dir : straightDirections) {
                for (int i = 1; i < 8; i++) {
                    char toCol = (char) (from.charAt(0) + i * dir[0]);
                    int toRow = fromRow + i * dir[1] + 1;
                    if (toCol < 'a' || toCol > 'h' || toRow < 1 || toRow > 8) {
                        break;
                    }
                    String to = "" + toCol + toRow;
                    if (isMoveLegal(board, from, to, color)) {
                        moves.add(to);
                    }
                    if (board.containsKey(to)) {
                        break;
                    }
                }
            }
        }

        System.out.println("Movimentos encontrados para " + type + " em " + from + ": " + moves);
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

    private List<String> getAllPossibleMoves(Long gameId,Map<String, Piece> board, PieceColor color) {
        List<String> allMoves = new ArrayList<>();
        
        // Para cada peça preta no tabuleiro
        for (Map.Entry<String, Piece> entry : board.entrySet()) {
            String from = entry.getKey();
            Piece piece = entry.getValue();
            if (piece.getColor() != color) {
                continue;
            }

            // Calcula movimentos possíveis para a peça
            List<String> moves = getPossibleMovesForPiece(gameId,board, from, piece);
            for (String to : moves) {
                String notation = generateMoveNotation(from, to, piece, board);
                allMoves.add(notation);
            }
        }

        return allMoves;
    }

    private List<String> getPossibleMovesForPiece(Long gameId,Map<String, Piece> board, String from, Piece piece) {
        List<String> moves = new ArrayList<>();
        int fromCol = from.charAt(0) - 'a';
        int fromRow = Character.getNumericValue(from.charAt(1));
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
                    if (!board.containsKey(oneStep) && !board.containsKey(twoStep)) {
                        moves.add(twoStep);
                    }
                }
                // Capturas
                for (int colOffset : new int[]{-1, 1}) {
                    int newCol = fromCol + colOffset;
                    if (newCol >= 0 && newCol < 8) {
                        String capture = String.valueOf((char) ('a' + newCol)) + (fromRow + direction);
                        if (board.containsKey(capture) && board.get(capture).getColor() != color) {
                            moves.add(capture);
                        }
                    }
                }
                // En passant (simplificado, reutiliza lógica existente)
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
                    if (newRow >= 1 && newRow <= 8 && newCol >= 0 && newCol < 8) {
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
                        if (newRow >= 1 && newRow <= 8 && newCol >= 0 && newCol < 8) {
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
                        if (newRow < 1 || newRow > 8 || newCol < 0 || newCol > 8) break;
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

    

}
