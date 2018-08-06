package java.engine.movegen.bitboards;

/**
 * Created by melvic on 7/14/18.
 */
public interface OneStep {
    class Masks {
        public static final long NOT_A_FILE = 0xfefefefefefefefeL;
        public static final long NOT_H_FILE = 0x7f7f7f7f7f7f7f7fL;
        public static final long NOT_AB_FILE = 0xfcfcfcfcfcfcfcfcL;
        public static final long NOT_GH_FILE = 0x3f3f3f3f3f3f3f3fL;
    }

    public long east(long bitboard);
    public long west(long bitboard);
    public long north(long bitboard);
    public long south(long bitboard);
    public long northEast(long bitboard);
    public long northWest(long bitboard);
    public long southEast(long bitboard);
    public long southWest(long bitboard);
}
