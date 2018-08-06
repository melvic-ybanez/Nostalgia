package java.engine.movegen.bitboards;

import java.util.function.Supplier;

/**
 * Created by melvic on 7/29/18.
 */
public interface WithOneStep {
    public default OneStep getOneStep() {
        return new PostShiftOneStep();
    }

    public default long apply(long emptySquares, long opponents, Supplier<Long> move) {
        return move.get() & (emptySquares | opponents);
    }
}
