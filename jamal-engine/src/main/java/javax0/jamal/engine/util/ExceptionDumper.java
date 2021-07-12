package javax0.jamal.engine.util;

import javax0.jamal.api.BadSyntaxAt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

public class ExceptionDumper {

    public static StringBuilder dump(BadSyntaxAt t) {
        final var me = new ExceptionDumper();
        final var pos = t.getPosition();
        me.output.append(t.getMessage()).append(" at ").append(pos.file).append('/')
            .append(pos.line).append(':').append(pos.column).append('\n');
        me.dumpIt(t, false);
        return me.output;
    }

    final StringBuilder output = new StringBuilder();
    final Set<Throwable> processed = new HashSet<>();

    private void dumpIt(Throwable t, boolean printMessage) {
        if (t == null || processed.contains(t)) {
            return;
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
