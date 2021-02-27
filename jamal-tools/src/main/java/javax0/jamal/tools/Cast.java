package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@code Cast.cast(s)} will convert a string to an object. The object can be
 *
 * <ul>
 *     <li>Integer,</li>
 *     <li>Long,</li>
 *     <li>Double,</li>
 *     <li>Float,</li>
 *     <li>Boolean,</li>
 *     <li>Short,</li>
 *     <li>Byte,</li>
 *     <li>Character, or</li>
 *     <li>String.</li>
 * </ul>
 * <p>
 * The casting is executed based on the casting prefix. The casting prefix has to be at the start of the string in the
 * format of {@code (type)}, where the {@code type} is the primitive type of the desired type. In case of
 * {@code Integer} it is {@code int}, in the case {@code Character} it is {@code char} and in the case of {@code String}
 * it is {@code string}. In all other cases the name, as in Java, is the same as the name of the type lower cased.
 * <p>
 * If the string does not start with a {@code (tyoe)} string then it is not converted. In that case it is a string.
 * In all other cases the conversion is done and the leading {@code (type)} is removed from the start.
 */
public class Cast {
    public static Object cast(String s) throws BadSyntax {
        try {
            return
                cast(s, "int", Integer::parseInt).orElseGet(() ->
                    cast(s, "long", Long::parseLong).orElseGet(() ->
                        cast(s, "double", Double::parseDouble).orElseGet(() ->
                            cast(s, "float", Float::parseFloat).orElseGet(() ->
                                cast(s, "boolean", Boolean::parseBoolean).orElseGet(() ->
                                    cast(s, "short", Short::parseShort).orElseGet(() ->
                                        cast(s, "byte", Byte::parseByte).orElseGet(() ->
                                            cast(s, "char", k -> k.charAt(0)).orElseGet(() ->
                                                cast(s, "string", k -> k).orElseGet(() ->
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
