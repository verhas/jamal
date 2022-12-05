package javax0.jamal.api;

/**
 * A marker interface signals that a class is a special macro
 * the processor should evaluate in the scope starting inside the macro. It means that the macro code will see
 * all user-defined macros defined inside the macro use.
 * <p>
 * The use and significance of this interface decreased in newer versions of Jamal with the introduction of macro options.
 * Before macro options, macros were reading option values from other argument-less macros.
 * This possibility is still there.
 * Macro options handling reads the macro named as the option if the option does not have a value specified at the use of the macro.
 * Using options, however, is more convenient in most cases.
 * Using macros holding the value for a macro option is useful when the Jamal code uses a macro option many times with the same value different from the default.
 *
 * This kind of use, however, does not require that the macro implements this interface.
 * In this case, the macro holding the value for the option is defined outside.
 * <p>
 * Old macros still implement this interface, and the functionality is kept in Jamal for backward compatibility reasons only.
 */

// end snippet
public interface InnerScopeDependent {
}
