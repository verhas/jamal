package javax0.jamal.api;

import java.util.Optional;

/**
 * Something that is configurable should implement this interface. Currently, the {@link UserDefinedMacro}
 * implementation does only.
 */
public interface Configurable {

    enum Keys {
        SOFT,
        XTENDED,
        DEFAULTS,
        PURE,
        ID,
        VERBATIM,
        TAIL,
        PARAMS,
        PROCESSOR,
        INPUT
    }

    void configure(Keys key, Object value);
    default Optional<Object> get(Keys key){
        return Optional.empty();
    };
}
