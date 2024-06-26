package javax0.jamal.api;

/**
 * An {@link Evaluable} that is verbatim.
 * This means that the result of the evaluation will not be further evaluated.
 */
public interface EvaluableVerbatim extends Evaluable {
    default boolean isVerbatim() {
        return true;
    }
}
