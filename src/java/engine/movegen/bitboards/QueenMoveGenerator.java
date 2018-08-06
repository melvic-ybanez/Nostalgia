package java.engine.movegen.bitboards;

import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Created by melvic on 7/30/18.
 */
public interface QueenMoveGenerator extends BishopMoveGenerator, RookMoveGenerator {
    public default Stream<BiFunction<Integer, Long, Long>> getAllMoves() {
        return Stream.concat(BishopMoveGenerator.super.getAllMoves(), RookMoveGenerator.super.getAllMoves());
    }
}
