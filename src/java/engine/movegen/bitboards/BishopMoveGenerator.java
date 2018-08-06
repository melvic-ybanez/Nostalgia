package java.engine.movegen.bitboards;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/29/18.
 */
public interface BishopMoveGenerator extends SlidingMoveGenerator {
    public default Stream<BiFunction<Integer, Long, Long>> getAllMoves() {
        return Stream.of(this::positiveDiagonal,
                this::negativeDiagonal,
                this::positiveAntiDiagonal,
                this::negativeAntiDiagonal);
    }

    public default long positiveDiagonal(int bishopPosition, long occupied) {
        return ray(bishopPosition, occupied, this::positiveSlide, this::getDiagonalMask);
    }

    public default long negativeDiagonal(int bishopPosition, long occupied) {
        return ray(bishopPosition, occupied, this::negativeSlide, this::getDiagonalMask);
    }

    public default long positiveAntiDiagonal(int bishopPosition, long occupied) {
        return ray(bishopPosition, occupied, this::positiveSlide, this::getAntiDiagonalMask);
    }

    public default long negativeAntiDiagonal(int bishopPosition, long occupied) {
        return ray(bishopPosition, occupied, this::negativeSlide, this::getAntiDiagonalMask);
    }
}
