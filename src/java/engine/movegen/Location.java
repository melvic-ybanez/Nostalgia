package java.engine.movegen;

import java.engine.board.Board;

import static java.engine.board.Board.SIZE;

/**
 * Created by melvic on 7/3/18.
 */
public interface Location {
    public enum File { A, B, C, D, E, F, G, H }
    public enum Rank { _1, _2, _3, _4, _5, _6, _7, _8 }

    public File getFile();
    public Rank getRank();

    public static Location of(File file, Rank rank) {
        return new Location() {
            @Override
            public File getFile() {
                return file;
            }

            @Override
            public Rank getRank() {
                return rank;
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof Location)) return false;
                Location location = (Location) obj;
                return location.getFile().equals(getFile())
                        && location.getRank().equals(getRank());
            }

            @Override
            public String toString() {
                return getFile().toString() + getRank().toString();
            }
        };
    }

    public static Location of(int row, int col) {
        File file = File.values()[col];
        Rank rank = Rank.values()[SIZE - 1 - row];
        return Location.of(file, rank);
    }

    public static Location of(int position) {
        return of(position / Board.SIZE, position % Board.SIZE);
    }
}
