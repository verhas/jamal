package javax0.jamal.api;

import java.util.Optional;

/**
 * User defined macro.
 * <p>
 * User defined macros have an identifier and are evaluable.
 * They are also debuggable, and they can be serialized and loaded from serialized form.
 */
public interface UserDefinedMacro extends Evaluable, Debuggable<Debuggable.UserDefinedMacro>, Serializing<UserDefinedMacro> {
    default Optional<UserDefinedMacro> debuggable() {
        return Optional.empty();
    }

    /**
     * Deserialize the macro from the serialized form.
     *
     * @param serialized the serialized form of the macro
     * @return the deserialized macro object
     * @throws BadSyntax if the serialized form is not correct
     */
    default javax0.jamal.api.UserDefinedMacro deserialize(String serialized) throws BadSyntax {
        return this;
    }

    /**
     * Serialize the macro.
     *
     * @return the serialized form of the macro as a string.
     */
    @Override
    default String serialize() {
        return "";
    }
}
