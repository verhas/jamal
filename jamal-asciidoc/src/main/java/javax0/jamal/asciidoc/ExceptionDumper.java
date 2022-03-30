package javax0.jamal.asciidoc;

import javax0.jamal.snippet.SnipCheckFailed;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to print out an exception stack trace and cause stack trace and suppressed exceptions.
 */
class ExceptionDumper {

    /**
     * Create an exception report.
     *
     * @param t the exception to be printed
     * @return a StringBuilder that contains the message of the exception, the stack trace and then recursively
     * all the suppressed exceptions and the causing exception in a similar manner.
     */
    public static StringBuilder dump(Exception t) {
        final var me = new ExceptionDumper();
        me.output.append(t.getMessage()).append('\n');
        me.dumpIt(t, false);
        me.output.append("sed '" + me.sedCommand + "'");
        return me.output;
    }

    final StringBuilder sedCommand = new StringBuilder();
    final StringBuilder output = new StringBuilder();
    final Set<Throwable> processed = new HashSet<>();

    private void dumpIt(Throwable t, boolean printMessage) {
        if (t == null || processed.contains(t)) {
            return;
        }
        if (t instanceof SnipCheckFailed) {
            sedCommand.append(((SnipCheckFailed) t).sed()).append(";");
        }
        processed.add(t);
        if (printMessage) {
            output.append(t.getMessage());
        }
        try (final var sw = new StringWriter();
             final var pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            output.append(sw);
        } catch (IOException ioException) {
            // does not happen, StringWriter does not do anything in close
        }
        if (t.getSuppressed().length > 0) {
            output.append("Suppressed exceptions:\n");
            for (final var sup : t.getSuppressed()) {
                dumpIt(sup, true);
            }
        }
        if (t.getCause() != null) {
            output.append("Causing Exception:\n");
            dumpIt(t.getCause(), true);
        }
    }


}
