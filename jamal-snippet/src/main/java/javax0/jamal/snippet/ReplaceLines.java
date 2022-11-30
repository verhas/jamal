package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import static javax0.jamal.tools.Params.holder;

public class ReplaceLines implements Macro, InnerScopeDependent, BlockConverter {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var replace = holder("replace").asList(String.class);
        // throw BadSyntax if there was nothing changed
        final var detectNoChange = holder("detectNoChange").asBoolean();
        Scan.using(processor).from(this).firstLine().keys(replace, detectNoChange).parse(in);

        convertTextBlock(in.getSB(), in.getPosition(), replace, detectNoChange);
        return in.toString();
    }

    @Override
    public void convertTextBlock(StringBuilder sb, Position pos, Params.Param<?>... params) throws BadSyntax {
        assertParams(2, params);
        final var replace = params[0].asList(String.class);
        final var detectNoChange = params[1].asBoolean();
        boolean noChange = detectNoChange.get();
        final var lines = sb.toString().split("\n", -1);
        for (final var replaceString : replace.get()) {
            final var parts = InputHandler.getParts(javax0.jamal.tools.Input.makeInput(replaceString));
            BadSyntaxAt.when(parts.length == 0,"The replace macro should have at least one part: '" + replace.get() + "'",pos);

            for (int k = 0; k < lines.length; k++) {
                for (int i = 0; i < parts.length; i += 2) {
                    final var from = parts[i];
                    final String to;
                    if (i < parts.length - 1) {
                        to = parts[i + 1];
                    } else {
                        to = "";
                    }
                    try {
                        final var modified = lines[k].replaceAll(from, to);
                        if (noChange && !modified.equals(lines[k])) {
                            noChange = false;
                        }
                        lines[k] = modified;
                    } catch (Exception e) {
                        throw new BadSyntax("There is a problem with the regular expression in macro 'replaceLines' : "
                                + from + "\n" + to + "\n", e);
                    }
                }
            }
        }
        BadSyntaxAt.when(noChange,"{@replaceLines did not change any of the lines.",pos);
        sb.setLength(0);
        sb.append(String.join("\n", lines));
    }

    @Override
    public String getId() {
        return "replaceLines";
    }
}
