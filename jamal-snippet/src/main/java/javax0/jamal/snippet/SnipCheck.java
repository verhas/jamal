package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.EnvironmentVariables;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scan;

import java.util.Locale;

public class SnipCheck implements Macro {

    // snipline SnipCheck_MIN_LINE
    public static final int MIN_LENGTH = 6;
    // snipline SnipCheck_JAMAL_SNIPPET_CHECK
    public static final String JAMAL_SNIPPET_CHECK = "JAMAL_SNIPPET_CHECK";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        if (EnvironmentVariables.getenv(JAMAL_SNIPPET_CHECK).filter(s -> s.equals("false")).isPresent()) {
            return "";
        }
        final var pos = in.getPosition();
        final var hashString = Params.<String>holder("hash", "hashCode").orElse("");
        final var lines = Params.<String>holder("lines").asInt();
        final var id = Params.<String>holder("id");
        final var fileName = Params.<String>holder("file", "files");
        final var message = Params.<String>holder("message").orElse("");
        Scan.using(processor).from(this).tillEnd().keys(hashString, lines, id, fileName, message).parse(in);
        BadSyntax.when(lines.isPresent() && hashString.isPresent(), "You cannot specify 'lines' and 'hash' the same time for snip:check");

        final String snippet = getSnippetContent(in, processor, id, fileName, message);

        if (hashString.isPresent()) {
            checkHashString(hashString, id, fileName, message, snippet, pos);
            return "";
        }

        if (lines.isPresent()) {
            checkLineCount(lines, id, fileName, message, snippet);
            return "";
        }
        throw new BadSyntax("Neither lines, nor hash is checked in " + getId() + "'" + message.get() + "'");
    }

    private void checkLineCount(Params.Param<Integer> lines, Params.Param<String> id, Params.Param<String> fileName, Params.Param<String> message, String snippet) throws BadSyntax {
        final var lastNl = snippet.charAt(snippet.length() - 1) == '\n' ? 0 : 1;
        final var newlines = snippet.replaceAll("[^\\n]", "").length() + lastNl;
        if (newlines == lines.get()) {
            return;
        }
        throw new BadSyntax("The " + getIdString(id, fileName) + " has " + newlines + " lines and not " + lines.get() + ".\n" + "'" + message.get() + "'");
    }

    private void checkHashString(final Params.Param<String> hashString,
                                 final Params.Param<String> id,
                                 final Params.Param<String> fileName,
                                 final Params.Param<String> message,
                                 final String snippet,
                                 final Position pos) throws BadSyntax {
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(snippet));
        final var hash = hashString.get().replaceAll("\\.", "").toLowerCase(Locale.ENGLISH);
        if (hash.length() < MIN_LENGTH) {
            BadSyntax.when(hashStringCalculated.contains(hash), "The %s hash is '%s'. '%s' is too short, you need at least %d characters.\n'%s'", getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), MIN_LENGTH, message.get());
            BadSyntax.when(true,"The %s hash is '%s', not '%s', which is too short anyway, you need at least %d characters.\n'%s'",
                    getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), MIN_LENGTH, message.get());
        }
        if (hashStringCalculated.contains(hash)) {
            return;
        }
        if (message.isPresent()) {
            throw new SnipCheckFailed(getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), message.get(), pos);
        } else {
            throw new SnipCheckFailed(getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), null, pos);
        }
    }

    public static String doted(final String s) {
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
                snippet.append(FileTools.getInput(absoluteFileName, processor));
            }
        }
        BadSyntax.when(!id.isPresent() && !fileNames.isPresent(),  "You have to specify either 'id' or 'fileName' for snip:check\n'%s'", message.get());
        return snippet.toString();
    }

    @Override
    public String getId() {
        return "snip:check";
    }
}
