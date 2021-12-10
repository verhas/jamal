package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Params.holder;

public class StringMacros {

    public static class Contains implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var regex = holder(null, "regex").asBoolean();
            final var text = holder(null, "text", "string").asString();
            Params.using(processor).from(this).between("()").keys(regex, text).parse(in);
            if (regex.is()) {
                return "" + Pattern.compile(text.get()).matcher(in.toString()).find();
            } else {
                return "" + in.toString().contains(text.get());
            }
        }

        @Override
        public String getId() {
            return "string:contains";
        }
    }

    public static class Quote implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in,1);
            if( parts.length != 1 ) {
                throw new BadSyntax("The string:quote macro expects exactly one argument");
            }
            return parts[0]
                .replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\b", "\\b")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\f", "\\f")
                .replace("\"", "\\\"");
        }

        @Override
        public String getId() {
            return "string:quote";
        }
    }

    private static abstract class XWith implements Macro, InnerScopeDependent {
        private final BiPredicate<String, String> with;

        protected XWith(BiPredicate<String, String> with) {
            this.with = with;
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in,2);
            if (parts.length != 2) {
                throw new BadSyntax(getId() + " needs two parts");
            }
            return "" + with.test(parts[0], parts[1]);
        }
    }

    public static class StartsWith extends XWith {
        public StartsWith() {
            super(String::startsWith);
        }

        @Override
        public String getId() {
            return "string:startsWith";
        }

    }

    public static class EndsWith extends XWith {
        public EndsWith() {
            super(String::endsWith);
        }

        @Override
        public String getId() {
            return "string:endsWith";
        }

    }

    public static class Equals implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var ignoreCase = holder("ignoreCase").asBoolean();
            Params.using(processor).from(this).between("()").keys(ignoreCase).parse(in);
            String[] parts = InputHandler.getParts(in,2);
            if (parts.length != 2) {
                throw new BadSyntax(getId() + " needs two parts");
            }
            return "" + (ignoreCase.is() ? parts[0].equalsIgnoreCase(parts[1]) : parts[0].equals(parts[1]));
        }

        @Override
        public String getId() {
            return "string:equals";
        }
    }

    public static class Reverse implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in,1);
            if( parts.length != 1 ) {
                throw new BadSyntax("The string:reverse macro expects exactly one argument");
            }
            return new StringBuilder(parts[0]).reverse().toString();
        }

        @Override
        public String getId() {
            return "string:reverse";
        }
    }

    public static class Substring implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var begin = holder(null, "begin").orElseInt(0);
            final var end = holder(null, "end").asInt();
            Params.using(processor).from(this).between("()").keys(begin, end).parse(in);
            String[] parts = InputHandler.getParts(in,1);
            if( parts.length != 1 ) {
                throw new BadSyntax("The string:substring macro expects exactly one argument");
            }
            final var beginIndex = begin.get() < 0 ? in.length() + begin.get() : begin.get();
            final var endIndex = end.isPresent() ? (end.get() < 0 ? in.length() + end.get() : end.get()) : in.length();
            return parts[0].substring(beginIndex, endIndex);
        }

        @Override
        public String getId() {
            return "string:substring";
        }
    }

    public static class Length implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var trim = holder(null, "trim").asBoolean();
            final var left = holder(null, "left").asBoolean();
            final var right = holder(null, "right").asBoolean();
            Params.using(processor).from(this).between("()").keys(trim, left, right).parse(in);
            if ((left.is() || right.is()) && !trim.is()) {
                throw new BadSyntax("You cannot use 'left' or 'right' on 'string:length' without trim");
            }
            var string = in.toString();
            if (trim.is()) {
                if (left.is() == right.is()) {
                    string = string.trim();
                } else if (left.is()) {
                    string = string.stripLeading();
                } else {
                    string = string.stripTrailing();
                }
            }
            return "" + string.length();
        }

        @Override
        public String getId() {
            return "string:length";
        }
    }

}
