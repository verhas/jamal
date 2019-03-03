package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.engine.macro.Segment;
import javax0.jamal.engine.macro.TextSegment;
import javax0.jamal.tools.ScriptingTools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static javax0.jamal.tools.ScriptingTools.*;

/**
 * Stores the information about a user defined macro and can also evaluate it using actual parameter string values.
 */
public class UserDefinedMacro implements javax0.jamal.api.UserDefinedMacro {
    final private String id;
    final private String[] parameters;
    final private String content;
    private boolean isScript;
    private String scriptType = null;
    private Segment root = null;

    /**
     * Creates a new user defined macro.
     *
     * @param id         the identifier of the macro. This is the string that stands after the {@code define}
     *                   keyword when the user defined macro is defined. This is a unique identified in the context
     *                   where the macro is reachable and usable.
     * @param content    the text of the macro that stands after the {@code =} character and before the macro closing
     *                   character.
     * @param parameters the names of the parameters. These do not actually need to be real identifiers, alphanumeric
     *                   or something like that. The only requirement is that there is no comma in these names. It is
     *                   recommended though to use usual identifiers.
     * @throws BadSyntax is thrown if one of the parameter names contain another parameter name. This would not be safe
     *                   because this way the result of the macro would be dependent on the evaluation order of
     *                   the parameters.
     */
    public UserDefinedMacro(String id, String content, String... parameters) throws BadSyntax {
        this.isScript = false;
        this.id = id;
        this.content = content;
        this.parameters = parameters;
        ensureSafety();
    }


    /**
     * Set the script type, for example JavaScript, if the user defined macro is defined using some script.
     *
     * @param scriptType the name of the script language that has to be available to the Java run-time.
     */
    @Override
    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
        isScript = true;
    }


    /**
     * Get the name / identifier of the user defined macro.
     *
     * @return the id.
     */
    @Override
    public String getId() {
        return id;
    }

    private void ensureSafety() throws BadSyntax {
        final var badSyntax = new BadSyntax("User defined macro parameter name should not be a substring of another parameter.");
        for (int i = 0; i < parameters.length; i++) {
            for (int j = 0; j < parameters.length; j++) {
                if (i != j) {
                    if (parameters[i].contains(parameters[j])) {
                        badSyntax.parameter("" + i + ". parameter '" + parameters[i] + "' contains the "
                            + j + ". parameter '" + parameters[j] + "'");
                    }
                }
            }
        }
        if (!badSyntax.getParameters().isEmpty()) {
            throw badSyntax;
        }
    }

    /**
     * Evaluate the content of the user defined macro using the actual values for the parameter values.
     *
     * @param actualValues the actual string values for the parameters
     * @return the string that is the result of the evaluation
     * @throws BadSyntaxAt if the user defined macro is a script and the script evaluation throws exception. This
     *                     exception is thrown if the number of the actual values is not the same as the number of the
     *                     parameters.
     */
    @Override
    public String evaluate(String... actualValues) throws BadSyntax {
        if (actualValues.length != parameters.length) {
            var badSyntax = new BadSyntax("Macro '" + id + "' needs " + parameters.length + " arguments and got " + actualValues.length);
            for (final var actual : actualValues) {
                badSyntax.parameter(actual);
            }
            throw badSyntax;
        }
        if (isScript) {
            var engine = getEngine(scriptType);
            for (int i = 0; i < parameters.length; i++) {
                populate(engine, parameters[i], actualValues[i]);
            }
            try {
                return resultToString(ScriptingTools.evaluate(engine, content));
            } catch (Exception e) {
                throw new BadSyntax("Script '" + id + "' threw exception", e);
            }
        } else {
            var values = buildValueMap(actualValues);
            if (root == null) {
                root = new TextSegment(null, content);
                for (int i = 0; i < actualValues.length; i++) {
                    for (Segment segment = root; segment != null; segment = segment.next()) {
                        segment.split(parameters[i]);
                    }
                }
            }
            final var output = new StringBuilder(segmentsLengthSum(root, values));
            for (Segment segment = root; segment != null; segment = segment.next()) {
                if (segment instanceof TextSegment) {
                    output.append(segment.content());
                } else {
                    output.append(values.get(segment.content()));
                }
            }
            return output.toString();
        }
    }

    private Map<String, String> buildValueMap(String[] values) {
        final var map = new HashMap<String, String>(values.length);
        for (int i = 0; i < parameters.length; i++) {
            map.put(parameters[i], values[i]);
        }
        return map;
    }

    private int segmentsLengthSum(Segment root, Map<String, String> values) {
        int size = 0;
        for (Segment segment = root; segment != null; segment = segment.next()) {
            if (segment instanceof TextSegment) {
                size += segment.content().length();
            } else {
                size += values.get(segment.content()).length();
            }
        }
        return size;
    }
}
