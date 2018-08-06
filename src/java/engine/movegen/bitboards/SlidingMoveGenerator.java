package java.engine.movegen.bitboards;

import java.engine.board.Board;
import java.engine.board.bitboards.Bitboard;
import java.engine.board.bitboards.transformers.Rotate180;
import java.engine.board.bitboards.transformers.Transformation;
import java.engine.board.Piece;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/29/18.
 */
public interface SlidingMoveGenerator extends BitboardMoveGenerator {
    class Masks {
        static final long[] FILES = {
                0x0101010101010101L,
                0x0202020202020202L,
                0x0404040404040404L,
                0x0808080808080808L,
                0x1010101010101010L,
                0x2020202020202020L,
                0x4040404040404040L,
                0x8080808080808080L
        };

        static final long[] RANKS = {
                0x00000000000000ffL,
                0x000000000000ff00L,
                0x0000000000ff0000L,
                0x00000000ff000000L,
                0x000000ff00000000L,
                0x0000ff0000000000L,
                0x00ff000000000000L,
                0xff00000000000000L
        };

        static final long[] DIAGONALS = new long[Board.SIZE * Board.SIZE];
        static final long[] ANTI_DIAGONALS = new long[DIAGONALS.length];

        // TODO: Consider optimizing this.
        static void populateDiagonals(long[] diagonals, Function<Long, Long> step) {
            for (int i = 0; i < diagonals.length; i++) {
                long bitboard = Bitboard.singleBitset(i);
                while (!Bitboard.isEmptySet(bitboard)) {
                    diagonals[i] |= bitboard;
                    bitboard = step.apply(bitboard);
                }
            }
        }

        static {
            OneStep oneStep = new PostShiftOneStep();
            populateDiagonals(DIAGONALS, oneStep::northEast);
            populateDiagonals(DIAGONALS, oneStep::southWest);
            populateDiagonals(ANTI_DIAGONALS, oneStep::northWest);
            populateDiagonals(ANTI_DIAGONALS, oneStep::southEast);
        }
    }

    /**
     * Multiply the slider by 2 to move it one step closer to the blocker.
     * Then subtract the product from the board of occupied squares to
     * flip the zeroes between the slider and the blocker to ones (due
     * to borrowing), thereby turning the blocker into zero. Then xor the
     * result with the occupied squares to toggle the bits. The blocker
     * shall be set back to one, the slider to zero, the empty squares
     * between them to ones, and the rest to zeroes.
     */
    public default long positiveSlide(int sliderPosition, long occupied) {
        long slider = Bitboard.singleBitset(sliderPosition);
        return occupied ^ (occupied - 2 * slider);
    }

    /**
     * The "reversed" version of the algorithm for the positive ray.
     */
    public default long negativeSlide(int sliderPosition, long occupied) {
        Transformation reverse = new Rotate180();

        long slider = Bitboard.singleBitset(sliderPosition);
        long reversedSlider = reverse.apply(slider);
        long reversedOccupied = reverse.apply(occupied);

        return occupied ^ reverse.apply(reversedOccupied - 2 * reversedSlider);
    }

    public default long ray(
            int sliderPosition,
            long occupied,
            BiFunction<Integer, Long, Long> slide,
            Function<Integer, Long> masker) {
        long mask = masker.apply(sliderPosition);
        long targetSquares = occupied & mask;
        return slide.apply(sliderPosition, targetSquares) & mask;
    }

    /**
     * Remove any potential allies in the ray, which can only be
     * the blocker (most significant bit), excluding the
     * slider itself.
     */
    public default long excludeAllies(long ray, long slider, long allies) {
        return ray ^ allies - slider;
    }

    public default long getFileMask(int sliderPosition) {
        return Masks.FILES[sliderPosition % Board.SIZE];
    }

    public default long getRankMask(int sliderPosition) {
        return Masks.RANKS[sliderPosition / Board.SIZE];
    }

    public default long getDiagonalMask(int sliderPosition) {
        return Masks.DIAGONALS[sliderPosition];
    }

    public default long getAntiDiagonalMask(int sliderPosition) {
        return Masks.ANTI_DIAGONALS[sliderPosition];
    }

    @Override
    public default Stream<Long> generateAllMoves(Bitboard bitboard, int source, Piece.Side sideToMove) {
        long occupied = bitboard.getOccupied();
        return getAllMoves()
                .map(move -> move.apply(source, occupied))
                .filter(Bitboard::isNonEmptySet);
    }

    public Stream<BiFunction<Integer, Long, Long>> getAllMoves();
}
