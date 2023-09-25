package javax0.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;

import java.util.Map;

/**
 * A complimentary formatter class that provides more formatting use than the old simple {@link Format} class.
 * <p>
 * The difference is that you can use a fluent API to specify the parameters, as well as you can define maro start and
 * end strings.
 */
public class Formatter implements AutoCloseable {

    private final Processor processor;

    public Formatter(String open, String close) {
        processor = new Processor(open, close);
    }

    /**
     * Specify parameters for the formatter. The parameters are key-value pairs.
     * <p>
     * The keys are the names of the macros and the values are the values.
     *
     * @param maps the parameter maps. You can spcify more than one to overcome the shortage of {@code Map.of()}
     *             limitation that it cannot be vararg.
     * @return this to allow chaining of the calls
     * @throws BadSyntax if the macro is already defined
     */
    @SafeVarargs
    public final Formatter using(Map<String, String>... maps) throws BadSyntax {
        for (final var macros : maps) {
            for (final var macro : macros.entrySet()) {
                define(macro.getKey(), macro.getValue(), false);
            }
        }
        return this;
    }

    /**
     * Specify parameters for the formatter. The parameters are key-value pairs.
     * <p>
     * The keys are the names of the macros and the values are the values.
     *
     * @param pairs the parameters and values. Ever string on an odd position is a key and the next is the value.
     * @return this to allow chaining of the calls
     * @throws BadSyntax if the macro is already defined
     */
    public final Formatter using(String... pairs) throws BadSyntax {
        for (int i = 0; i < pairs.length - 1; i += 2) {
            define(pairs[i], pairs[i + 1], false);
        }
        return this;
    }

    /**
     * Format the content using the macros that are defined in the formatter and the new macros provided as arguments.
     * <p>
     * Note that the macros specified in the map are added to the processor, therefore are usable in subsequent format
     * calls as well.
     *
     * @param content the string content containing the macros
     * @param maps    new maps containing the macros. In this case, it is possible to override already existing macros.
     * @return the formatted string
     * @throws BadSyntax if there is some error during the processing, for example, a macro used is not defined
     */
    @SafeVarargs
    public final String format(String content, Map<String, String>... maps) throws BadSyntax {
        for (final var macros : maps) {
            for (final var macro : macros.entrySet()) {
                define(macro.getKey(), macro.getValue(), true);
            }
        }
        final var in = new Input(content, new Position(""));
        return processor.process(in);
    }

    /**
     * The formatter uses a Jamal Processor as a resource which is closable.
     * This implementation closes the processor.
     *
     * @throws Exception whatever the processor is closing
     */
    @Override
    public void close() throws Exception {
        processor.close();
    }


    /**
     * Define a macro in the processor.
     *
     * @param key the name of the macro
     * @param value the value of the macro
     * @param override if {@code true} then the macro is defined even if it is already defined
     * @throws BadSyntax if there is a BadSyntax error during the definition of the macro
     */
    private void define(String key, String value, boolean override) throws BadSyntax {
        final var register = processor.getRegister();
        BadSyntax.when((!override) && register.getUdMacroLocal(key).isPresent(), "Macro " + key + " is already defined");
        register.global(processor.newUserDefinedMacro(key, value));
    }

}
