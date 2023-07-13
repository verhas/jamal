package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.SHA256;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.IntegerParameter;
import javax0.jamal.tools.param.StringParameter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;

public class SnipSave implements Macro, InnerScopeDependent, Scanner.WholeInput {
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
        final var scanner = newScanner(in, processor);
        final var idRegex = scanner.str("name", "id").defaultValue("");
        final var fnRegex = scanner.str("file", "fileName").defaultValue("");
        final var textRegex = scanner.str("text", "contains").defaultValue("");
        final var output = scanner.str("output").defaultValue("");
        final var format = scanner.str("format").defaultValue("XML");
        final var tab = scanner.number("tab", "tabSize").defaultValue(4);
        scanner.done();
        BadSyntax.when(!"XML".equals(format.get()), "The only supported format is XML");
        saveXML(processor, ref, idRegex, fnRegex, textRegex, output, tab);
        return "";

    }

    private void saveXML(final Processor processor,
                         final String ref,
                         final StringParameter idRegex,
                         final StringParameter fnRegex,
                         final StringParameter textRegex,
                         final StringParameter output,
                         final IntegerParameter tab) throws BadSyntax {
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
