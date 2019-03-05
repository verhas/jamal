package javax0.jamal.tracer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class TraceDumper {
    private static final String sep = "-".repeat(80);
    private static final long LAG = 80L;
    private static final String END_TAG = "</traces>";

    public void dump(List<TraceRecord> traces, String fileName, Exception ex) {
        final String adjustedFileName;
        if (fileName.startsWith("~/")) {
            adjustedFileName = System.getProperty("user.home") + fileName.substring(2);
        } else {
            adjustedFileName = fileName;
        }
        if (fileName.endsWith(".xml")) {
            dumpXml(traces, adjustedFileName, ex);
        } else {
            dumpText(traces, adjustedFileName, ex);
        }
    }

    private void dumpXml(List<TraceRecord> traces, String fileName, Exception ex) {
        try {
            if (!new File(fileName).exists()) {
                new FileOutputStream(fileName).close();
            }
        } catch (Exception fnfe) {
            fnfe.printStackTrace(System.err);
        }
        try (var raf = new RandomAccessFile(fileName, "rw")) {
            var lag = LAG;
            if (raf.length() < lag) {
                lag = raf.length();
            }
            if (lag > END_TAG.length()) {
                raf.seek(raf.length() - LAG);
                byte[] buffer = new byte[(int) lag];
                raf.readFully(buffer);
                var ending = new String(buffer, StandardCharsets.UTF_8);
                var index = ending.indexOf(END_TAG);
                if (index >= -1) {
                    raf.seek(raf.length() - lag + index);
                } else {
                    raf.seek(raf.length());
                }
            } else {
                raf.seek(raf.length());
                raf.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
                raf.writeBytes("<traces>" + "\n");
            }
            if (ex == null) {
                raf.writeBytes("<trace>\n");
            } else {
                raf.writeBytes("<trace hasException=\"true\">\n");
            }
            int i = 1;
            for (final var trace : traces) {
                raf.writeBytes("<record " +
                        "column=\"" + trace.position().column + "\" " +
                        "line=\"" + trace.position().line + "\" " +
                        "file=\"" + trace.position().file + "\" " +
                        "type=\"" + trace.type() + "\" " +
                        "level=\"" + trace.level() + "\" " +
                        "index=\"" + i + "\" " +
                        ">\n");
                if (!trace.source().isEmpty()) {
                    raf.writeBytes("<input>\n");
                    raf.writeBytes(cData(trace.source()));
                    raf.writeBytes("\n</input>\n");
                }
                if ("TEXT".equals(trace.type())) {
                    raf.writeBytes("<text>\n");
                    raf.writeBytes(cData(trace.target()));
                    raf.writeBytes("\n</text>\n");
                } else {
                    if (trace.target().length() == 0) {
                        if (trace.hasOutput()) {
                            raf.writeBytes("<output></output>\n");
                        } else {
                            raf.writeBytes("<output><error/></output>\n");
                        }
                    } else {
                        raf.writeBytes("<output>\n");
                        raf.writeBytes(cData(trace.target()));
                        raf.writeBytes("\n</output>\n");
                    }
                }
                raf.writeBytes("</record>\n");
                i++;
            }
            if (ex != null) {
                raf.writeBytes("<exception " +
                        " message=\"" + ex.getMessage().replaceAll("\n", " ") + "\"" +
                        ">\n");
                var sw = new StringWriter();
                var pw = new PrintWriter(sw);
                ex.printStackTrace(pw);
                sw.close();
                raf.writeBytes(cData(sw.toString()));
                raf.writeBytes("\n");
                raf.writeBytes("</exception>\n");
            }
            raf.writeBytes("</trace>\n");
            raf.writeBytes(END_TAG);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private static String cData(String string){
        return "<![CDATA[" + cDataEscape(string) + "]]>";
    }

    private static String cDataEscape(String string){
        return string.replaceAll("\\]\\]>","]]]]<![CDATA[>");
    }

    private void dumpText(List<TraceRecord> traces, String fileName, Exception ex) {
        try (final var fos = new FileOutputStream(fileName, true);
             final var pw = new PrintWriter(fos)) {
            pw.println("#".repeat(80));
            pw.println("###" + " ".repeat(74) + "###");
            pw.println("###" + " ".repeat(74) + "###");
            pw.println("###" + " ".repeat(34) + " TRACE" + " ".repeat(34) + "###");
            pw.println("###" + " ".repeat(74) + "###");
            pw.println("###" + " ".repeat(74) + "###");
            pw.println("#".repeat(80));
            int i = 1;
            for (final var trace : traces) {
                final var tab = " ".repeat(trace.level());
                pw.println(tab + "LEVEL:" + trace.level() + " record " + i + ".");
                pw.println(tab + trace.position().file + "/" + trace.position().line + ":" + trace.position().column);
                pw.println(tab + trace.type());
                if (!trace.source().isEmpty()) {
                    pw.println(tab + "SOURCE:");
                    Arrays.stream(trace.source().split("\n")).map(s -> tab + s).forEach(pw::println);
                }
                if ("TEXT".equals(trace.type())) {
                    pw.println(tab + "COPIED:");
                } else {
                    pw.println(tab + "CONVERTED:");
                }
                Arrays.stream(trace.target().split("\n")).map(s -> tab + s).forEach(pw::println);
                pw.println(tab + sep);
                i++;
            }
            if (ex != null) {
                ex.printStackTrace(pw);
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
