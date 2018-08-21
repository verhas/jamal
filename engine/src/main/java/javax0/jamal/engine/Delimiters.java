package javax0.jamal.engine;

public class Delimiters implements javax0.jamal.api.Delimiters {

    private String openDelimiter;
    private String closeDelimiter;

    @Override
    public String open() {
        return openDelimiter;
    }

    @Override
    public String close() {
        return closeDelimiter;
    }

    @Override
    public void separators(String openDelimiter, String closeDelimiter) {
        this.openDelimiter = openDelimiter;
        this.closeDelimiter = closeDelimiter;
    }

    @Override
    public String toString() {
        return "{@sep/" + openDelimiter + "/" + closeDelimiter + "}";
    }
}
