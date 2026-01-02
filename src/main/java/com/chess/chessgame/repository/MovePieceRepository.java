package com.chess.chessgame.repository;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.chess.chessgame.model.MovePiece;
public interface MovePieceRepository extends JpaRepository<MovePiece, Long> {
    //@Query("SELECT mp FROM MovePiece mp WHERE mp.type = :type and mp.color = :color")
    //List<MovePiece> findMovePieceByPiece(@Param("type") PieceType type, @Param("color") PieceColor color);
    //@Query("SELECT mp FROM MovePiece mp WHERE mp.mov = :from")
    //List<MovePiece> findMovePieceByFrom(@Param("from") String from);
//@Query("SELECT mp FROM MovePiece mp WHERE mp.mov = :fromPos ORDER BY mp.id DESC")
    //MovePiece findMovePieceByFrom(@Param("fromPos") String fromPos, Pageable pageable);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove AND mp2.toPiece =:fromPos)")
    MovePiece findMovePieceByToEqualsFrom(@Param("isLastMove") Boolean isLastMove, @Param("fromPos") String fromPos);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.toPiece IS NULL AND mp2.lastMove = :isLastMove AND mp2.fromPiece =:fromPos)")
    MovePiece findMovePieceByToEqualsFromLastMoveTrue(@Param("isLastMove") Boolean isLastMove, @Param("fromPos") String fromPos);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.fromPiece = :fromPos AND mp2.lastMove = :isLastMove)")
    MovePiece findMovePieceByToEqualsFrom(@Param("fromPos") String fromPos, @Param("isLastMove") Boolean isLastMove);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.toPiece = :fromPos AND mp2.lastMove = :isLastMove and mp2.colorPiece =:color)")
    MovePiece findMovePieceByToCaptureTo(@Param("fromPos") String fromPos, @Param("isLastMove") Boolean isLastMove , @Param("color") String color);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.fromPiece = :fromPos AND mp2.lastMove = :isLastMove and mp2.colorPiece =:color AND mp2.toPiece IS NULL)")
    MovePiece findMovePieceByToCaptureFrom(@Param("fromPos") String fromPos, @Param("isLastMove") Boolean isLastMove , @Param("color") String color);
@Query("SELECT mp FROM MovePiece mp WHERE mp.lastMove = :isLastMove")
    List<MovePiece> findMovePieceByLastMoveTrue(@Param("isLastMove") Boolean isLastMove);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.ID) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove and mp2.colorPiece =:color)")
    List<MovePiece> findMovePieceByPieceColorLastMoveTrueLast(@Param("isLastMove") Boolean isLastMove, @Param("color") String color);

//Seleciona as peças ativas de um cor
@Query("SELECT mp FROM MovePiece mp WHERE mp.lastMove = :isLastMove and mp.colorPiece =:color")
List<MovePiece> findMovePieceByPieceColorLastMoveTrue(@Param("isLastMove") Boolean isLastMove, @Param("color") String color);

@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MIN(mp2.ID) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove and mp2.typePiece =:typePeca and mp2.fromPiece =:fromPeca)")
    MovePiece findMovePieceByLastMoveTruePieceTypeFrom(@Param("isLastMove") Boolean isLastMove, @Param("typePeca") String TypePeca, @Param("fromPeca") String fromPeca);
//@Query("DELETE mp FROM MovePiece mp WHERE mp.lastMove = :isLastMove")
    //List<MovePiece> deleteMovePieceByLastMoveFalse(@Param("isLastMove") Boolean isLastMove);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove AND mp2.toPiece IS NULL AND mp2.typePiece =: tipoPeca and mp2.codigoPiece =: cod)")
MovePiece findMovePieceByToPieceIsNullLastMoveTrue(@Param("isLastMove") Boolean isLastMove, @Param("tipoPeca") String tipoPeca, @Param("cod") Integer cod);
//@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove AND mp2.colorPiece = :color AND :targetPosition MEMBER OF mp2.moveAttack)")
//MovePiece findMovePieceByPieceTypeLastMoveTrue(@Param("isLastMove") Boolean isLastMove, @Param("color") String color, @Param("targetPosition") String targetPosition);
@Query("SELECT mp2 FROM MovePiece mp2 WHERE mp2.fromPiece IS NULL OR mp2.fromPiece =: fromPeca")
List<MovePiece> findMovePieceByFromOrFromIsNull(@Param("fromPeca") String fromPeca);
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove AND mp2.toPiece IS NULL AND mp2.typePiece =: tipoPeca and mp2.codigoPiece =: cod)")
MovePiece findMovePieceByFromPieceIsNullLastMoveTrue(@Param("isLastMove") Boolean isLastMove, @Param("tipoPeca") String tipoPeca, @Param("cod") Integer cod);
@Query("SELECT mp2 FROM MovePiece mp2 WHERE SUBSTRING(mp2.fromPiece, 2, 1) = :endPosition")
List<MovePiece> findMovePieceByEndPosition(@Param("endPosition") String endPosition);

