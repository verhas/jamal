package javax0.jamal.extensions;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Camel {

    private static String sentence(String s) {
        var c = new StringBuilder();
        var first = true;
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch) && !first) {
                c.append(" ");
            }
            c.append(Character.toLowerCase(ch));
            first = false;
        }
        return c.toString();
    }

    private static String cstyle(String s, char sep) {
        var c = new StringBuilder();
        var first = true;
        for (var ch : s.toCharArray()) {
            if (Character.isUpperCase(ch) && !first) {
                c.append(sep);
            }
            c.append(Character.toUpperCase(ch));
            first = false;
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

    public static class LowerCase implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return camelCase(in.toString().trim());
        }
    }

    public static class UpperCase implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            return Case.capitalize(camelCase(in.toString().trim()));
        }
    }

    public static class CStyle implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in);
            char sep = in.charAt(0);
            InputHandler.skip(in, 1);
            return cstyle(in.toString(), sep);
        }
    }

    public static class Sentence implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) {
            InputHandler.skipWhiteSpaces(in);
            return sentence(in.toString().trim());
        }
    }

}
