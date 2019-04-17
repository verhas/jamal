package javax0.jamal.extensions;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Macros defined in static inner classes that some way help handling regular expressions.
 */
public class Regex {

    /**
     * This macro splits the input into three parts using {@link InputHandler#getParts(Input)} and then
     * returns {@code }part[0].replaceAll(part[2], part[3])}
     */
    public static class ReplaceAll implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var part = InputHandler.getParts(in);
            if (part.length != 3) {
                throw new BadSyntax("replaceAll needs exactly 3 parts separated but the input has " +
                    part.length);
            }
            return part[0].replaceAll(part[1], part[2]);
        }

        @Override
        public String getId() {
            return "replaceAll";
        }
    }

    private static class GroupUserDefinedMacro implements UserDefinedMacro {

        final String name;
        final java.util.regex.Matcher matcher;

        GroupUserDefinedMacro(String name, java.util.regex.Matcher matcher) {
            this.name = name;
            this.matcher = matcher;
        }

        @Override
        public String getId() {
            return name;
        }

        @Override
        public String evaluate(String... actualValues) throws BadSyntax {
            if (actualValues.length == 0) {
                throw new BadSyntax("The generated macro " + name + " needs at least one argument");
            }
            var command = actualValues[0].trim();
            final String arg;
            if (actualValues.length > 1) {
                arg = actualValues[1];
            } else {
                arg = null;
            }
            try {
                switch (command) {
                    case "nr":
                        return "" + matcher.groupCount();
                    case "groupIndices":
                        return IntStream.range(1, matcher.groupCount() + 1)
                            .mapToObj(i -> "" + i).collect(Collectors.joining(","));
                    case "start":
                        return arg == null ? "" + matcher.start() : "" + matcher.start(Integer.parseInt(arg));
                    case "end":
                        return arg == null ? "" + matcher.end() : "" + matcher.end(Integer.parseInt(arg));
                    case "group":
                        if (arg == null) {
                            return matcher.group();
                        }
                        try {
                            var index = Integer.parseInt(arg);
                            try {
                                return matcher.group(index);
                            } catch (IllegalStateException ise) {
                                throw new BadSyntax("The generated macro '" + name + "' for 'group' caused exception with the argument '" + index + "'");
                            }
                        } catch (NumberFormatException ignored) {
                            try {
                                return matcher.group(arg);
                            } catch (IllegalStateException ise) {
                                throw new BadSyntax("The generated macro '" + name + "' for 'group' caused exception with the argument '" + arg + "'");
                            }
                        }
                    case "matches":
                        return "" + matcher.matches();
                    case "find":
                        if (arg == null) {
                            return "" + matcher.find();
                        } else {
                            var start = Integer.parseInt(arg);
                            return "" + matcher.find(start);
                        }
                    default:
                        throw new BadSyntax("The generated macro '" + name + "' argument '" + command + "' cannot be interpreted.");
                }
            } catch (NumberFormatException nfe) {
                throw new BadSyntax("The command '" + command + "' in the generated macro '" + name + "' needs a numeric argument");
            }
        }
    }

    /**
     * Match a string against a regular expression.
     * <p>
     * This macro splits the input into two parts using {@link
     * InputHandler#getParts(Input)} and matches
     */
    public static class Matcher implements Macro {
        @Override
        public String evaluate(Input in, Processor processor) throws BadSyntax {
            final var part = InputHandler.getParts(in);
            if (part.length != 3) {
                throw new BadSyntax("matcher needs exactly 3 parts separated but the input has " +
                    part.length);
            }
            var groupMacroName = part[0].trim();
            var pattern = Pattern.compile(part[1]);
            var matcher = pattern.matcher(part[2]);
            var udm = new GroupUserDefinedMacro(groupMacroName, matcher);
            processor.define(udm);
            return "";
        }
    }

}
