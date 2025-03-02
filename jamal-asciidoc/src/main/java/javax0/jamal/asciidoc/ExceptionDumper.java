package javax0.jamal.asciidoc;

import javax0.jamal.snippet.SnipCheckFailed;

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
    public static StringBuilder dump(final Exception t, final String inputFileName) {
        final var me = new ExceptionDumper();
        me.output.append(t.getMessage()).append('\n');
        me.dumpIt(t, false);

        final var fn = t instanceof SnipCheckFailed ? ((SnipCheckFailed) t).getPosition().file : inputFileName;
        me.output.append("sed -i.bak ").append(" '").append(me.sedCommand).append("' ").append(fn);
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
        output.append("<button onclick=\"this.nextElementSibling.style.display=(this.nextElementSibling.style.display==='none'?'inline':'none');\">[+]</button><span style=\"display:none;\">\n");
        for (final var s : t.getStackTrace()) {
            if (s.getClassName().startsWith("javax0.jamal")) {
                output.append(String.format("\t%s(%s:%d)\n", s.getClassName(), s.getMethodName(), s.getLineNumber()));
            }
        }
        output.append("</span><br/>\n");
        if (t.getCause() != null) {
            output.append("Causing Exception:\n");
            dumpIt(t.getCause(), true);
        }
        if (t.getSuppressed().length > 0) {
            output.append("Suppressed exceptions:\n");
            for (final var sup : t.getSuppressed()) {
                dumpIt(sup, true);
            }
        }
    }
}
