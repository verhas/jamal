package javax0.jamal.api;

/**
 * Something that can be evaluated. The evaluation may use strings as arguments. Typically user defined macros and
 * scripts are evaluable.
 */
public interface Evaluable {
    String evaluate(String... actualValues) throws BadSyntax;
}
