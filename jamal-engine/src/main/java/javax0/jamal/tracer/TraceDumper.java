package javax0.jamal.tracer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TraceDumper {
    private static final long LAG = 80L;
    private static final String END_TAG = "</traces>";

    private static String cData(String string) {
        return "<![CDATA[" + cDataEscape(string) + "]]>";
    }

    private static String cDataEscape(String string) {
        return string.replaceAll("]]>", "]]]]<![CDATA[>");
    }

    public void dump(List<TraceRecord> traces, String fileName, Exception ex) {
        final String adjustedFileName;
        if (fileName.startsWith("~/")) {
            adjustedFileName = System.getProperty("user.home") + fileName.substring(2);
        } else {
            adjustedFileName = fileName;
        }
        dumpXml(traces, adjustedFileName, ex);
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
            (trace.getId().length() > 0 ? "name=\"" + trace.getId() + "\" " : "") +
            "type=\"" + trace.type() + "\" " +
            "level=\"" + trace.level() + "\" " +
            "index=\"" + i.get() + "\" " +
            ">\n");
        if (trace.getWarnings().size() > 0) {
            outputFile.writeBytes("<warnings>");
            for (final var warning : trace.getWarnings()) {
                outputFile.writeBytes("<warning>" + warning + "</warning>");
            }
            outputFile.writeBytes("</warnings>");
        }
        if (trace.type() == TraceRecord.Type.USER_DEFINED_MACRO && trace.getParameters() != null && trace.getParameters().length > 0) {
            outputFile.writeBytes("<parameters>");
            for (final var parameter : trace.getParameters()) {
                outputFile.writeBytes("<parameter>");
                outputFile.writeBytes(cData(parameter));
                outputFile.writeBytes("</parameter>");
            }
            outputFile.writeBytes("</parameters>");
        }
        outputFile.writeBytes("<position " +
            "column=\"" + trace.position().column + "\" " +
            "line=\"" + trace.position().line + "\" " +
            "file=\"" + trace.position().file + "\" " +
            "/>");
        if (!trace.beforeState().isEmpty()) {
            outputFile.writeBytes("<input>\n");
            outputFile.writeBytes(cData(trace.beforeState()));
            outputFile.writeBytes("\n</input>\n");
        }
        if (!trace.evaluatedState().isEmpty()) {
            outputFile.writeBytes("<evaluated>\n");
            outputFile.writeBytes(cData(trace.evaluatedState()));
            outputFile.writeBytes("\n</evaluated>\n");
        }
        if (TraceRecord.Type.TEXT == trace.type()) {
            outputFile.writeBytes("<text>\n");
            outputFile.writeBytes(cData(trace.resultState()));
            outputFile.writeBytes("\n</text>\n");
        } else {
            if (trace.resultState().length() == 0) {
                if (trace.hasOutput()) {
                    outputFile.writeBytes("<result></result>\n");
                } else {
                    outputFile.writeBytes("<result><error/></result>\n");
                }
            } else {
                outputFile.writeBytes("<result>\n");
                outputFile.writeBytes(cData(trace.resultState()));
                outputFile.writeBytes("\n</result>\n");
            }
        }
        if (trace.getSubRecords().size() > 0) {
            outputFile.writeBytes("<records>\n");
            for (final var subTrace : trace.getSubRecords()) {
                outputRecord(outputFile, i, subTrace);
            }
            outputFile.writeBytes("\n</records>\n");
        }
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
            if (index > -1) {
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
}
