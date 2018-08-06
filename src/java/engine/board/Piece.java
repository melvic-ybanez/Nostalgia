package java.engine.board;

/**
 * Created by melvic on 6/27/18.
 */
public interface Piece {
    public enum Type {
        PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING;
    }
    public enum Side {
        WHITE, BLACK;

        public Side opposite() {
            return values()[ordinal() ^ 1];
        }
    }

    public Type getType();
    public Side getSide();

    class Builder {
        private Side side;

        Builder(Side side) {
            this.side = side;
        }

        public Piece of(Type type) {
            return new Piece() {
                @Override
                public Type getType() {
                    return type;
                }

                @Override
                public Side getSide() {
                    return side;
                }

                @Override
                public String toString() {
                    return getSide() + " " + getType();
                }

                @Override
                public boolean equals(Object obj) {
                    if (!(obj instanceof Piece)) return false;
                    Piece that = (Piece) obj;
                    return this.getType() == that.getType()
                            && this.getSide() == that.getSide();
                }
            };
        }
    }

    public static Builder build(Side side) {
        return new Builder(side);
    }
    public static Builder white = new Builder(Side.WHITE);
    public static Builder black = new Builder(Side.BLACK);
}
