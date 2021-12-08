package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

import static javax0.jamal.tools.Params.holder;

public class SnipSave implements Macro, InnerScopeDependent {
    public static final String NS = "https://snippets.jamal.javax0.com/v1/snippets";
    public static final String SNIPPETS = "snippets";
    public static final String SNIPPET = "snippet";
    public static final String TIME_STAMP = "ts";
    public static final String DATE_TIME = "dateTime";
    public static final String ID = "id";
    public static final String FILE = "file";
    public static final String LINE = "line";
    public static final String COLUMN = "column";
    public static final String HASH = "hash";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var ref = in.getReference();
        final var idRegex = holder("name", "id").orElse("").asString();
        final var fnRegex = holder("file", "fileName").orElse("").asString();
        final var textRegex = holder("text", "contains").orElse("").asString();
        final var output = holder("output").orElse("").asString();
        final var format = holder("format").orElse("XML").asString();
        final var tab = holder("tab", "tabSize").orElseInt(4);
        Params.using(processor).from(this).keys(idRegex, fnRegex, textRegex, output, format, tab).tillEnd().parse(in);
        if (!"XML".equals(format.get())) {
            throw new BadSyntax("The only supported format is XML");
        }
        try {
            final var documentFactory = DocumentBuilderFactory.newInstance();
            final var documentBuilder = documentFactory.newDocumentBuilder();
            final var document = documentBuilder.newDocument();
            final var root = document.createElementNS(NS, SNIPPETS);
            document.appendChild(root);
            final var timeStamp = document.createAttribute(TIME_STAMP);
            final var currTime = System.currentTimeMillis();
            timeStamp.setValue(currTime + "");
            root.setAttributeNode(timeStamp);
            final var date = document.createAttribute(DATE_TIME);
            final var formatter = new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
            date.setValue(formatter.format(currTime));
            root.setAttributeNode(date);

            final var store = SnippetStore.getInstance(processor);
            store.snippetList(idRegex.get(), fnRegex.get(), textRegex.get())
                .filter(snip -> snip.exception == null)
                .forEach(snip -> {
                    final var snippet = document.createElement(SNIPPET);
                    snippet.setAttribute(ID, snip.id);
                    snippet.setAttribute(FILE, snip.pos.file);
                    snippet.setAttribute(LINE, snip.pos.line + "");
                    snippet.setAttribute(COLUMN, snip.pos.column + "");
                    snippet.setAttribute(HASH, SnipCheck.doted(HexDumper.encode(SHA256.digest(snip.text))));
                    final var text = document.createCDATASection(snip.text);
                    snippet.appendChild(text);
                    root.appendChild(snippet);
                });
            final var dump = XmlDocument.formatDocument(document, "" + tab.get());
            try (final var o = new FileOutputStream(FileTools.absolute(ref, output.get()))) {
                o.write(dump.getBytes(StandardCharsets.UTF_8));
            }
            return "";
        } catch (
            ParserConfigurationException | TransformerException | IOException e) {
            throw new BadSyntax("Failed to create XML document", e);
        }

    }

    @Override
    public String getId() {
        return "snip:save";
    }
}
