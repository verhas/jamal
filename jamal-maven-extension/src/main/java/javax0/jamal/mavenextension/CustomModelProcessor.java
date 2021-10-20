package javax0.jamal.mavenextension;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.ModelParseException;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component(role = ModelProcessor.class)
public class CustomModelProcessor implements ModelProcessor {
    @Requirement
    private ModelReader modelReader;

    @Override
    public File locatePom(File projectDirectory) {
        File jamFile = new File(projectDirectory, "pom.xml.jam");
        if (!jamFile.exists()) {
            throw new RuntimeException("There is no 'pom.xml.jam' file.");
        }
        Processor processor = new javax0.jamal.engine.Processor();
        final String fileName = jamFile.getAbsolutePath();
        final String pomXml;
        try {
            pomXml = processor.process(FileTools.getInput(fileName));
        } catch (BadSyntax e) {
            throw new RuntimeException("Jamal error processing the file 'pom.xml.jam'\n" + dumpException(e), e);
        }
        String formattedPomXml;
        try {
            formattedPomXml = formatOutput(pomXml);
        } catch (Exception e) {
            throw new RuntimeException("Cannot format the file 'pom.xml'\n" + dumpException(e), e);
        }

        final File output = new File(projectDirectory, "pom.xml");
        try (final OutputStream os = new FileOutputStream(output)) {
            os.write(formattedPomXml.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Cannot write the 'pom.xml' file.", e);
        }
        return new File(projectDirectory, "pom.xml");
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
        return read( new FileInputStream(input),options);
    }
}