//Seleciona o último movimento de uma peça específica para atualizar a lista do moveAttack
@Query("SELECT mp FROM MovePiece mp WHERE mp.id = (SELECT MAX(mp2.id) FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove AND mp2.typePiece =:tipoPeca and mp2.codigoPiece =:cod and mp2.colorPiece =:color)")
MovePiece findMovePieceByTypePieceLastMoveTrueCodigoPieceColorPiece(@Param("isLastMove") Boolean isLastMove, @Param("tipoPeca") String tipoPeca, @Param("cod") Integer cod, @Param("color") String color);

//Seleciona peças pretas que contenham na lista de casas disponíveis para movimentação o TO de peças adversárias c/ 3 parâmetros
@Query("SELECT mp FROM MovePiece mp WHERE mp.lastMove = :lastMovePiece AND mp.colorPiece = :color AND :targetPosition MEMBER OF mp.moveAttack")
List<MovePiece> findMovePieceByPieceColorInMoveAttckLastMoveTrue(@Param("lastMovePiece") Boolean lastMovePiece, @Param("color") String color, @Param("targetPosition") String targetPosition);

@Query("SELECT mp FROM MovePiece mp WHERE mp.lastMove = :isLastMove AND mp.colorPiece = :color and :targetPosition MEMBER OF mp.moveAttack")
    List<MovePiece> findMovePieceByPieceColorInMoveAttckLastMoveTrueDiffTo(@Param("isLastMove") Boolean isLastMove, @Param("color") String color , @Param("targetPosition") String targetPosition);


//@Query("SELECT mp FROM MovePiece mp WHERE mp.lastMove = :isLastMove AND mp.colorPiece = :color and mp.toPiece !=:fromPeca AND :targetPosition MEMBER OF mp.moveAttack")
//List<MovePiece> findMovePieceByPieceColorInMoveAttckLastMoveTrueDiffTo(@Param("isLastMove") Boolean isLastMove, @Param("color") String color , @Param("fromPeca") String fromPeca , @Param("targetPosition") String targetPosition);


//@Query("SELECT mp2 FROM MovePiece mp2 WHERE mp2.lastMove = :isLastMove and mp2.typePiece =:typePeca and mp2.fromPiece =:fromPeca and mp2.typePiece != :differentType")
//MovePiece findMovePieceByLastMoveTruePieceTypeFrom(@Param("isLastMove") Boolean isLastMove, @Param("typePeca") String TypePeca, @Param("fromPeca") String fromPeca, @Param("differentType") String differentType);

//Seleciona peças pretas que contenham na lista de casas disponíveis para movimentação o TO de peças adversárias c/ 4 parâmetros
//@Query("SELECT mp FROM MovePiece mp WHERE mp.typePiece != :differentType AND mp.lastMove = :isLastMove AND mp.colorPiece = :color AND :targetPosition MEMBER OF mp.moveAttack")
//List<MovePiece> findMovePieceByPieceColorInMoveAttckLastMoveTrue(@Param("differentType") String differentType, @Param("isLastMove") Boolean isLastMove, @Param("color") String color, @Param("targetPosition") String targetPosition);

//@Query("SELECT mp FROM MovePiece mp WHERE :targetPosition MEMBER OF (SELECT mp2.moveAttack FROM MovePiece mp2 WHERE mp2.typePiece != :differentType AND mp2.lastMove = :isLastMove AND mp2.colorPiece = :color)")
    //List<MovePiece> findMovePieceByPieceColorInMoveAttckLastMoveTrueDiffPieceType(@Param("differentType") String differentType, @Param("isLastMove") Boolean isLastMove, @Param("color") String color, @Param("targetPosition") String targetPosition);



//Nova query: Encontrar peças que atacam uma posição específica (para counterattack)
@Query("SELECT mp FROM MovePiece mp WHERE mp.colorPiece = :color AND :position MEMBER OF mp.moveAttack AND mp.lastMove = :isLastMove")
List<MovePiece> findPiecesAttackingPosition(@Param("color") String color, @Param("position") String position, @Param("isLastMove") Boolean isLastMove);

// Nova query: Encontrar peças que podem ser perdidas após captura (baseado em valuePiece > threshold)
@Query("SELECT mp FROM MovePiece mp WHERE mp.colorPiece = :color AND mp.valuePiece > :threshold AND mp.lastMove = :isLastMove")
List<MovePiece> findHighValuePieces(@Param("color") String color, @Param("threshold") Integer threshold, @Param("isLastMove") Boolean isLastMove);

