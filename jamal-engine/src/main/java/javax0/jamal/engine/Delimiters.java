package javax0.jamal.engine;

/**
 * See {@link javax0.jamal.api.Delimiters}.
 * <p>
 * This implementation stores the macro opening and closing Strings as {@link String} fields.
 */
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
