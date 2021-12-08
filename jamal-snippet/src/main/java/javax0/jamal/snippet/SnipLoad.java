package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.HexDumper;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.SHA256;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Params.holder;

public class SnipLoad implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var ref = in.getReference();
        final var idRegex = holder("name", "id").orElse("").asString();
        final var fnRegex = holder("file", "fileName").orElse("").asString();
        final var textRegex = holder("text", "contains").orElse("").asString();
        final var input = holder("input").orElse("").asString();
        final var format = holder("format").orElse("XML").asString();
        Params.using(processor).from(this).keys(idRegex, fnRegex, textRegex, input, format).tillEnd().parse(in);
        if (!"XML".equals(format.get())) {
            throw new BadSyntax("The only supported format is XML");
        }

        try {
            final var store = SnippetStore.getInstance(processor);
            final var dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            final var is = new ByteArrayInputStream(FileTools.getFileContent(FileTools.absolute(ref, input.get())).getBytes(StandardCharsets.UTF_8));
            final var dBuilder = dbFactory.newDocumentBuilder();
            final var doc = dBuilder.parse(is);
            final var root = doc.getDocumentElement();
            if (!"snippets".equals(root.getLocalName()) || !SnipSave.NS.equals(root.getNamespaceURI())) {
                throw new BadSyntax("The root element of the XML document must be <snippets xmlns=\"" + SnipSave.NS + "\">");
            }
            final var children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                final var child = children.item(i);
                if (child.getNodeType() == Document.TEXT_NODE && child.getTextContent().trim().isEmpty() || Document.COMMENT_NODE == child.getNodeType()) {
                    continue;
                }
                if (Document.ELEMENT_NODE != child.getNodeType() || !"snippet".equals(child.getLocalName()) || !SnipSave.NS.equals(child.getNamespaceURI())) {
                    throw new BadSyntax("XML document must contain only 'snippet' tags under the 'snippets' root element");
                }
                final var id = child.getAttributes().getNamedItem("id");
                if (id == null) {
                    throw new BadSyntax("The 'snippet' tag must have an 'id' attribute");
                }
                final var idValue = id.getNodeValue();
                if (!convertRegex(idRegex.get()).test(idValue)) {
                    continue;
                }
                final var fn = child.getAttributes().getNamedItem("file");
                if (fn == null) {
                    throw new BadSyntax("The 'snippet id=" + idValue + "' tag must have a 'file' attribute");
                }
                final var fnValue = fn.getNodeValue();
                if (!convertRegex(fnRegex.get()).test(fnValue)) {
                    continue;
                }
                final var line = child.getAttributes().getNamedItem("line");
                if (line == null) {
                    throw new BadSyntax("The 'snippet id=" + idValue + "' tag must have a 'line' attribute");
                }
                final int lineValue;
                try {
                    lineValue = Integer.parseInt(line.getNodeValue());
                } catch (NumberFormatException e) {
                    throw new BadSyntax("The 'line' attribute of the 'snippet id=" + idValue + "' tag must be an integer");
                }
                final var column = child.getAttributes().getNamedItem("column");
                if (column == null) {
                    throw new BadSyntax("The 'snippet id=" + idValue + "' tag must have a 'column' attribute");
                }
                final int columnValue;
                try {
                    columnValue = Integer.parseInt(column.getNodeValue());
                } catch (NumberFormatException e) {
                    throw new BadSyntax("The 'column' attribute of the 'snippet id=" + idValue + "' tag must be an integer");
                }

                final var texts = child.getChildNodes();

                final var sb = new StringBuilder();
                int countTexts = 0;
                for (int j = 0; j < texts.getLength(); j++) {
                    final var text = texts.item(j);
                    if (Document.TEXT_NODE == text.getNodeType() && text.getTextContent().trim().isEmpty() || Document.COMMENT_NODE == text.getNodeType()) {
                        continue;
                    }
                    if (Document.CDATA_SECTION_NODE == text.getNodeType()) {
                        sb.append(text.getNodeValue());
                        countTexts++;
                    }
                }
                if (countTexts == 0) {
                    throw new BadSyntax("The 'snippet id=" + idValue + "' tag must have at least one CDATA section");
                }
                final var text = sb.toString();
                if (!convertRegex(textRegex.get()).test(text)) {
                    continue;
                }
                final var hash = child.getAttributes().getNamedItem("hash");
                if( hash != null){
                    final var hashValue = hash.getNodeValue();
                    final var calculatedHashValue = SnipCheck.doted(HexDumper.encode(SHA256.digest(text)));
                    if( !Objects.equals(hashValue, calculatedHashValue)){
                        throw new BadSyntax("The 'hash' attribute of the 'snippet id=" + idValue + "' tag must be equal to the hash of the text");
                    }
                }
                store.snippet(idValue, text, new Position(fnValue, lineValue, columnValue));
            }
            return "";
        } catch (Exception e) {
            throw new BadSyntax("Could not read or parse the XML file", e);
        }
    }

    private static Predicate<String> convertRegex(String regex) {
        return regex == null || regex.length() == 0 ? x -> true : Pattern.compile(regex).asPredicate();
    }

    @Override
    public String getId() {
        return "snip:load";
    }
}
