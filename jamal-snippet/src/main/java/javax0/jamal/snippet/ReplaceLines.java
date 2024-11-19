package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.ArrayList;
import java.util.List;

public class ReplaceLines implements Macro, InnerScopeDependent, BlockConverter, Scanner.FirstLine {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var replace = scanner.list("replace");
        // throw BadSyntax if there was nothing changed
        final var detectNoChange = scanner.bool("detectNoChange");
        scanner.done();

        convertTextBlock(processor, in.getSB(), in.getPosition(), replace.getParam(), detectNoChange.getParam());
        return in.toString();
    }

    @Override
    public void convertTextBlock(Processor processor, StringBuilder sb, Position pos, Params.Param<?>... params) throws BadSyntax {
        assertParams(2, params);
        final var replace = params[0].asList(String.class);
        final var detectNoChange = params[1].asBoolean();
        List<Boolean> noChangeList = new ArrayList<>();
        List<String> fromList = new ArrayList<>();
        final var lines = sb.toString().split("\n", -1);
        for (final var replaceString : replace.get()) {
            final var parts = ReplaceUtil.chop(InputHandler.getParts(javax0.jamal.tools.Input.makeInput(replaceString), processor));
            BadSyntaxAt.when(parts.length == 0, "The replace macro should have at least one part: '" + replace.get() + "'", pos);
            for (int i = 0; i < parts.length; i += 2) {
                var noChange = detectNoChange.is();
                final var from = parts[i];
                for (int k = 0; k < lines.length; k++) {
                    final String to = ReplaceUtil.fetchElement(parts, i + 1);
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
                noChangeList.add(noChange);
                fromList.add(from);
            }
        }
        if (noChangeList.stream().anyMatch(it -> it)) {
            final var notFound = new StringBuilder();
            for (var j = 0; j < noChangeList.size(); j++) {
                final var noChange = noChangeList.get(j);
                if (noChange) {
                    notFound.append(fromList.get(j)).append("\n");
                }
            }
            throw new BadSyntaxAt("{@replaceLines did not find some of the search string.\n" +
                    "Search strings no found are:\n" +
                    notFound, pos);
        }
        sb.setLength(0);
        sb.append(String.join("\n", lines));
    }

    @Override
    public String getId() {
        return "replaceLines";
    }
}
