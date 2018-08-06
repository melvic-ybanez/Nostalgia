package java.engine.board.bitboards.transformers;

/**
 * Created by melvic on 7/13/18.
 */
public class Rotate180 implements Transformation {
    @Override
    public long apply (long bitboard) {
        return new VerticalFlip().apply(
                new HorizontalMirror().apply(bitboard)
        );
    }
}
