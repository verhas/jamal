package javax0.jamal.api;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Any class that wants to function as a macro implementation should implement this interface.
 * <p>
 * The built-in macro {@code use} implemented in {@code javax0.jamal.builtins.Use} in the core package also assumes that
 * the class has a zero-parameter (default) constructor.
 * <p>
 * Macro implementations are supposed to be state-less, but they can have state. Be careful, however, that the macros
 * can have many instances whileprocessing a single file if they come into life via the {@code use} macro.
 */
@FunctionalInterface
public interface Macro {
    static List<Macro> getInstances() {
        ServiceLoader<Macro> services = ServiceLoader.load(Macro.class);
        List<Macro> list = new ArrayList<>();
        services.iterator().forEachRemaining(list::add);
        return list;
    }

    /**
     * This method reads the input an returns the result of the macro as a String.
     * <p>
     * When the macro is used, like
     * <pre>{@code
     *       {@builtInMacro this is the input}
     * }</pre>
     * <p>
     * then the input will contain '{@code this is the input}' without the spaces that are between the macro name and
     * the first non-space character, which is the word '{@code this}' as in the example.
     *
     * @param in        the input that is the "parameter" to the built-in macro
     * @param processor the processor that executes the macro. See {@link Processor}
     * @return the result string that will be inserted into the output in the place of the macro use
     * @throws BadSyntax the evaluation should throw this exception with reasonable message text in case the input has
     *                   bad format.
     */
    String evaluate(Input in, Processor processor) throws BadSyntax;

    /**
     * When a built-in macro is registered then the name used in the source file will be the string returned by this
     * method. When a macro is registered using the built-in macro {@code use} (see {@code javax0.jamal.builtins.Use})
     * the the caller can provide an alias. Even when the proposed use is to be declared through the {@code use} macro
     * it is recommended to provide a reasonable id.
     *
     * @return the id/name of the macro
     */
    default String getId() {
        return this.getClass().getSimpleName().toLowerCase();
    }
}
