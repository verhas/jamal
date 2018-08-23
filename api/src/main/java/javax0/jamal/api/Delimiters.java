package javax0.jamal.api;

public interface Delimiters {
    String open();
    String close();
    void separators(String openDelimiter, String closeDelimiter) throws BadSyntax;
}
