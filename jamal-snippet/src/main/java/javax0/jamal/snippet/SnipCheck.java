package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;

// snippet SnipCheck
public class SnipCheck implements Macro {

    private static final int MIN_LENGTH = 6;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var hashString = Params.<String>holder("hash", "hashCode").orElse("");
        final var lines = Params.<String>holder("lines").asInt();
        final var id = Params.<String>holder("id");
        final var fileName = Params.<String>holder("file");
        Params.using(processor).from(this).keys(hashString, lines, id, fileName).parse(in);
        if (id.isPresent() && fileName.isPresent()) {
            throw new BadSyntax("You cannot specify 'id' and 'file' the same time for snip:check");
        }

        final String snippet = getSnippetContent(in, processor, id, fileName);

        if (hashString.isPresent()) {
            return checkHashString(hashString, id, fileName, snippet);
        }

        if (lines.isPresent()) {
            return checkLineCount(lines, id, fileName, snippet);
        }
        return "";
    }

    private String checkLineCount(Params.Param<Integer> lines, Params.Param<String> id, Params.Param<String> fileName, String snippet) throws BadSyntax {
        final var newlines = snippet.replaceAll("[^\\n]", "").length();
        if (newlines == lines.get()) {
            return "";
        }
        if (id.isPresent()) {
            throw new BadSyntax("The snippet '" + id.get() + "' has " + newlines + " lines and not " + lines.get() + ".");
        } else {
            throw new BadSyntax("The file '" + fileName.get() + "' hash " + newlines + " lines and not " + lines.get() + ".");
        }
    }

    private String checkHashString(Params.Param<String> hashString, Params.Param<String> id, Params.Param<String> fileName, String snippet) throws BadSyntax {
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(snippet));
        if (hashString.get().length() < MIN_LENGTH) {
            if (id.isPresent()) {
                throw new BadSyntax("The snippet '" + id.get() + "' hash is '" + hashStringCalculated + "'.");
            } else {
                throw new BadSyntax("The file '" + fileName.get() + "' hash is '" + hashStringCalculated + "'.");
            }
        }
        if (hashStringCalculated.endsWith(hashString.get())) {
            return "";
        }
        if (id.isPresent()) {
            throw new BadSyntax("The snippet '" + id.get() + "' hash is '" + hashStringCalculated + "' does not end with '" + hashString.get() + "'.");
        } else {
            throw new BadSyntax("The file '" + fileName.get() + "' hash is '" + hashStringCalculated + "' does not end with '" + hashString.get() + "'.");
        }
    }

    private String getSnippetContent(Input in, Processor processor, Params.Param<String> id, Params.Param<String> fileName) throws BadSyntax {
        final String snippet;
        if (id.isPresent()) {
            snippet = SnippetStore.getInstance(processor).snippet(id.get());
        } else if (fileName.isPresent()) {
            var reference = in.getReference();
            var absoluteFileName = FileTools.absolute(reference, fileName.get());
            snippet = FileTools.getInput(absoluteFileName).toString();
        } else {
            throw new BadSyntax("You have to specify either 'id' or 'fileName' for snip:check");
        }
        return snippet;
    }

    @Override
    public String getId() {
        return "snip:check";
    }
}
//end snippet
