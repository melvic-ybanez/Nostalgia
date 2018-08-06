package java.engine.movegen.bitboards;

import java.engine.utils.TriFunction;

import java.util.stream.Stream;

/**
 * Created by melvic on 7/28/18.
 */
public interface KnightMoveGenerator extends NonSlidingMoveGenerator, WithOneStep {
    @Override
    public default Stream<TriFunction<Long, Long, Long, Long>> getAllMoves() {
        return Stream.of(this::northEastEast,
                this::northNorthEast, this::northNorthWest, this::northWestWest,
                this::southWestWest, this::southSouthWest, this::southSouthEast,
                this::southEastEast);
    }

    public default long northNorthEast(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().northEast(getOneStep().north(knights)));
    }

    public default long northNorthWest(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().northWest(getOneStep().north(knights)));
    }

    public default long northEastEast(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().east(getOneStep().northEast(knights)));
    }

    public default long northWestWest(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().west(getOneStep().northWest(knights)));
    }

    public default long southEastEast(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().east(getOneStep().southEast(knights)));
    }

    public default long southWestWest(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().west(getOneStep().southWest(knights)));
    }

    public default long southSouthEast(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().southEast(getOneStep().south(knights)));
    }

    public default long southSouthWest(long knights, long emptySquares, long opponents) {
        return apply(emptySquares, opponents,
                () -> getOneStep().southWest(getOneStep().south(knights)));
    }
}
