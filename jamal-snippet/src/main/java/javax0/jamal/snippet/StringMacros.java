package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class StringMacros {

    @Macro.Name("string:contains")
    public static class Contains implements Macro, InnerScopeDependent, Scanner {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var regex = scanner.bool(null, "regex");
            final var text = scanner.str(null, "text", "string");
            scanner.done();
            if (regex.is()) {
                return "" + Pattern.compile(text.get()).matcher(in.toString()).find();
            } else {
                return "" + in.toString().contains(text.get());
            }
        }
    }

    @Macro.Name("string:quote")
    public static class Quote implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in, processor, 1);
            BadSyntax.when(parts.length != 1, "The string:quote macro expects exactly one argument");
            return parts[0]
                    .replace("\\", "\\\\")
                    .replace("\t", "\\t")
                    .replace("\b", "\\b")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\f", "\\f")
                    .replace("\"", "\\\"");
        }
    }

    public static class StringMacro implements Macro, InnerScopeDependent, Scanner {


        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var ignoreCase = scanner.bool("ignoreCase");
            scanner.done();
            String[] parts = InputHandler.getParts(in, processor, 3);
            BadSyntax.when(parts.length != 3, "%s needs three parts", getId());
            switch (parts[1].trim().toLowerCase()) {
                case "startswith":
                    return "" + (ignoreCase.is() ? parts[0].toLowerCase().startsWith(parts[2].toLowerCase()) : parts[0].startsWith(parts[2]));
                case "endswith":
                    return "" + (ignoreCase.is() ? parts[0].toLowerCase().endsWith(parts[2].toLowerCase()) : parts[0].endsWith(parts[2]));
                case "equals":
                case "equalsto":
                    return "" + (ignoreCase.is() ? parts[0].equalsIgnoreCase(parts[2]) : parts[0].equals(parts[2]));
                case "contains":
                    return "" + (ignoreCase.is() ? parts[0].toLowerCase().contains(parts[2].toLowerCase()) : parts[0].contains(parts[2]));

            }
            throw new BadSyntax("Unknown string macro: " + parts[1]);
        }

        @Override
        public String getId() {
            return "string";
        }
    }

    private static abstract class XWith implements Macro, InnerScopeDependent {
        private final BiPredicate<String, String> with;

        protected XWith(BiPredicate<String, String> with) {
            this.with = with;
        }

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in, processor, 2);
            BadSyntax.when(parts.length != 2, "%s needs two parts", getId());
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

    @Macro.Name("string:endsWith")
    public static class EndsWith extends XWith {
        public EndsWith() {
            super(String::endsWith);
        }

    }

    @Macro.Name("string:equals")
    public static class Equals implements Macro, InnerScopeDependent, Scanner {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var ignoreCase = scanner.bool("ignoreCase");
            scanner.done();
            String[] parts = InputHandler.getParts(in, processor, 2);
            BadSyntax.when(parts.length != 2, "%s needs two parts", getId());
            return "" + (ignoreCase.is() ? parts[0].equalsIgnoreCase(parts[1]) : parts[0].equals(parts[1]));
        }
    }

    @Macro.Name("string:reverse")
    public static class Reverse implements Macro, InnerScopeDependent {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            String[] parts = InputHandler.getParts(in, processor, 1);
            BadSyntax.when(parts.length != 1, "The string:reverse macro expects exactly one argument");
            return new StringBuilder(parts[0]).reverse().toString();
        }
    }

    @Macro.Name("string:chop")
    public static class Chop implements Macro, Scanner {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var prefix = scanner.str(null, "prefix", "pre", "start");
            final var postfix = scanner.str(null, "postfix", "post", "end");
            final var ignore = scanner.bool(null, "ignorecase", "ignoreCase");
            scanner.done();
            var result = in.toString();
            if (prefix.isPresent() && (ignore.is() ? result.toLowerCase().startsWith(prefix.get().toLowerCase()) : result.startsWith(prefix.get()))) {
                result = result.substring(prefix.get().length());
            }
            if (postfix.isPresent() && (ignore.is() ? result.toLowerCase().endsWith(postfix.get().toLowerCase()) : result.endsWith(postfix.get()))) {
                result = result.substring(0, result.length() - postfix.get().length());
            }
            return result;
        }
    }

    @Macro.Name("string:between")
    public static class Between implements Macro, Scanner {

        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var after = scanner.str(null, "after").optional();
            final var before = scanner.str(null, "before").optional();
            final var ignore = scanner.bool(null, "ignorecase", "ignoreCase");
            scanner.done();
            var result = in.toString();

            final int afterIndex;
            if (after.isPresent()) {
                afterIndex = (ignore.is() ?
                        result.toLowerCase().indexOf(after.get().toLowerCase()) : result.indexOf(after.get()))
                        + after.get().length();
                if (afterIndex < after.get().length()) {
                    return "";
                }
            } else {
                afterIndex = 0;
            }
            final int beforeIndex;
            if (before.isPresent()) {
                beforeIndex = ignore.is() ?
                        result.toLowerCase().lastIndexOf(before.get().toLowerCase()) : result.lastIndexOf(before.get());
                if (beforeIndex < 0) {
                    return "";
                }
            } else {
                beforeIndex = result.length();
            }
            if (afterIndex >= beforeIndex) {
                return "";
            }
            result = result.substring(afterIndex, beforeIndex);
            return result;
        }
    }

    @Macro.Name({"string:after", "string:before"})
    public static class After implements Macro, Scanner {
// snippet GETID_AFTER
        @Override
        public String evaluate(final Input in, final Processor processor) throws BadSyntax {
            final var action = processor.getId();
// end snippet
            final var scanner = newScanner(in, processor);
            final var str = scanner.str(null, "first", "theFirst", "second", "theSecond", "third", "theThird", "last", "theLast", "nth", "theNth");
            final var fromEnd = scanner.bool(null, "fromEnd", "fromTheEnd");
            final var nParameter = scanner.number(null, "n").optional();
            final var ignore = scanner.bool(null, "ignorecase", "ignoreCase");
            scanner.done();

            int n;
            boolean reverse = false;
            switch (str.name()) {
                case "first":
                case "theFirst":
                    n = 1;
                    BadSyntax.when(nParameter.isPresent(), "You cannot use 'n' with 'first'");
                    BadSyntax.when(fromEnd.isPresent(), "You cannot use 'fromEnd' with 'first'. Simply use 'last'");
                    break;
                case "second":
                case "theSecond":
                    n = 2;
                    BadSyntax.when(nParameter.isPresent(), "You cannot use 'n' with 'second'");
                    break;
                case "third":
                case "theThird":
                    n = 3;
                    BadSyntax.when(nParameter.isPresent(), "You cannot use 'n' with 'third'");
                    break;
                case "last":
                case "theLast":
                    n = 1;
                    reverse = true;
                    BadSyntax.when(nParameter.isPresent(), "You cannot use 'n' with 'last'");
                    BadSyntax.when(fromEnd.is(), "You cannot use 'fromEnd' with 'last'. Simply use 'first'");
                    break;
                case "nth":
                case "theNth":
                    BadSyntax.when(!nParameter.isPresent(), "You must use 'n' with 'nth'");
                    n = nParameter.get();
                    break;
                default:
                    throw new BadSyntax("Unknown parameter name: " + str.name());
            }
            if (fromEnd.is()) {
                reverse = true;
            }
            var searchedString = in.toString();

            final int index;
            final var findStr = str.get();
            if (reverse) {
                index = findNthOccurrenceFromEnd(searchedString, findStr, n, ignore.is());
            } else {
                index = findNthOccurrence(searchedString, findStr, n, ignore.is());
            }
            if (action.equals("string:before")) {
                return searchedString.substring(0, index);
            } else {
                return searchedString.substring(index + findStr.length());
            }
        }

        /**
         * Finds the index of the n-th occurrence of a substring within a given string, searching from the end of the string.
         * If the substring is found n times when searching backwards, the index (0-based) of the n-th occurrence is returned.
         * If the substring does not occur n times when searching in this manner, -1 is returned.
         *
         * @param str     The string to search in.
         * @param findStr The substring to find.
         * @param n       The occurrence number to find (1 for the first occurrence from the end, 2 for the second, etc.).
         * @return The index of the n-th occurrence of findStr in str when searching from the end, or -1 if findStr does not occur n times in str from the end.
         * @throws NullPointerException     if str or findStr is null.
         * @throws IllegalArgumentException if n is less than 1.
         */
        private static int findNthOccurrenceFromEnd(String str, String findStr, int n, boolean ignoreCase) {
            if (ignoreCase) {
                str = str.toLowerCase();
                findStr = findStr.toLowerCase();
            }
            int index = str.length();
            for (int i = 0; i < n; i++) {
                index = str.lastIndexOf(findStr, index - 1); // Decrease index to search in the remaining part of the string from the end
                if (index == -1) break; // If there are less than n occurrences
            }
            return index;
        }

        /**
         * Finds the index of the n-th occurrence of a substring within a given string.
         * If the substring is found n times, the index (0-based) of the n-th occurrence is returned.
         * If the substring does not occur n times, -1 is returned.
         *
         * @param str     The string to search in.
         * @param findStr The substring to find.
         * @param n       The occurrence number to find (1 for the first occurrence, 2 for the second, etc.).
         * @return The index of the n-th occurrence of findStr in str or -1 if findStr does not occur n times in str.
         * @throws NullPointerException     if str or findStr is null.
         * @throws IllegalArgumentException if n is less than 1.
         */
        private static int findNthOccurrence(String str, String findStr, int n, boolean ignoreCase) {
            if (ignoreCase) {
                str = str.toLowerCase();
                findStr = findStr.toLowerCase();
            }
            int index = -1;
            for (int i = 0; i < n; i++) {
                index = str.indexOf(findStr, index + 1);
                if (index == -1) break;
            }
            return index;
        }
    }

    public static class Substring implements Macro, InnerScopeDependent, Scanner {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var begin = scanner.number(null, "begin").defaultValue(0);
            final var end = scanner.number(null, "end");
            scanner.done();
            String[] parts = InputHandler.getParts(in, processor, 1);
            BadSyntax.when(parts.length != 1, "The string:substring macro expects exactly one argument");
            final var beginIndex = begin.get() < 0 ? in.length() + begin.get() : begin.get();
            final var endIndex = end.isPresent() ? (end.get() < 0 ? in.length() + end.get() : end.get()) : in.length();
            return parts[0].substring(beginIndex, endIndex);
        }

        @Override
        public String getId() {
            return "string:substring";
        }
    }

    @Macro.Name("string:length")
    public static class Length implements Macro, InnerScopeDependent, Scanner {

        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var scanner = newScanner(in, processor);
            final var trim = scanner.bool(null, "trim");
            final var left = scanner.bool(null, "left");
            final var right = scanner.bool(null, "right");
            scanner.done();
            BadSyntax.when((left.is() || right.is()) && !trim.is(), "You cannot use 'left' or 'right' on 'string:length' without trim");
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
    }

}
