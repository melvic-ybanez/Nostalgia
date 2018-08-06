package java.engine.movegen.bitboards;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/29/18.
 */
public interface RookMoveGenerator extends SlidingMoveGenerator {
    public default Stream<BiFunction<Integer, Long, Long>> getAllMoves() {
        return Stream.of(this::north, this::south, this::east, this::west);
    }

    public default long north(int rookPosition, long occupied) {
        return ray(rookPosition, occupied, this::positiveSlide, this::getFileMask);
    }

    public default long south(int rookPosition, long occupied) {
        return ray(rookPosition, occupied, this::negativeSlide, this::getFileMask);
    }

    public default long east(int rookPosition, long occupied) {
        return ray(rookPosition, occupied, this::positiveSlide, this::getRankMask);
    }

    public default long west(int rookPosition, long occupied) {
        return ray(rookPosition, occupied, this::negativeSlide, this::getRankMask);
    }
}
