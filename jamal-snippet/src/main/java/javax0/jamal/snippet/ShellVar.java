package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.HashMap;
import java.util.Map;

public class ShellVar implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var parameters = Params.<String>holder("variables").orElse("");
        Scan.using(processor).from(this).between("()").keys(parameters).parse(in);
        final Map<String, String> values = new HashMap<>();
        Params.using(processor).tillEnd().parse(javax0.jamal.tools.Input.makeInput(parameters.get()), values::put, s -> true);

        final var sb = in.getSB();
        replace(processor, values, sb,0);
        return sb.toString();
    }

    private static String replace(Processor processor, Map<String, String> values, StringBuilder sb, int depth) throws BadSyntax {
        if( depth > 100 ){
            throw new BadSyntax("Too deep recursion in shell variable substitution");
        }
        int start = 0;
        for (; ; ) {
            int i = sb.indexOf("$", start);
            if (i > 0 && sb.charAt(i - 1) == '\\') {
                start = i + 1;
                continue;
            }
            if (i < 0 || i == sb.length() - 1) {
                break;
            }
            if (sb.charAt(i + 1) == '{') {
                int j = sb.indexOf("}", i + 2);
                if (j < 0) {
                    throw new BadSyntax("Missing '}' in the shell variable substitution");
                }
                String var = sb.substring(i + 2, j);
                String value = getVariable(var, values, processor, depth + 1);
                sb.replace(i, j + 1, value);
                start = i + value.length();
            } else {
                int j = i + 1;
                if (j < sb.length() && Macro.validId1stChar(sb.charAt(j))) {
                    j++;
                } else {
                    throw new BadSyntax("Missing variable name after '$' in the shell variable substitution");
                }
                while (j < sb.length() && Macro.validIdChar(sb.charAt(j)) && sb.charAt(j) != '$' ) {
                    j++;
                }
                String var = sb.substring(i + 1, j);
                String value = getVariable(var, values, processor, depth + 1);
                sb.replace(i, j, value);
            }
        }
        return sb.toString();
    }

    private static String getVariable(String var, final Map<String, String> values, Processor processor, int depth) throws BadSyntax {
        if (var.contains("$")) {
            final var sb = new StringBuilder(var);
            var = replace(processor, values, sb, depth);
        }

        String value = values.get(var);
        if (value == null) {
            final var macro = processor.getRegister().getUserDefined(var)
                    .filter(m -> m instanceof Evaluable)
                    .map(m -> ((Evaluable) m))
                    .filter(m -> m.expectedNumberOfArguments() == 0);
            if (macro.isPresent()) {
                value = macro.get().evaluate();
            }
        }
        if (value == null) {
            System.getProperty(var);
        }
        if (value == null) {
            value = System.getenv(var);
        }
        if (value == null) {
            throw new BadSyntax("Variable '" + var + "' is not defined");
        }
        return replace(processor, values, new StringBuilder(value), depth);
    }

    @Override
    public String getId() {
        return "shell:var";
    }
}
