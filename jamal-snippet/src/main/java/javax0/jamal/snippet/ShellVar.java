package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Macro.Name("shell:var")
public class ShellVar implements Macro, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var parameters = scanner.str("variables").defaultValue("");
        scanner.done();
        final Map<String, String> values = new HashMap<>();
        // get the parameters without any key name constraint using the params handling tools
        Params.using(processor).tillEnd().parse(javax0.jamal.tools.Input.makeInput(parameters.get()), values::put, s -> true);

        final var sb = new StringBuilder(in);
        replace(processor, values, sb, 0);
        unescape(sb);
        return sb.toString();
    }

    /*
     * Remove the escape character from the string builder.
     */
    private static void unescape(final StringBuilder sb) {
        for (int i = sb.length() - 1; i >= 0; i--) {
            if (isEscape(sb, i)) {
                sb.deleteCharAt(i);
            }
        }
    }

    /**
     * Replaces shell-style variable references in a string with their corresponding values.
     * This method supports both ${variable} and $variable syntax for variable references.
     * It also handles escaped dollar signs (\$) by skipping them.
     *
     * @param processor The Processor object used for variable resolution.
     * @param values    A Map containing variable names and their corresponding values.
     * @param sb        A StringBuilder containing the input string with variable references.
     * @param depth     The current recursion depth for nested variable substitutions.
     * @return A String with all variable references replaced by their values.
     * @throws BadSyntax If the variable syntax is invalid or if the recursion depth exceeds 100.
     */
    private static String replace(Processor processor, Map<String, String> values, StringBuilder sb, int depth) throws BadSyntax {
        if (depth > 100) {
            throw new BadSyntax("Too deep recursion in shell variable substitution");
        }
        int start = 0;
        while (true) {
            int i = sb.indexOf("$", start);
            if (signIsEscaped(sb, i)) {
                start = i + 1;
                continue;
            }
            if (noDollarSignOrItIsTheLastCharacter(sb, i)) {
                break;
            }
            if (variableIsBraced(sb, i)) {
                final var j = Optional.of(sb.indexOf("}", i + 2)).filter(k -> k >= 0)
                        .orElseThrow(() -> new BadSyntax("Missing '}' in the shell variable substitution"));
                final var name = sb.substring(i + 2, j);
                final var value = getVariable(name, values, processor, depth + 1);
                sb.replace(i, j + 1, value);
                start = i + value.length();
            } else {
                int j = Optional.of(i + 1).filter(k -> k < sb.length() && Macro.validId1stChar(sb.charAt(k)))
                        .orElseThrow(() -> new BadSyntax("Missing variable name after '$' in the shell variable substitution")) + 1;
                while (j < sb.length() && Macro.validIdChar(sb.charAt(j)) && sb.charAt(j) != '$') {
                    j++;
                }
                final String name = sb.substring(i + 1, j);
                String value = getVariable(name, values, processor, depth + 1);
                sb.replace(i, j, value);
            }
        }
        return sb.toString();
    }

    private static boolean variableIsBraced(StringBuilder sb, int i) {
        return sb.charAt(i + 1) == '{';
    }

    private static boolean noDollarSignOrItIsTheLastCharacter(StringBuilder sb, int i) {
        return i < 0 || i == sb.length() - 1;
    }

    private static boolean isEscape(StringBuilder sb, int i) {
        return i >= 0 && sb.charAt(i) == '\\' && !isEscape(sb, i - 1);
    }

    private static boolean signIsEscaped(StringBuilder sb, int i) {
        return i > 0 && isEscape(sb, i - 1);
    }

    private static String getVariable(String variable, final Map<String, String> values, Processor processor, int depth) throws BadSyntax {
        final String envVar;
        if (variable.contains("$")) {
            final var sb = new StringBuilder(variable);
            envVar = replace(processor, values, sb, depth);
        } else {
            envVar = variable;
        }
        String value = values.get(envVar);
        if (value == null) {
            final var macro = processor.getRegister().getUserDefined(envVar)
                    .filter(m -> m instanceof Evaluable)
                    .map(m -> ((Evaluable) m))
                    .filter(m -> m.expectedNumberOfArguments() == 0);
            if (macro.isPresent()) {
                value = macro.get().evaluate();
            }
        }
        if (value == null) {
            value = EnvironmentVariables.getenv(variable).orElseThrow(() -> new BadSyntax("Variable '" + variable + "' is not defined"));
        }
        return replace(processor, values, new StringBuilder(value), depth);
    }
}
