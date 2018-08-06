package java.engine.movegen;

/**
 * Created by melvic on 8/4/18.
 */
public interface Move<T> {
    public enum Type { DEFAULT, PAWN_DOUBLE_PUSH }

    public T getSource();
    public T getDestination();

    public default Type getType() {
        return Type.DEFAULT;
    }

    class Builder<T> {
        private T source;

        Builder(T source) {
            this.source = source;
        }

        public Move<T> to(T destination) {
            return new Move<T>() {
                @Override
                public T getSource() {
                    return source;
                }

                @Override
                public T getDestination() {
                    return destination;
                }
            };
        }
    }

    public static <T> Builder<T> from(T source) {
        return new Builder<T>(source);
    }
}
