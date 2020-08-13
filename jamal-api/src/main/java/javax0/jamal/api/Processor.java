package javax0.jamal.api;

public interface Processor {
    String process(final Input in) throws BadSyntax;

    /**
     * Get the macro register of this processor. See {@link MacroRegister}
     *
     * @return the register
     */
    MacroRegister getRegister();

    /**
     * Create a new user defined macro. The implementation of this method calls the constructor of the matching
     * implementation of the user defined macro. The existence of this method loosens the coupling of the user
     * of the API and the actual implementation. The code using the implementation may need only to initiate the
     * engine that implements this interface, but does not need to access directly the UserDefinedMacro or other
     * interface implementations.
     * <p>
     * NOTE: The invocation of this method creates a new object but it DOES NOT register the created user defined
     * macro in the macro registry. The sole purpose of this method is to decouple the API usage and the implementation.
     *
     * @param id the identifier (name) of the macro
     * @param input the content of the macro
     * @param params the parameter names of the macro
     * @return the new user defined macro
     * @throws BadSyntax in case the parameter names contain each other
     */
    UserDefinedMacro newUserDefinedMacro(String id, String input, String[] params) throws BadSyntax;

    /**
     * Create a new user defined script. Read the important comments for
     * {@link #newUserDefinedMacro(String, String, String[])}
     *
     * @param id see {@link #newUserDefinedMacro(String, String, String[])}
     * @param scriptType see {@link #newUserDefinedMacro(String, String, String[])}
     * @param input see {@link #newUserDefinedMacro(String, String, String[])}
     * @param params see {@link #newUserDefinedMacro(String, String, String[])}
     * @return see {@link #newUserDefinedMacro(String, String, String[])}
     * @throws BadSyntax see {@link #newUserDefinedMacro(String, String, String[])}
     */
    ScriptMacro newScriptMacro(String id, String scriptType, String input, String[] params) throws BadSyntax;

    default boolean isDefined(String id) {
        return getRegister().getUserDefined(id).isPresent();
    }

    default void defineGlobal(Identified macro) {
        getRegister().global(macro);
    }

    default void define(Identified macro) {
        getRegister().define(macro);
    }

    default void separators(String openMacro, String closeMacro) throws BadSyntax {
        getRegister().separators(openMacro, closeMacro);
    }
}
