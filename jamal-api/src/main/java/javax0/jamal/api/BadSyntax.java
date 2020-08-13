package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

/**
 * This exception, as the name suggest, is thrown when the processor or a macro finds something it cannot interpret.
 * This exception is always captured inside Jamal and then rethrown as a {@link BadSyntaxAt} exception adding the line
 * reference information. This exception is used at code locations where it is not possible to identify the actual
 * input location where the erroneous syntax started. See also {@link BadSyntaxAt}.
 */
public class BadSyntax extends Exception {
    public BadSyntax() {

    }

    public BadSyntax(String message) {
        super(message);
    }

    public BadSyntax(String message, Throwable cause) {
        super(message, cause);
    }

    final private List<String> parameters = new ArrayList<>();

    public List<String > getParameters(){
        return parameters;
    }

    public BadSyntax parameter(String param) {
        parameters.add(param);
        return this;
    }

    private static String abbreviate(String longParam) {
        if (longParam.length() > 60) {
            return longParam.substring(0, 60) + "...";
        }
        return longParam;
    }

    @Override
    public String getMessage() {
        return super.getMessage() + "\n" +
            parameters.stream()
                .map(BadSyntax::abbreviate)
                .map(m -> ">>>" + m + "\n")
                .collect(joining());
    }
}
