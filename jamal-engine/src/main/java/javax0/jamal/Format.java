package javax0.jamal;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;

import java.util.Map;

public class Format {


    /**
     * A simple formatting wrapper that formats a string using macros that are provided in a Map. The Map is
     * String String map and these macros cannot have arguments. This is a simple templating implementation
     * using Jamal as a template engine.
     *
     * @param content          the text that uses the macros
     * @param predefinedMacros the Map that contains the predefined macros
     */
    public static String format(String content, Map<String, String> predefinedMacros) throws BadSyntax {
        final var processor = new Processor("{{", "}}");
        final var register = processor.getRegister();
        for (final var macro : predefinedMacros.entrySet()) {
            register.global(processor.newUserDefinedMacro(macro.getKey(), macro.getValue()));
        }
        final var in = new Input(content, new Position(""));
        return processor.process(in);
    }
}
