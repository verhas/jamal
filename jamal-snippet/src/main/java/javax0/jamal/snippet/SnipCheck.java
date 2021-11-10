package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;

import java.util.Locale;

public class SnipCheck implements Macro {

    // snipline SnipCheck_MIN_LINE
    private static final int MIN_LENGTH = 6;

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var hashString = Params.<String>holder("hash", "hashCode").orElse("");
        final var lines = Params.<String>holder("lines").asInt();
        final var id = Params.<String>holder("id");
        final var fileName = Params.<String>holder("file", "files");
        final var message = Params.<String>holder("message").orElse("");
        Params.using(processor).from(this).tillEnd().keys(hashString, lines, id, fileName, message).parse(in);
        if (lines.isPresent() && hashString.isPresent()) {
            throw new BadSyntax("You cannot specify 'lines' and 'hash' the same time for snip:check");
        }

        final String snippet = getSnippetContent(in, processor, id, fileName, message);

        if (hashString.isPresent()) {
            return checkHashString(hashString, id, fileName, message, snippet);
        }

        if (lines.isPresent()) {
            return checkLineCount(lines, id, fileName, message, snippet);
        }
        throw new BadSyntax("Neither lines, nor hash is checked in " + getId() + "'" + message.get() + "'");
    }

    private String checkLineCount(Params.Param<Integer> lines, Params.Param<String> id, Params.Param<String> fileName, Params.Param<String> message, String snippet) throws BadSyntax {
        final var newlines = snippet.replaceAll("[^\\n]", "").length();
        if (newlines == lines.get()) {
            return "";
        }
        throw new BadSyntax("The " + getIdString(id, fileName) + " has " + newlines + " lines and not " + lines.get() + ".\n" + "'" + message.get() + "'");
    }

    private String checkHashString(Params.Param<String> hashString,
                                   Params.Param<String> id,
                                   Params.Param<String> fileName,
                                   Params.Param<String> message,
                                   String snippet) throws BadSyntax {
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(snippet));
        final var hash = hashString.get().replaceAll("\\.","").toLowerCase(Locale.ENGLISH);
        if (hash.length() < MIN_LENGTH) {
            if (hashStringCalculated.contains(hash)) {
                throw new BadSyntax("The " + getIdString(id, fileName) + " hash is '" + doted(hashStringCalculated) + "'. '" +
                    hashString.get() + "' is too short, you need at least " + MIN_LENGTH +
                    " characters.\n" + "'" + message.get() + "'");
            } else {
                throw new BadSyntax("The " + getIdString(id, fileName) + " hash is '" + doted(hashStringCalculated) + "', not '" +
                    hashString.get() + "', which is too short anyway, you need at least " + MIN_LENGTH +
                    " characters.\n" + "'" + message.get() + "'");
            }
        }
        if (hashStringCalculated.contains(hash)) {
            return "";
        }
        throw new BadSyntax("The " + getIdString(id, fileName) + " hash is '" + doted(hashStringCalculated) +
            "' does not contain '" + hashString.get() + "'.\n" + "'" + message.get() + "'");
    }

    private static String doted(final String s){
        return s.replaceAll("([0-9a-fA-F]{8})(?!$)", "$1.");
    }

    private String getIdString(Params.Param<String> id, Params.Param<String> fileName) throws BadSyntax {
        final var sb = new StringBuilder();
        if (id.isPresent()) {
            sb.append("id(").append(id.get()).append(")");
        } else {
            sb.append("file(").append(fileName.get()).append(")");
        }
        return sb.toString();
    }

    private String getSnippetContent(Input in, Processor processor, Params.Param<String> id, Params.Param<String> fileNames, Params.Param<String> message) throws BadSyntax {
        final StringBuilder snippet = new StringBuilder();
        if (id.isPresent()) {
            for (final var snipid : id.get().split(",")) {
                snippet.append(SnippetStore.getInstance(processor).snippet(snipid.trim()));
            }
        }
        if (fileNames.isPresent()) {
            var reference = in.getReference();
            for (final var fileName : fileNames.get().split(",")) {
                var absoluteFileName = FileTools.absolute(reference, fileName.trim());
                snippet.append(FileTools.getInput(absoluteFileName));
            }
        }
        if (!id.isPresent() && !fileNames.isPresent()) {
            throw new BadSyntax("You have to specify either 'id' or 'fileName' for snip:check\n" + "'" + message.get() + "'");
        }
        return snippet.toString();
    }

    @Override
    public String getId() {
        return "snip:check";
    }
}
