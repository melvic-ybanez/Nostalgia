package java.engine.movegen.bitboards;

import java.engine.board.bitboards.Bitboard;
import java.engine.board.Piece.Side;

import java.util.stream.Stream;

/**
 * Created by melvic on 7/30/18.
 */
public interface BitboardMoveGenerator {
    public Stream<Long> generateAllMoves(Bitboard bitboard, int source, Side sideToMove);

    public default Stream<Long> generateAttacks(Bitboard bitboard, int source, Side sideToMove) {
        return generateAllMoves(bitboard, source, sideToMove);
    }
}
