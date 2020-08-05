package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static javax0.jamal.extensions.UDMacro.macro;
import static javax0.jamal.tools.FileTools.absolute;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Snippet implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var snippetFile = macro("snippetFile").from(processor).orElse(null);
        if (snippetFile == null) {
            throw new BadSyntax("snippetFile is null");
        }
        final var snippetStart = macro("snippetStart").from(processor).orElse("snippet\\s+");
        final var snippetEnd = macro("snippetEnd").from(processor).orElse("snippet\\s+end");
        skipWhiteSpaces(in);
        final var snippetId = in.toString().trim();
        var reference = in.getReference();
        var fileName = absolute(reference, snippetFile);
        final String content;
        try {
            content = Files.lines(Paths.get(fileName)).collect(Collectors.joining("\n"));
        } catch (IOException ioe) {
            throw new BadSyntax("Snippet file '" + fileName + "' cannot be read.", ioe);
        }
        final var start = Pattern.compile(snippetStart + Pattern.quote(snippetId));
        final var end = Pattern.compile(snippetEnd);
        final var startMatcher = start.matcher(content);
        final var endMatcher = end.matcher(content);
        if (startMatcher.find()) {
            int i = startMatcher.end();
            while (i < content.length() && content.charAt(i) != '\n') i++;
            if (i < content.length()) i++;
            if (endMatcher.find(i)) {
                int j = endMatcher.start();
                while (j > i && content.charAt(j) != '\n') j--;
                return content.substring(i, j);
            }
        }
        throw new BadSyntax("There is no snippet named '" + snippetId + "' in the file '" + snippetFile + "'");
    }
}
