package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import java.util.IllegalFormatException;
import java.util.Optional;
import java.util.function.Function;

public class Format implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final String[] parts = InputHandler.getParts(in);
        final String format = parts[0];
        try {
            final Object[] value = new Object[parts.length - 1];
            for (int i = 0; i < value.length; i++) {
                value[i] = cast(parts[i + 1]);
            }
            return String.format(format, value);
        } catch (IllegalFormatException e) {
            throw new BadSyntax("The format string '" + format + "'in macro '" + getId() + "' is incorrect.", e);
        }
    }

    private static Object cast(String s) throws BadSyntax {
        try {
            return
                cast(s, "int", Integer::parseInt).orElse(
                    cast(s, "long", Long::parseLong).orElse(
                        cast(s, "double", Double::parseDouble).orElse(
                            cast(s, "float", Float::parseFloat).orElse(
                                cast(s, "boolean", Boolean::parseBoolean).orElse(
                                    cast(s, "short", Short::parseShort).orElse(
                                        cast(s, "byte", Byte::parseByte).orElse(
                                            cast(s, "char", k -> k.charAt(0)).orElse(
                                                cast(s, "string", k -> k).orElse(
                                                    s
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                );
        } catch (Exception e) {
            throw new BadSyntax("There was an error during casting the value '" + s + "'", e);
        }
    }

    private static Optional<Object> cast(String s, String prefix, Function<String, Object> converter) {
        prefix = "(" + prefix + ")";
        if (s.startsWith(prefix)) {
            return Optional.of(converter.apply(s.substring(prefix.length()).replaceAll("^\\s*", "")));
        }
        return Optional.empty();
    }
}
