package javax0.jamal.tracer;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class TraceDumper {

    public void dump(List<TraceRecord> traces, String fileName) {
        try (final var fos = new FileOutputStream(fileName, true);
             final var pw = new PrintWriter(fos)) {
            int i = 1;
            for (final var trace : traces) {
                final var tab = " ".repeat(trace.level());
                pw.println(tab + "record " + i + ".");
                pw.println(tab + trace.source());
                pw.println(tab + "---------------------------------------------");
                pw.println(tab + trace.target());
                pw.println(tab + "---------------------------------------------");
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
