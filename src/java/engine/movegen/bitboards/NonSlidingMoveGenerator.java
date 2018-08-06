package java.engine.movegen.bitboards;

import java.engine.board.bitboards.Bitboard;
import java.engine.board.Piece;
import java.engine.utils.TriFunction;

import java.util.stream.Stream;

/**
 * Created by melvic on 7/31/18.
 */
public interface NonSlidingMoveGenerator extends BitboardMoveGenerator {
    @Override
    public default Stream<Long> generateAllMoves(Bitboard bitboard, int source, Piece.Side sideToMove) {
        long pieces = Bitboard.singleBitset(source);
        long emptySquares = bitboard.getEmptySquares();
        long opponents = bitboard.getSideBoard(sideToMove.opposite());

        return getAllMoves()
                .map(move -> move.apply(pieces, emptySquares, opponents))
                .filter(Bitboard::isNonEmptySet);
    }

    public Stream<TriFunction<Long, Long, Long, Long>> getAllMoves();
}
