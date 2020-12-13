package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.tools.ScriptingTools;

import static javax0.jamal.tools.ScriptingTools.getEngine;
import static javax0.jamal.tools.ScriptingTools.populate;
import static javax0.jamal.tools.ScriptingTools.resultToString;

/**
 * Stores the information about a user defined macro and can also evaluate it using actual parameter string values.
 */
public class ScriptMacro implements javax0.jamal.api.ScriptMacro {
    final private String id;
    final private Processor processor;
    final private String content;
    final private String scriptType;
    final ArgumentHandler argumentHandler;
    final private boolean isJShell;

    /**
     * Creates a new user defined macro.
     *
     * @param processor  is the context of the evaluation. Through this object a macro can access the evaluation
     *                   environment.
     * @param id         the identifier of the macro. This is the string that stands after the {@code define} keyword
     *                   when the user defined macro is defined. This is a unique identified in the context where the
     *                   macro is reachable and usable.
     * @param scriptType the type of the script. The scripting engine with this name is used to execute the content. The
     *                   value {@code jamal} means that content has to be interpreted by Jamal itself.
     * @param content    the text of the macro that stands after the {@code =} character and before the macro closing
     *                   character.
     * @param parameters the names of the parameters. These do not actually need to be real identifiers, alphanumeric or
     *                   something like that. The only requirement is that there is no comma in these names. It is
     *                   recommended though to use usual identifiers.
     */
    public ScriptMacro(Processor processor, String id, String scriptType, String content, String... parameters) {
        this.processor = processor;
        this.scriptType = scriptType;
        this.id = id;
        this.content = content;
        argumentHandler = new ArgumentHandler(this, parameters);
        isJShell = scriptType.equals("JShell");
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

    private boolean isLenient() {
        return processor.option("lenient").isPresent();
    }

    /**
     * Evaluate the content of the user defined macro using the actual values for the parameter values.
     *
     * @param parameters the actual string values for the parameters
     * @return the string that is the result of the evaluation
     * @throws BadSyntaxAt if the user defined macro is a script and the script evaluation throws exception. This
     *                     exception is thrown if the number of the actual values is not the same as the number of the
     *                     parameters.
     */
    @Override
    public String evaluate(final String... parameters) throws BadSyntax {
        final var adjustedValues = argumentHandler.adjustActualValues(parameters, isLenient());
        if (isJShell) {
            for (int i = 0; i < argumentHandler.parameters.length; i++) {
                ScriptingTools.populateJShell(processor.getJShellEngine(), argumentHandler.parameters[i], adjustedValues[i]);
            }
            return processor.getJShellEngine().evaluate(content);
        } else {
            final var engine = getEngine(scriptType);
            for (int i = 0; i < argumentHandler.parameters.length; i++) {
                populate(engine, argumentHandler.parameters[i], adjustedValues[i]);
            }
            try {
                return resultToString(ScriptingTools.evaluate(engine, content));
            } catch (Exception e) {
                throw new BadSyntax("Script '" + id + "' threw exception", e);
            }
        }
    }

    @Override
    public int expectedNumberOfArguments() {
        return argumentHandler.parameters.length;
    }
}
