package javax0.jamal.api;

public interface JShellEngine extends AutoCloseable {
    String evaluate(String input) throws BadSyntax;
    void define(String input) throws BadSyntax;
}
