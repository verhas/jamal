package javax0.jamal.api;

public interface UserDefinedMacro {
    String getId();
    String evaluate(String... actualValues) throws BadSyntax;
}
