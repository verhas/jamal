package javax0.jamal.api;

import java.util.Optional;

/**
 * User defined macros. User defined macros have an identifier and are evaluable.
 * They are also debuggable, and they can be serialized and loaded from serialized form.
 */
public interface UserDefinedMacro extends Evaluable, Debuggable<Debuggable.UserDefinedMacro>, Serializing<UserDefinedMacro> {
    default Optional<UserDefinedMacro> debuggable() {
        return Optional.empty();
    }

    default javax0.jamal.api.UserDefinedMacro deserialize(String serialized) throws BadSyntax {
        return this;
    }

    @Override
    default String serialize() {
        return "";
    }
}
