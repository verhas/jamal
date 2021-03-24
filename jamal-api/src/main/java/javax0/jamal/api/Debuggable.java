package javax0.jamal.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This interface collects those interfaces that define the functionalities of certain types in the engine that
 * implement these methods only for the debugger and these methods are not needed and should not be used for the normal
 * operation.
 */
public interface Debuggable<T> {

    /**
     * When a class implements a debuggable version then this method returns the debuggable version.
     * @return an optional debuggable. When it is not available then Empty is returned.
     */
    Optional<T> debuggable();

    /**
     * Scopes are internal matter of the processor. When a debugger wants to look into the scopes and gets a list of
     * scopes it actually gets the real list but the type is declared as list of {@code Debuggable.Scope}. These are the
     * only methods that a debugger is allowed to invoke.
     */
    interface Scope {
        /**
         * Get the map of the user defined macros.
         * @return the map of the user defined macros, wich are defined in the current scope
         */
        Map<String, Identified> getUdMacros();

        /**
         * Get the built-in macros.
         * @return the map of built-in macros that are defined in this scope
         */
        Map<String, Macro> getMacros();

        /**
         * Get the macro open and close strings.
         * @return a {@link Delimiters} object
         */
        Delimiters getDelimiterPair();
    }

    /**
     * Methods of the macro register available only for the debugger
     */
    interface MacroRegister {
        List<Scope> getScopes();

        List<Marker> getPoppedMarkers();
    }

    /**
     * Methods of the user defined macro class available only for the debugger
     */
    interface UserDefinedMacro {
        String[] getParameters();

        String getContent();

        String getOpenStr();

        String getCloseStr();
    }
}