//Nova query: Verificar se existe peça na posição to
@Query("SELECT mp2.valuePiece FROM MovePiece mp2 WHERE mp2.id = (SELECT MAX(mp.id) FROM MovePiece mp WHERE mp.toPiece > :toPeca AND mp.lastMove = :isLastMove)")
MovePiece findValuePieceToPiece(@Param("toPeca") String toPeca, @Param("isLastMove") Boolean isLastMove);

//Nova query: Verificar se existe peça na posição from
@Query("SELECT mp2.valuePiece FROM MovePiece mp2 WHERE mp2.id = (SELECT MAX(mp.id) FROM MovePiece mp WHERE mp.fromPiece > :toPeca AND mp.lastMove = :isLastMove)")
MovePiece findValuePieceFromPiece(@Param("toPeca") String toPeca, @Param("isLastMove") Boolean isLastMove);


//Seleciona a peça que está em uma casa específica toPiece or fromPiece em caso de ativa.
@Query("SELECT mp2 FROM MovePiece mp2 WHERE mp2.id = (SELECT MAX(mp.id) FROM MovePiece mp WHERE COALESCE(mp.toPiece, mp.fromPiece) = :peca AND mp.lastMove = :isLastMove)")
MovePiece findValuePieceFromPieceNew(@Param("peca") String peca, @Param("isLastMove") Boolean isLastMove);

//Seleciona a peça que está em uma casa específica toPiece or fromPiece em caso de ativa e com color do oponente.
@Query("SELECT mp2 FROM MovePiece mp2 WHERE mp2.id = (SELECT MAX(mp.id) FROM MovePiece mp WHERE COALESCE(mp.toPiece, mp.fromPiece) = :peca AND mp.lastMove = :isLastMove AND mp.colorPiece =:color)")
MovePiece findValuePieceFromPieceByColor(@Param("peca") String peca, @Param("isLastMove") Boolean isLastMove, @Param("color") String color);

//Seleciona a peça de menor valor que esteja mais próxima das linhas iniciais do seu lado do tabuleiro no campo toPiece or fromPiece em caso de ativa.
@Query("SELECT mp FROM MovePiece mp WHERE mp.valuePiece = (SELECT MIN(mp2.valuePiece) FROM MovePiece mp2 WHERE SUBSTRING(COALESCE(mp2.toPiece, mp2.fromPiece), 2, 1) = :linha AND mp2.lastMove = :isLastMove) AND SUBSTRING(COALESCE(mp.toPiece, mp.fromPiece), 2, 1) = :linha AND mp.lastMove = :isLastMove")
MovePiece findMinValuePieceByLine(@Param("linha") String linha,@Param("isLastMove") Boolean isLastMove);

//Seleciona a peça de menor valor que esteja mais próxima das linhas iniciais do seu lado do tabuleiro no campo toPiece or fromPiece em caso de ativa e colocando a opção da cor de peça.
@Query("SELECT mp FROM MovePiece mp WHERE mp.valuePiece = (SELECT MIN(mp2.valuePiece) FROM MovePiece mp2 WHERE SUBSTRING(COALESCE(mp2.toPiece, mp2.fromPiece), 2, 1) = :linha AND mp2.colorPiece = :color AND mp2.lastMove = :isLastMove) AND SUBSTRING(COALESCE(mp.toPiece, mp.fromPiece), 2, 1) = :linha AND mp.colorPiece = :color AND mp.lastMove = :isLastMove")
List<MovePiece> findMinValuePieceByLineAndColor(@Param("linha") String linha, @Param("color") String color, @Param("isLastMove") Boolean isLastMove);


//Seleciona a peça em uma linha específica com o campo toPiece is null e fromPiece específico com casa ativa e colocando a opção da cor de peça.
@Query("SELECT mp FROM MovePiece mp WHERE SUBSTRING(mp.fromPiece, 2, 1) = :linha AND mp.colorPiece = :color AND mp.lastMove = :isLastMove AND mp.toPiece IS NULL AND mp.fromPiece =:fromPeca AND mp.typePiece =:typePeca")
MovePiece findPieceByLineAndColorAndFromPieceAndTypePiece(@Param("linha") String linha, @Param("color") String color, @Param("isLastMove") Boolean isLastMove, @Param("fromPeca") String fromPeca , @Param("typePeca") String typePeca);

//Seleciona a peça em uma linha específica com o campo toPiece is null e fromPiece específico com casa ativa e colocando a opção da cor de peça.
@Query("SELECT mp FROM MovePiece mp WHERE SUBSTRING(COALESCE(mp.toPiece, mp.fromPiece), 2, 1) =:linha AND mp.colorPiece =:color AND mp.lastMove =:isLastMove AND COALESCE(mp.toPiece, mp.fromPiece) =:fromPeca")
MovePiece findPieceByLineAndColorAndFromPiece(@Param("linha") String linha, @Param("color") String color, @Param("isLastMove") Boolean isLastMove, @Param("fromPeca") String fromPeca);

