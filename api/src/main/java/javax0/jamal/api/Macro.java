package javax0.jamal.api;

public interface Macro {
    String evaluate(StringBuilder input, Processor processor) throws BadSyntax;
    String getId();
}
