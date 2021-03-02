package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.MacroReader;

import static javax0.jamal.snippet.SkipLines.needsNoExtraNl;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces2EOL;

public class ReplaceLines implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        boolean noChange = true;
        final var reader = MacroReader.macro(processor);
        final var replace = reader.readValue("replace").orElseThrow(
            () -> new BadSyntaxAt("The macro replaceLines needs a defined 'replace' user defined macro.", in.getPosition()));
        final var parts = InputHandler.getParts(javax0.jamal.tools.Input.makeInput(replace));
        if (parts.length == 0) {
            throw new BadSyntaxAt("The replace macro should have at least one part: '" + replace + "'", in.getPosition());
        }
        skipWhiteSpaces2EOL(in);
        final var lines = in.toString().split("\n", -1);
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
                    if( noChange && !modified.equals(lines[k])){
                        noChange = false;
                    }
                    lines[k] = modified;
                } catch (Exception e) {
                    throw new BadSyntax("There is a problem with the regular expression in macro 'replaceLines' : "
                        + from + "\n" + to + "\n", e);
                }
            }
        }
        if( noChange ){
            throw new BadSyntaxAt("{@replaceLines did not change any of the lines.",in.getPosition());
        }
        final var joined = String.join("\n", lines);
        if (needsNoExtraNl(in, true, joined)) {
            return joined;
        } else {
            return joined + "\n";
        }
    }

    @Override
    public String getId() {
        return "replaceLines";
    }
}
