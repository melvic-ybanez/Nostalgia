package java.engine.utils;

/**
 * Created by melvic on 8/4/18.
 */
public interface TriFunction<A, B, C, D> {
    public D apply(A a, B b, C c);
}
