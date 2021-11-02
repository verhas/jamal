package javax0.jamal.api;

import java.util.Optional;

/**
 * User defined macros. User defined macros have an identifier and are evaluable. They are also debuggable.
 */
public interface UserDefinedMacro extends Identified, Evaluable, Debuggable<Debuggable.UserDefinedMacro> {
    default Optional<UserDefinedMacro> debuggable() {
        return Optional.empty();
    }
}
