package javax0.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;

import java.util.Map;

/**
 * A simple formatting wrapper that formats a string using macros that are provided in a Map. The Map is a (String,
 * String) map and these macros cannot have arguments. This is a simple templating implementation using Jamal as a
 * template engine.
 */
public class Format {

    /**
     * The macro opening string is '{{' and the closing string is '}}' like in many other templating languages, however
     * in this case the whole weaponry of Jamal is available. Since the input does not come from a file and thus there
     * is no actual location of the input any {@code import} or {@code include} in the file can only be absolute file
     * name or a resource starting with {@code res:xyz} form file name or a web resource starting with {@code https:}.
     *
     * @param content          the text that uses the macros
     * @param predefinedMacros the Map that contains the predefined macros
     * @return the formatted string
     * @throws BadSyntax in case the string cannot be formatted using the provided macros
     */
    public static String format(String content, Map<String, String> predefinedMacros) throws BadSyntax {
        try (final var processor = new Processor("{{", "}}")) {
            final var register = processor.getRegister();
            for (final var macro : predefinedMacros.entrySet()) {
                register.global(processor.newUserDefinedMacro(macro.getKey(), macro.getValue()));
            }
            final var in = new Input(content, new Position(""));
            return processor.process(in);
        }
    }
}
