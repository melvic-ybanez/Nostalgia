package java.engine.movegen.bitboards;

import java.engine.board.bitboards.Bitboard;
import java.engine.board.Piece;
import java.engine.utils.TriFunction;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/27/18.
 */
public interface PawnMoveGenerator extends BitboardMoveGenerator, WithOneStep {
    public default long singlePush(long pawns, long emptySquares, Piece.Side sideToMove) {
        // Move north first,
        long north = getOneStep().north(pawns);

        // then south twice, if piece is black (ordinal = 1).
        long optionalTwoRows = sideToMove.ordinal() << 4;       // 2 ^ 4 = 16 (bits) = 2 rows
        long board = north >> optionalTwoRows;

        // And make sure you land on an empty square.
        return board & emptySquares;
    }

    public default long doublePush(long pawns, long emptySquares, Piece.Side sideToMove) {
        long[] destinationMasks = {
                0x00000000FF000000L,    // rank 4 (white's double push destination)
                0x000000FF00000000L     // rank 5 (black's double push destination)
        };

        long pushedPawns = singlePush(pawns, emptySquares, sideToMove);
        return singlePush(pushedPawns, emptySquares, sideToMove)
                & destinationMasks[sideToMove.ordinal()];
    }

    public default long attack(
            long pawns, long opponents, Piece.Side sideToMove,
            Function<Long, Long> northAttack, Function<Long, Long> southAttack) {
        Function<Long, Long> step = sideToMove == Piece.Side.WHITE?
                northAttack : southAttack;
        return step.apply(pawns) & opponents;
    }

    public default long attackEast(long pawns, long opponents, Piece.Side sideToMove) {
        return attack(pawns, opponents, sideToMove, getOneStep()::northEast, getOneStep()::southEast);
    }

    public default long attackWest(long pawns, long opponents, Piece.Side sideToMove) {
        return attack(pawns, opponents, sideToMove, getOneStep()::northWest, getOneStep()::southWest);
    }

    @Override
    public default Stream<Long> generateAllMoves(Bitboard bitboard, int source, Piece.Side sideToMove) {
        return Stream.concat(
                generatePushes(bitboard, source, sideToMove),
                generateAttacks(bitboard, source, sideToMove));
    }

    public default Stream<Long> generatePushes(Bitboard bitboard, int source, Piece.Side sideToMove) {
        return generatePawnMoves(bitboard, source, sideToMove, bitboard::getEmptySquares, this::getPushMoves);
    }

    @Override
    public default Stream<Long> generateAttacks(Bitboard bitboard, int source, Piece.Side sideToMove) {
        return generatePawnMoves(bitboard, source, sideToMove,
                () -> bitboard.getSideBoard(sideToMove.opposite()), this::getAttackMoves);
    }

    public default Stream<Long> generatePawnMoves(
            Bitboard bitboard,
            int source,
            Piece.Side sideToMove,
            Supplier<Long> targetSquaresSupplier,
            Supplier<Stream<TriFunction<Long, Long, Piece.Side, Long>>> generator) {
        long pawns = Bitboard.singleBitset(source);
        long targetSquares = targetSquaresSupplier.get();

        return generator.get()
                .map(move -> move.apply(pawns, targetSquares, sideToMove))
                .filter(Bitboard::isNonEmptySet);
    }

    public default Stream<TriFunction<Long, Long, Piece.Side, Long>> getPushMoves() {
        return Stream.of(this::singlePush, this::doublePush);
    }

    public default Stream<TriFunction<Long, Long, Piece.Side, Long>> getAttackMoves() {
        return Stream.of(this::attackEast, this::attackWest);
    }
}
