package javax0.jamal.api;

public interface Processor {
    String process(final Input in) throws BadSyntax;

    MacroRegister getRegister();

    UserDefinedMacro newUserDefinedMacro(String id, String input, String[] params) throws BadSyntax;

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
