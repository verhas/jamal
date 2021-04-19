package javax0.jamal.api;

/**
 * Something that is usually defined by the user and as such has an identifier.
 * <p>
 * The typical user defined things are
 *
 * <ul>
 *     <li>user defined macros</li>
 *     <li>user defined scripts</li>
 * </ul>
 * <p>
 * Some macros also want to implement something similar things that may or may not be accessed from the processed
 * jamal text, but are available and managed by the code. A good example is how the macro
 * {@code javax0.jamal.builtins.Options} is implemented. When the options is first defined it creates an instance of
 * {@code javax0.jamal.tools.OptionsStore}, which is neither a script, nor a macro and it cannot be evaluated, but
 * it has an identifier, which is {@code `options}. This object is stored along with the scripts and user defined
 * macros with the same life cycle as those. If an option was set in a local environment it will no affect environments
 * above unless it is exported.
 */
@FunctionalInterface
public interface Identified {
    /**
     * Get the string identifier of the identifiable.
     *
     * @return the string representation of the identifier. Usually a human readable name.
     */
    String getId();

    String DEFAULT_MACRO = "default";
    String MACRO_NAME_ARG1 = "$macro";
    String MACRO_NAME_ARG2 = "$_";
}