@Query("SELECT mp FROM MovePiece mp " +
	       "WHERE mp.toPiece IS NULL " +
	       "AND mp.fromPiece = :fromPeca " +
	       "AND UPPER(mp.colorPiece) = UPPER(:color) " +
	       "AND mp.lastMove = :isLastMove")
	MovePiece findPieceByFromPieceAndColorAndLastMove(
	      @Param("fromPeca") String fromPeca,
	      @Param("color") String color,
	      @Param("isLastMove") boolean isLastMove);


//Seleciona a peça de menor valor que esteja mais próxima das linhas iniciais do seu lado do tabuleiro no campo toPiece or fromPiece em caso de ativa e colocando a opção da cor de peça.
//@Query("SELECT mp FROM MovePiece mp WHERE mp.codigoPiece = (SELECT MAX(mp2.codigoPiece) FROM MovePiece mp2 WHERE mp2.colorPiece = :color AND mp2.lastMove = :isLastMove AND mp2.typePiece = :typePiece)")
//MovePiece findMaxCodigoValuePieceByLineAndColor(@Param("color") String color, @Param("isLastMove") Boolean isLastMove, @Param("typePiece") String typePiece);

//Seleciona a peça de menor valor que esteja mais próxima das linhas iniciais do seu lado do tabuleiro no 
//campo toPiece or fromPiece em caso de ativa e colocando a opção da cor de peça e o tipo de peça.
@Query("SELECT mp FROM MovePiece mp " +
	       "WHERE mp.valuePiece = (" +
	       "   SELECT MIN(mp2.valuePiece) FROM MovePiece mp2 " +
	       "   WHERE SUBSTRING(COALESCE(mp2.toPiece, mp2.fromPiece), 2, 1) = :linha " +
	       "   AND mp2.colorPiece = :color " +
	       "   AND mp2.lastMove = :isLastMove " +
	       "   AND mp2.typePiece = :typePiece" +
	       ") " +
	       "AND SUBSTRING(COALESCE(mp.toPiece, mp.fromPiece), 2, 1) = :linha " +
	       "AND mp.colorPiece = :color " +
	       "AND mp.lastMove = :isLastMove " +
	       "AND mp.typePiece = :typePiece")
	List<MovePiece> findMinValuePieceByLineAndColorAndType(
	        @Param("linha") String linha,
	        @Param("color") String color,
	        @Param("isLastMove") Boolean isLastMove,
	        @Param("typePiece") String typePiece);




@Query("SELECT mp FROM MovePiece mp " +
	       "WHERE mp.colorPiece = :color " +
	       "AND mp.lastMove = :isLastMove " +
	       "AND mp.typePiece = :typePiece " +
	       "ORDER BY mp.codigoPiece DESC LIMIT 1")
	MovePiece findMaxCodigoValuePieceByLineAndColor(@Param("color") String color,
	                                                @Param("isLastMove") Boolean isLastMove,
	                                                @Param("typePiece") String typePiece);

@Query("SELECT mp FROM MovePiece mp " +
	       "WHERE mp.valuePiece = (" +
	       "   SELECT MAX(mp2.valuePiece) FROM MovePiece mp2 " +
	       "   WHERE mp2.colorPiece = :color " +
	       "   AND mp2.lastMove = :isLastMove " +
	       "   AND mp2.moveAttack LIKE %:attack%" +
	       ") " +
	       "AND mp.colorPiece = :color " +
	       "AND mp.lastMove = :isLastMove " +
	       "AND mp.moveAttack LIKE %:attack%")
	List<MovePiece> findMaxValuePieceByColorAndAttack(
	        @Param("color") String color,
	        @Param("isLastMove") Boolean isLastMove,
	        @Param("attack") String attack);






    @Query("SELECT COUNT(mp) FROM MovePiece mp " +
           "WHERE mp.typePiece = :typePiece " +
           "AND mp.colorPiece = :colorPiece " +
           "AND mp.codigoPiece = :codigoPiece")
    Integer countPieceByTypeColorAndCodigo(@Param("typePiece") String typePiece,
                                        @Param("colorPiece") String colorPiece,
                                        @Param("codigoPiece") Integer codigoPiece);


    @Query("SELECT mp FROM MovePiece mp " +
    	       "WHERE mp.toPiece IS NULL " +
    	       "AND SUBSTRING(mp.fromPiece, 2, 1) = :linha " +
    	       "AND mp.colorPiece = :color " +
    	       "AND mp.lastMove = :isLastMove " +
    	       "AND mp.fromPiece = :fromPeca")
    	MovePiece findPieceByLineAndColorAndFromPiece(@Param("linha") String linha,
    	                                              @Param("color") String color,
    	                                              @Param("isLastMove") boolean isLastMove,
    	                                              @Param("fromPeca") String fromPeca);



}