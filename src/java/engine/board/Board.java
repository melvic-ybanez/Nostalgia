package java.engine.board;

import java.engine.movegen.Location;
import java.engine.movegen.Move;

import java.util.Optional;

/**
 * Created by melvic on 6/26/18.
 */
public interface Board {
    public final static int SIZE = 8;

    public void initialize();
    public Optional<Piece> at(Location location);
    public int evaluate();
    public void updateByMove(Move<Location> move);
    public void clear();

    @Deprecated
    public static Location locate(int row, int col) {
        return Location.of(row, col);
    }

    public default Optional<Piece> at(int row, int col) {
        return at(Location.of(row, col));
    }
}
