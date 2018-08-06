package java.engine.board.bitboards.transformers;

/**
 * Created by melvic on 7/13/18.
 */
public class HorizontalMirror implements Transformation {
    @Override
    public long apply(long bitboard) {
        long singleColMask = 0x5555555555555555L;
        long doubleColMask = 0x3333333333333333L;
        long quadColMask = 0x0f0f0f0f0f0f0f0fL;

        // shift even files to the left, odd files to the right, and combine the results
        long mirroredBoard = ((bitboard >> 1) & singleColMask) | ((bitboard & singleColMask) << 1);

        // do the same thing with the previous result, but shift twice as much
        mirroredBoard = ((mirroredBoard >> 2) & doubleColMask) | ((mirroredBoard & doubleColMask) << 2);

        // do the same thing with the previous result, but shift twice as much (four times the original)
        return ((mirroredBoard >> 4) & quadColMask) |((mirroredBoard & quadColMask) << 4);
    }
}
