package javax0.jamal.mavenextension;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelReader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.ReaderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is the extension class that implements the maven macro extension
 */
@Component(role = ModelProcessor.class)
public class CustomModelProcessor implements ModelProcessor {
    @Requirement
    private ModelReader modelReader;

    @Override
    public File locatePom(File projectDirectory) {

        convertExtensionsJam(projectDirectory);

        jam2Xml(projectDirectory, "pom", false);
        return new File(projectDirectory, "pom.xml");
    }

    /**
     * Asynchronously convert the extensions.jam file to extensions.xml.
     * The advantage of the asynchronous conversion is that the conversion, especially when downloading web resources, does not slow down the compilation.
     * Another side effect is the error report, which does happen if the extensions.jam has syntax errors but it does not stop the compilation.
     * <p>
     * The implementation creates a new thread.
     * Since this task is started only once for a built, which is usually a few minutes typically, there is no need to use any executor service.
     *
     * @param projectDirectory is the root of the project directory provided by Jamal
     */
    private void convertExtensionsJam(final File projectDirectory) {
        createTouchFile(projectDirectory);
        final var t = new Thread(() -> {
            final var dotMvnDir = new File(projectDirectory, ".mvn");
            if (dotMvnDir.exists()) {
                jam2Xml(dotMvnDir, "extensions", true);
            }
        });
        t.setDaemon(false); // should finish before we exit the process
        t.setName("extensions.jam-to-extensions.xml");
        t.start();
    }

    /**
     * Create a touch file in the .mvn directory to signal that the extension executes.
     * <p>
     * This helps test the extension that it is compatible and runs with the actual Maven version.
     * It is a piece of code that gets into production code, so it is not nice, but seems fairly harmless.
     * <p>
     * The unit test of the extension checks that the touch file is there and that the time stamp is recent.
     * <p>
     * The touch file must exist in the .mvn directory and its name must be 'touch'.
     * It is NOT created if it does not exist, not to create garbage in real life projects.
     * <p>
     * This functionality is not a guaranteed feature of the extension; therefore, it is not documented in the README.
     *
     * @param projectDirectory is the root of the project directory provided by Jamal
     */
    private static void createTouchFile(File projectDirectory) {
        final var touchFile = new File(projectDirectory, ".mvn/touch");
        if (touchFile.exists()) {
            try {
                Files.writeString(Paths.get(touchFile.getAbsolutePath()), System.currentTimeMillis() + "\n", StandardCharsets.UTF_8);
            } catch (IOException ignore) {
            }
        }
    }

    private void jam2Xml(final File directory, final String sourceName, final boolean optional) {
        File jamFile = new File(directory, sourceName + ".xml.jam");
        if (!jamFile.exists()) {
            jamFile = new File(directory, sourceName + ".jam");
            if (!jamFile.exists()) {
                if (optional) {
                    return;
                } else {
                    throw new RuntimeException("There is no '" + sourceName + ".xml.jam' or '" + sourceName + ".jam' file.");
                }
            }
        }
        Processor processor = new javax0.jamal.engine.Processor();

        final String fileName = jamFile.getAbsolutePath();
        final String xml;
        try {
            xml = processor.process(FileTools.getInput(fileName, processor));
        } catch (BadSyntax e) {
            throw new RuntimeException("Jamal error processing the file " + fileName + "\n" + dumpException(e), e);
        }
        String formattedXml;
        try {
            formattedXml = formatOutput(xml);
        } catch (Exception e) {
            throw new RuntimeException("Cannot format the file " + fileName + "\n" + dumpException(e), e);
        }

        final File output = new File(directory, sourceName + ".xml");
        // noinspection ResultOfMethodCallIgnored
        output.setWritable(true);
        try (final OutputStream os = new FileOutputStream(output)) {
            os.write(formattedXml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Cannot write the '" + sourceName + ".xml' file.", e);
        }
    }

    private String dumpException(Throwable e) {
        return dumpException(e, new HashSet<>());
    }

    private String dumpException(Throwable e, Set<Throwable> processed) {
        if (e == null || processed.contains(e)) {
            return "";
        }
        processed.add(e);
        StringBuilder output = new StringBuilder();
        output.append(e.getMessage()).append("\n");
        try (final var sw = new StringWriter();
             final var pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            output.append(sw);
        } catch (IOException ioException) {
            // does not happen, StringWriter does not do anything in close
        }
        output.append(dumpException(e.getCause(), processed));
        for (final Throwable t : e.getSuppressed()) {
            output.append(dumpException(t, processed));
        }
        return output.toString();
    }

    private String formatOutput(String result) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(result)));
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        Writer out = new StringWriter();
        tf.transform(new DOMSource(doc), new StreamResult(out));
        return Arrays.stream(out.toString().split(System.lineSeparator())).filter(s -> s.trim().length() > 0).collect(Collectors.joining(System.lineSeparator()));
    }

    @Override
    public Model read(InputStream input, Map<String, ?> options) throws IOException {
        try (final Reader in = ReaderFactory.newPlatformReader(input)) {
            return read(in, options);
        }
    }

    @Override
    public Model read(Reader reader, Map<String, ?> options) throws IOException {
        return modelReader.read(reader, options);
    }

    @Override
    public Model read(File input, Map<String, ?> options) throws IOException {
        return read(new FileInputStream(input), options);
    }
}