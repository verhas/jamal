package javax0.jamal.snippet;


import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

/**
 * Macros defined in static inner classes that change the casing of the input.
 */
public class Case {
    private static String deCapitalize(String s) {
        if (s != null && !s.isEmpty()) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        }
        return s;
    }

    static String capitalize(String s) {
        if (s != null && !s.isEmpty()) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return s;
    }

    /**
     * Converts the argument of the macro to all characters lower case.
     */
    @Macro.Name("case:lower")
    public static
    class Lower implements Macro {
        @Override
        public String evaluate(Input input, Processor processor) {
            InputHandler.skipWhiteSpaces(input);
            return input.toString().trim().toLowerCase();
        }

    }

    /**
     * Converts the argument of the macro to all characters upper case.
     */
    @Macro.Name("case:upper")
    public static
    class Upper implements Macro {
        @Override
        public String evaluate(Input input, Processor processor) {
            return input.toString().trim().toUpperCase();
        }
    }

    /**
     * Capitalizes the input. Changes the first character to upper case.
     */
    @Macro.Name("case:cap")
    public static
    class Cap implements Macro {
        @Override
        public String evaluate(Input input, Processor processor) {
            return capitalize(input.toString().trim());
        }
    }

    /**
     * Uncapitalizes the input. Changes the first character to lower case.
     */
    @Macro.Name("case:decap")
    public static
    class Decap implements Macro {
        @Override
        public String evaluate(Input input, Processor processor) {
            return deCapitalize(input.toString().trim());
        }
    }
}
/*template jm_case
{template |case|case:$TO$ $C$|change the case of a macro|
        {variable |TO|enum("lower","upper","cap","decap")}
        {variable |C|"..."}
 }
 */