package java.engine.board.bitboards.transformers;

/**
 * Created by melvic on 7/13/18.
 */
public class VerticalFlip implements Transformation {
    @Override
    public long apply(long bitboard) {
        long singleRowMask = 0x00ff00ff00ff00ffL;
        long doubleRowMask = 0x0000ffff0000ffffL;

        // shift even ranks downward, odd ranks upward, and combine the result
        long flippedBoard = ((bitboard >> 8) & singleRowMask) | ((bitboard & singleRowMask) << 8);

        // do the same thing with the previous result, but shift twice as much
        flippedBoard = ((flippedBoard >> 16) & doubleRowMask) | ((flippedBoard & doubleRowMask) << 16);

        // do the same thing with the previous result, but shift twice as much (four times the original)
        return (flippedBoard >> 32) | (flippedBoard << 32);
    }
}
