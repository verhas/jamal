package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.stream.Collectors;

@Macro.Stateful
public class XmlFormat implements Macro, InnerScopeDependent, Closer.OutputAware, AutoCloseable {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var tabsize = Params.holder("tabsize").orElseInt(4);
        Params.using(processor).from(this).between("()").keys(tabsize).parse(in);

        InputHandler.skipWhiteSpaces(in);
        if (in.length() > 0) {
            final String input = in.toString();
            return formatXml(input, "" + tabsize.get());
        } else {
            this.tabsize = "" + tabsize.get();
            //TODO create a new instance and defer to that one to be thread safe
            processor.deferredClose(this);
            return "";
        }
    }

    private static String formatXml(String input, String tabsize) throws BadSyntax {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(input)));
            Transformer tf = TransformerFactory.newInstance().newTransformer();
            tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", tabsize);
            Writer out = new StringWriter();
            tf.transform(new DOMSource(doc), new StreamResult(out));
            return Arrays.stream(out.toString().split(System.lineSeparator())).filter(s -> s.trim().length() > 0).collect(Collectors.joining(System.lineSeparator()));
        } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
            throw new BadSyntax("There was an XML exception", e);
        }
    }

    @Override
    public String getId() {
        return "xmlFormat";
    }

    private Input output = null;
    private String tabsize = "4";

    @Override
    public void close() throws BadSyntax {
        if (output != null) {
            InputHandler.skipWhiteSpaces(output);
            final var result = formatXml(output.toString(), tabsize);
            output.getSB().delete(0, output.getSB().length());
            output.getSB().append(result);
        }
    }

    @Override
    public void set(Input output) {
        this.output = output;
    }
}
