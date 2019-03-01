package javax0.jamal.extensions;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Case {
    private static String deCapitalize(String s) {
        if (s != null && s.length() > 0) {
            return s.substring(0, 1).toLowerCase() + s.substring(1);
        }
        return s;
    }

    static String capitalize(String s) {
        if (s != null && s.length() > 0) {
            return s.substring(0, 1).toUpperCase() + s.substring(1);
        }
        return s;
    }

    public static class Lower implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            var input = in;
            InputHandler.skipWhiteSpaces(input);
            return input.toString().trim().toLowerCase();
        }
    }

    public static class Upper implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            var input = in;
            return input.toString().trim().toUpperCase();
        }
    }

    public static class Cap implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            var input = in;
            return capitalize(input.toString().trim());
        }
    }

    public static class Decap implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            var input = in;
            return deCapitalize(input.toString().trim());
        }
    }
}
