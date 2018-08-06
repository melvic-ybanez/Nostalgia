package java.engine.movegen.bitboards;

import java.engine.board.bitboards.Bitboard;
import java.engine.board.Piece;
import java.engine.movegen.Move;
import java.engine.utils.TriFunction;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/30/18.
 */
public interface KingMoveGenerator extends NonSlidingMoveGenerator, WithOneStep {
    @Override
    public default Stream<TriFunction<Long, Long, Long, Long>> getAllMoves() {
        return Stream.of(this::east, this::northEast, this::north, this::northWest,
                this::west, this::southWest, this::south, this::southEast);
    }

    public default long east(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().east(king));
    }

    public default long west(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().west(king));
    }

    public default long north(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().north(king));
    }

    public default long south(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().south(king));
    }

    public default long northEast(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().northEast(king));
    }

    public default long northWest(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().northWest(king));
    }

    public default long southEast(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().southEast(king));
    }

    public default long southWest(long king, long emptySquares, long opponents) {
        return apply(emptySquares, opponents, () -> getOneStep().southWest(king));
    }

    public default boolean isInCheck(Bitboard bitboard, Move<Integer> move, Piece.Side sideToMove) {
        Bitboard movedBitboard = new Bitboard(bitboard, move);
        int kingPosition = Bitboard.getPieceIndex(movedBitboard.getKing(sideToMove));

        Piece.Side opponentSide = sideToMove.opposite();
        long pawns = movedBitboard.getPawns(opponentSide);
        long knights = movedBitboard.getKnights(opponentSide);
        long queen = movedBitboard.getQueen(opponentSide);
        long bishopsOrQueens = movedBitboard.getBishops(opponentSide) | queen;
        long rooksOrQueens = movedBitboard.getRooks(opponentSide) | queen;
        long king = movedBitboard.getKing(opponentSide);

        BiFunction<BitboardMoveGenerator, Long, Optional<Boolean>> check = (generator, pieces) ->
                generator.generateAttacks(movedBitboard, kingPosition, sideToMove)
                    .filter(board -> Bitboard.isNonEmptySet(board & pieces))
                    .findFirst()
                    .map(board -> true);

        return check.apply(new PawnMoveGenerator() {}, pawns)
                .orElseGet(() -> check.apply(new KnightMoveGenerator() {}, knights)
                .orElseGet(() -> check.apply(new BishopMoveGenerator() {}, bishopsOrQueens)
                .orElseGet(() -> check.apply(new RookMoveGenerator() {}, rooksOrQueens)
                .orElseGet(() -> check.apply(new KingMoveGenerator() {}, king)
                        .orElse(false)))));
    }
}
