package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * Convert a thin XML to regular XML.
 */
public class ThinXml {

    public static final int NOT_CALCULATED_YET = -1;
    private final String thinXml;
    /**
     * The tab size in spaces for the generated XML. This is not fascinating since the resulting XML is formatted.
     */
    private final int tabSize = 4;
    boolean tabClosed = true;

    public ThinXml(final String thinXml) {
        this.thinXml = thinXml;
    }

    /**
     * Store the data for an opening tag. We store the tag id, so that when it closed we know what to output following
     * the {@code </} character. We also store the position of the opening tag in the thin XML source. It is needed, so
     * we know when a text or tag starts on a column which is more left than the column of the opening tag. We also store
     * the position of the closing tag in the output so we know how to tab the elements below it and also the closing tag.
     * <p>
     * When the tag is {@code null} then the tag comes from an XML tag start, which should be closed explicitly by the
     * XML tag in the source, which is copied verbatim.
     */
    private static class Tag {
        final int srcTab;
        final String tag;
        final int outTab;

        private Tag(int srcTab, String tag, int outTab) {
            this.srcTab = srcTab;
            this.tag = tag;
            this.outTab = outTab;
        }
    }

    /**
     * The stack of tags which are open. (This comment 100% was written by GitHub copilot before I even could ask for it.)
     */
    final ArrayList<Tag> tags = new ArrayList<>();

    /**
     * Format the think XML to regular XML and return the XML as a string. The XML is not processed as XML, only as text.
     * It is not formatted, not checked for completeness, not checked for correctness, not checked for syntax errors.
     * The leading {@code <?xml} line is not generated in front of the XML as it may be a partial XML.
     *
     * @return the regular XML created from the thin XML
     * @throws BadSyntax if there is any problem parsing the attributes
     */
    public String getXml() throws BadSyntax {
        final var xml = new StringBuilder();
        final var lines = thinXml.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            final var line = lines[i];
            final var trimmedLine = line.trim();
            if (trimmedLine.startsWith("<![CDATA[")) {
                i = handleCDATA(xml, lines, i);
            } else if (trimmedLine.length() == 0) {
                if (i < lines.length - 1) {
                    xml.append("\n");
                }
            } else {
                final boolean containsBirdBeak = line.contains("<");
                final var in = Input.makeInput(line, new Position(null, 0, 0));
                if (!containsBirdBeak) {
                    closeTags(xml, spcCount(line));
                }
                boolean tabText = true;
                int firstTagColumn = NOT_CALCULATED_YET;
                while (in.length() > 0) {
                    if (in.indexOf(">") >= 0 && !containsBirdBeak) {
                        if (firstTagColumn == NOT_CALCULATED_YET) {
                            skipWhiteSpaces(in);
                            firstTagColumn = in.getColumn();
                        }
                        tabText = convertTag(xml, in, tabText, firstTagColumn);
                    } else {
                        convertText(xml, in, tabText);
                    }
                }
            }
        }
        closeTags(xml, 0);
        return xml.toString();
    }

    /**
     * Convert a CDATA section from the thinXML to regular XML.
     * <p>
     * <p>
     * The CDATA start should be on its own line, though it may be followed by characters that belong to the CDATA
     * section. The position of the {@code <![CDATA[} controls which opened tags are closed before the CDATA section.
     *
     * @param xml   the built-up XML
     * @param lines the lines containing the thinXML
     * @param i     the index of the current line that contains the first line of the CDATA section
     * @return the index of the line after the last line of the CDATA section
     */
    private int handleCDATA(StringBuilder xml, String[] lines, int i) {
        final var line = lines[i];
        closeTags(xml, line.indexOf("<"));
        lines[i] = line.substring(line.indexOf("<"));
        while (i < lines.length && !lines[i].trim().endsWith("]]>")) {
            xml.append(lines[i++]).append("\n");
        }
        xml.append(lines[i]).append("\n");
        return i;
    }

    private void convertText(StringBuilder xml, Input in, boolean tabText) {
        if (in.indexOf("</") >= 0) {
            cleanTags(xml);
            xml.append(in).append("\n");
        } else if (in.indexOf("<") >= 0) {
            if (!tabClosed) {
                closeLastTag(xml);
            }
            xml.append(in).append("\n");
            skipWhiteSpaces(in);
            final var column = in.getColumn();
            tags.add(new Tag(column, null, column));
        } else {
            if (tabText) {
                skipWhiteSpaces(in);
                if (tags.size() > 0) {
                    xml.append(spaces(tags.get(tags.size() - 1).outTab + tabSize));
                }
            }
            xml.append(in);
            if (tabText) {
                xml.append("\n");
            }
            tabClosed = tabText;
        }
        in.getSB().setLength(0);
    }

    private boolean convertTag(StringBuilder xml, Input in, boolean tabText, int column) throws BadSyntax {
        if (!tabText) {
            xml.append("\n");
        }
        skipWhiteSpaces(in);
        final var tag = fetchId(in);
        final int outTab;
        if (tags.size() == 0) {
            outTab = 0;
        } else {
            outTab = tags.get(tags.size() - 1).outTab + tabSize;
        }
        tags.add(new Tag(column, tag, outTab));
        skipWhiteSpaces(in);
        final var params = Params.using(null).from(() -> this.getClass().getSimpleName()).endWith('>').fetchParameters(in);
        xml.append(spaces(outTab));
        xml.append("<").append(tag);
        convertAttributes(xml, params);
        xml.append(">");
        tabText = in.length() == 0;
        if (tabText) {
            xml.append("\n");
        }
        tabClosed = tabText;
        return tabText;
    }

    private void convertAttributes(StringBuilder xml, LinkedHashMap<String, String> params) {
        for (final var param : params.entrySet()) {
            xml.append(" ")
                    .append(param.getKey())
                    .append("=\"")
                    .append(param.getValue()
                            .replaceAll("\"", "&quot;"))
                    .append("\"");
        }
    }

    private void cleanTags(StringBuilder xml) {
        while (tags.size() > 0 && tags.get(tags.size() - 1).tag != null) {
            if (tabClosed) {
                xml.append(spaces(tags.get(tags.size() - 1).outTab));
            }
            xml.append("</")
                    .append(tags.remove(tags.size() - 1).tag)
                    .append(">")
                    .append("\n");
            tabClosed = true;
        }
        if (tags.size() > 0) {
            tags.remove(tags.size() - 1);
        }
    }

    private void closeLastTag(StringBuilder xml) {
        if (tags.size() > 0) {
            xml.append("</")
                    .append(tags.remove(tags.size() - 1).tag)
                    .append(">")
                    .append("\n");
            tabClosed = true;
        }

    }

    private void closeTags(StringBuilder xml, int tab) {
        while (tags.size() > 0 && tags.get(tags.size() - 1).tag != null && tags.get(tags.size() - 1).srcTab >= tab) {
            if (tabClosed) {
                xml.append(spaces(tags.get(tags.size() - 1).outTab));
            }
            xml.append("</")
                    .append(tags.remove(tags.size() - 1).tag)
                    .append(">")
                    .append("\n");
            tabClosed = true;
        }
    }

    String spaces(int i) {
        return " ".repeat(i);
    }

    int spcCount(String line) {
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                return i;
            }
        }
        return -1; // empty line
    }

}
