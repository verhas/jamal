package javax0.jamal.extensions;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Camel {

    public static class LowerCase implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return camelCase(in.getInput().toString().trim());
        }
    }

    public static class UpperCase implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return Case.capitalize(camelCase(in.getInput().toString().trim()));
        }
    }

    public static class CStyle implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in.getInput());
            char sep = in.getInput().charAt(0);
            InputHandler.skip(in.getInput(), 1);
            return cstyle(in.getInput().toString(), sep);
        }
    }

    public static class Sentence implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in.getInput());
            return sentence(in.getInput().toString().trim());
        }
    }

    private static String sentence(String s) {
        var c = new StringBuilder();
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                c.append(" ");
            }
            c.append(Character.toLowerCase(ch));
        }
        return c.toString();
    }

    private static String cstyle(String s, char sep) {
        var c = new StringBuilder();
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                c.append(sep);
            }
            c.append(Character.toUpperCase(ch));
        }
        return c.toString();
    }

    private static String camelCase(String s) {
        var cased = new StringBuilder();
        boolean inside = true;
        for (final var c : s.toCharArray()) {
            if (Character.isAlphabetic(c) || Character.isDigit(c)) {
                cased.append(inside ? Character.toLowerCase(c) : Character.toUpperCase(c));
                inside = true;
            } else {
                inside = false;
            }
        }
        return cased.toString();
    }

}
