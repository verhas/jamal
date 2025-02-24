package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;

import java.math.BigDecimal;
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
 * If the string does not start with a {@code (type)} string then it is not converted. In that case it is a string.
 * In all other cases the conversion is done and the leading {@code (type)} is removed from the start.
 */
public class Cast {
    public static Object cast(String s) throws BadSyntax {
        if( s == null || s.isEmpty() ){
            return "";
        }
        int closer = s.indexOf(')');
        if( s.charAt(0) == '(' && closer > 0 ){
            final var type = s.substring(1, closer);
            final var value = s.substring(closer + 1).replaceAll("^\\s*", "");
            Function<String,Object> converter;
            switch (type) {
                case "int": converter = Integer::parseInt; break;
                case "long": converter = Long::parseLong; break;
                case "short": converter = Short::parseShort; break;
                case "byte": converter = Byte::parseByte; break;
                case "double": converter = Double::parseDouble; break;
                case "float": converter = Float::parseFloat; break;
                case "boolean": converter = Boolean::parseBoolean; break;
                case "BigDecimal": converter = BigDecimal::new; break;
                case "char" : converter = k -> k.charAt(0); break;
                case "string": converter = k -> k;break;
                default : throw new BadSyntax("Unsupported type: " + type);
            }
            return converter.apply(value);
        }else{
            return s;
        }
    }
}
