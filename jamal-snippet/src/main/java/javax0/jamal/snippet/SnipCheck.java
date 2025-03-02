package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;

import java.util.Locale;

@Macro.Name("snip:check")
public class SnipCheck implements Macro, Scanner.WholeInput {

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
        final var scanner = newScanner(in, processor);
        final var hashString = scanner.str("hash", "hashCode").defaultValue("");
        final var lines = scanner.number("lines").optional();
        final var id = scanner.str("id").optional();
        final var fileName = scanner.str("file", "files").optional();
        final var message = scanner.str("message").defaultValue("");
        final var warning = scanner.bool("snipCheckWarningOnly", "warning", "warningOnly");
        final var error = scanner.bool("snipCheckError", "error", "errorLog");
        scanner.done();

        BadSyntax.when(lines.isPresent() && hashString.isPresent(), "You cannot specify 'lines' and 'hash' the same time for snip:check");
        BadSyntax.when(warning.is() && error.is(), "You cannot specify 'warning' and 'error' the same time for snip:check");

        final String snippet = getSnippetContent(in, processor, id, fileName, message);

        if (hashString.isPresent()) {
            checkHashString(hashString, id, fileName, message, warning, error, snippet, pos, processor);
            return "";
        }

        if (lines.isPresent()) {
            checkLineCount(lines, id, fileName, message, warning, error, snippet, pos, processor);
            return "";
        }
        processor.deferredThrow("Neither lines, nor hash is checked in %s '%s'", getId(), message.get());
        return "";
    }

    private void checkLineCount(final IntegerParameter lines,
                                final StringParameter id,
                                final StringParameter fileName,
                                final StringParameter message,
                                final BooleanParameter warning,
                                final BooleanParameter error,
                                final String snippet,
                                final Position pos,
                                final Processor processor) throws BadSyntax {
        final var lastNl = snippet.charAt(snippet.length() - 1) == '\n' ? 0 : 1;
        final var newlines = snippet.replaceAll("[^\\n]", "").length() + lastNl;
        if (newlines == lines.get()) {
            return;
        }
        if (warning.is()) {
            processor.logger().log(System.Logger.Level.WARNING, pos, "The " + getIdString(id, fileName) + " has " + newlines + " lines and not " + lines.get() + ".\n" + "'" + message.get() + "'");
            return;
        }
        if (error.is()) {
            processor.logger().log(System.Logger.Level.ERROR, pos, "The " + getIdString(id, fileName) + " has " + newlines + " lines and not " + lines.get() + ".\n" + "'" + message.get() + "'");
        }
        processor.deferredThrow("The %s has %d lines and not %d.\n'%s'", getIdString(id, fileName), newlines, lines.get(), message.get());
    }

    private void checkHashString(final StringParameter hashString,
                                 final StringParameter id,
                                 final StringParameter fileName,
                                 final StringParameter message,
                                 final BooleanParameter warning,
                                 final BooleanParameter error,
                                 final String snippet,
                                 final Position pos,
                                 final Processor processor) throws BadSyntax {
        final var hashStringCalculated = HexDumper.encode(SHA256.digest(snippet));
        final var hash = hashString.get().replaceAll("\\.", "").toLowerCase(Locale.ENGLISH);
        if (hash.length() < MIN_LENGTH) {
            BadSyntax.when(hashStringCalculated.contains(hash), "The %s hash is '%s'. '%s' is too short, you need at least %d characters.\n'%s'", getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), MIN_LENGTH, message.get());
            BadSyntax.format("The %s hash is '%s', not '%s', which is too short anyway, you need at least %d characters.\n'%s'",
                    getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), MIN_LENGTH, message.get());
        }
        if (hashStringCalculated.contains(hash)) {
            return;
        }
        if (warning.is()) {
            processor.logger().log(System.Logger.Level.WARNING, pos, "The %s hash is '%s', not '%s'.\n'%s'", getIdString(id, fileName), doted(hashStringCalculated), hashString.get(),
                    message.isPresent() ? message.get() : "");
            return;
        }
        if (error.is()) {
            processor.logger().log(System.Logger.Level.ERROR, pos, "The %s hash is '%s', not '%s'.\n'%s'", getIdString(id, fileName), doted(hashStringCalculated), hashString.get(),
                    message.isPresent() ? message.get() : "");
        }
        processor.deferredThrow(new SnipCheckFailed(getIdString(id, fileName), doted(hashStringCalculated), hashString.get(), message.orNull(), pos));
    }

    public static String doted(final String s) {
        return s.replaceAll("([0-9a-fA-F]{8})(?!$)", "$1.");
    }

    private String getIdString(StringParameter id, StringParameter fileName) throws BadSyntax {
        final var sb = new StringBuilder();
        if (id.isPresent()) {
            sb.append("id(").append(id.get()).append(")");
        } else {
            sb.append("file(").append(fileName.get()).append(")");
        }
        return sb.toString();
    }

    private String getSnippetContent(Input in, Processor processor, StringParameter id, StringParameter fileNames, StringParameter message) throws BadSyntax {
        final StringBuilder snippet = new StringBuilder();
        if (id.isPresent()) {
            for (final var snipid : id.get().split(",")) {
                processor.deferBadSyntax(() -> snippet.append(SnippetStore.getInstance(processor).snippet(snipid.trim())));
            }
        }
        if (fileNames.isPresent()) {
            var reference = in.getReference();
            for (final var fileName : fileNames.get().split(",")) {
                var absoluteFileName = FileTools.absolute(reference, fileName.trim());
                snippet.append(FileTools.getInput(absoluteFileName, processor));
            }
        }
        BadSyntax.when(!id.isPresent() && !fileNames.isPresent(), "You have to specify either 'id' or 'fileName' for snip:check\n'%s'", message.get());
        return snippet.toString();
    }

}
