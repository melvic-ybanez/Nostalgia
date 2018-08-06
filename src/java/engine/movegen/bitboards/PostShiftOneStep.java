package java.engine.movegen.bitboards;

import java.engine.board.Board;

/**
 * Created by melvic on 7/14/18.
 */
public class PostShiftOneStep implements OneStep {
    @Override
    public long east(long bitboard) {
        return (bitboard << 1) & Masks.NOT_A_FILE;
    }

    @Override
    public long west(long bitboard) {
        return (bitboard >>> 1) & Masks.NOT_H_FILE;
    }

    @Override
    public long north(long bitboard) {
        return bitboard << Board.SIZE;
    }

    @Override
    public long south(long bitboard) {
        return bitboard >>> Board.SIZE;
    }

    @Override
    public long northEast(long bitboard) {
        return (bitboard << Board.SIZE + 1) & Masks.NOT_A_FILE;
    }

    @Override
    public long northWest(long bitboard) {
        return (bitboard << Board.SIZE - 1) & Masks.NOT_H_FILE;
    }

    @Override
    public long southEast(long bitboard) {
        return (bitboard >>> Board.SIZE - 1) & Masks.NOT_A_FILE;
    }

    @Override
    public long southWest(long bitboard) {
        return (bitboard >>> Board.SIZE + 1) & Masks.NOT_H_FILE;
    }
}
