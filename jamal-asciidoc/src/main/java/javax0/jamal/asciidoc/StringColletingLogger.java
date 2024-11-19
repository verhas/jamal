package javax0.jamal.asciidoc;

import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;

/**
 * A {@code StringCollectingLogger} is an implementation of {@link Processor.Logger} that collects log messages into an internal
 * {@link StringBuilder}, allowing log entries to be retrieved as a single formatted string.
 * <p>
 * This logger can be used to display the logs in the output when an interactive tool,
 * AsciiDoc preprocessor in IntelliJ plugin is used to process the input.
 */
public class StringColletingLogger implements Processor.Logger {
    /**
     * A {@link StringBuilder} that accumulates log messages.
     */
    private final StringBuilder sb = new StringBuilder();

    /**
     * Logs a message at the specified logging {@code level} with the provided {@code format} and optional {@code params}.
     * Each log entry is formatted according to the provided format string, and appended to the internal {@link StringBuilder}.
     *
     * @param level  the logging level, represented by {@link System.Logger.Level}, indicating the severity of the log message.
     * @param pos    the position within the source code or application where the log was generated; this may be {@code null}.
     * @param format the format string, compatible with {@link String#format}, specifying the log message template.
     * @param params the parameters to be formatted into the log message.
     */
    @Override
    public void log(final System.Logger.Level level, final Position pos, final String format, final String... params) {
        sb.append(level.toString()).append(": ").append(String.format(format, (Object[]) params)).append("\n");
    }

    /**
     * Returns all log entries collected by this logger as a single formatted {@link String}.
     * Each log entry is separated by a newline character.
     *
     * @return the concatenated log entries as a string.
     */
    @Override
    public String toString() {
        return sb.toString();
    }
}
