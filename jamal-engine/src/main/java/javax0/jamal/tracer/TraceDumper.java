package javax0.jamal.tracer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceDumper {
    private static final String sep = "-".repeat(80);
    private static final long LAG = 80L;
    private static final String END_TAG = "</traces>";

    private static String cData(String string) {
        return "<![CDATA[" + cDataEscape(string) + "]]>";
    }

    private static String cDataEscape(String string) {
        return string.replaceAll("\\]\\]>", "]]]]<![CDATA[>");
    }

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
        try (var outputFile = new RandomAccessFile(fileName, "rw")) {
            amputateClosingXmlTag(outputFile);
            openNewTrace(outputFile, ex);
            final AtomicInteger i = new AtomicInteger(1);
            for (final var trace : traces) {
                outputRecord(outputFile, i, trace);
            }
            if (ex != null) {
                outputException(ex, outputFile);
            }
            outputFile.writeBytes("</trace>\n");
            outputFile.writeBytes(END_TAG);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    private void outputException(Exception ex, RandomAccessFile outputFile) throws IOException {
        outputFile.writeBytes("<exception " +
                " message=\"" + ex.getMessage().replaceAll("\n", " ") + "\"" +
                ">\n");
        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        sw.close();
        outputFile.writeBytes(cData(sw.toString()));
        outputFile.writeBytes("\n");
        outputFile.writeBytes("</exception>\n");
    }

    private void outputRecord(RandomAccessFile outputFile, AtomicInteger i, TraceRecord trace) throws IOException {
        outputFile.writeBytes("<record " +
                "id=\"" + trace.getId() + "\" " +
                "type=\"" + trace.type() + "\" " +
                "level=\"" + trace.level() + "\" " +
                "index=\"" + i.get() + "\" " +
                ">\n");
        outputFile.writeBytes("<position " +
                "column=\"" + trace.position().column + "\" " +
                "line=\"" + trace.position().line + "\" " +
                "file=\"" + trace.position().file + "\" " +
                "/>");
        if (!trace.source().isEmpty()) {
            outputFile.writeBytes("<input>\n");
            outputFile.writeBytes(cData(trace.source()));
            outputFile.writeBytes("\n</input>\n");
        }
        if (TraceRecord.Type.TEXT == trace.type()) {
            outputFile.writeBytes("<text>\n");
            outputFile.writeBytes(cData(trace.target()));
            outputFile.writeBytes("\n</text>\n");
        } else {
            if (trace.target().length() == 0) {
                if (trace.hasOutput()) {
                    outputFile.writeBytes("<output></output>\n");
                } else {
                    outputFile.writeBytes("<output><error/></output>\n");
                }
            } else {
                outputFile.writeBytes("<output>\n");
                outputFile.writeBytes(cData(trace.target()));
                outputFile.writeBytes("\n</output>\n");
            }
        }
        outputFile.writeBytes("<records>\n");
        for (final var subTrace : trace.getSubRecords()) {
            outputRecord(outputFile, i, subTrace);
        }
        outputFile.writeBytes("\n</records>\n");
        outputFile.writeBytes("</record>\n");
        i.incrementAndGet();
    }

    private void openNewTrace(RandomAccessFile outputFile, Exception ex) throws IOException {
        if (ex == null) {
            outputFile.writeBytes("<trace>\n");
        } else {
            outputFile.writeBytes("<trace hasException=\"true\">\n");
        }
    }

    private void amputateClosingXmlTag(RandomAccessFile outputFile) throws IOException {
        var lag = LAG;
        if (outputFile.length() < lag) {
            lag = outputFile.length();
        }
        if (lag > END_TAG.length()) {
            outputFile.seek(outputFile.length() - LAG);
            byte[] buffer = new byte[(int) lag];
            outputFile.readFully(buffer);
            var ending = new String(buffer, StandardCharsets.UTF_8);
            var index = ending.indexOf(END_TAG);
            if (index >= -1) {
                outputFile.seek(outputFile.length() - lag + index);
            } else {
                outputFile.seek(outputFile.length());
            }
        } else {
            outputFile.seek(outputFile.length());
            outputFile.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\n");
            outputFile.writeBytes("<traces>" + "\n");
        }
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
