package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Position;
import javax0.jamal.tools.Input;
import javax0.jamal.tools.Params;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class ThinXml {

    private final String thinXml;
    private int tabSize = 4;
    boolean tabClose = true;

    public ThinXml(final String thinXml) {
        this.thinXml = thinXml;
    }

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

    final ArrayList<Tag> tags = new ArrayList<>();

    public String getXml() throws BadSyntax {
        final var xml = new StringBuilder();
        final var lines = thinXml.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            final var line = lines[i];
            if (line.trim().equals("<![CDATA[")) {
                while (i < lines.length && !lines[i].trim().equals("]]>")) {
                    xml.append(lines[i++]).append("\n");
                }
                xml.append(lines[i]).append("\n");
            } else if (line.length() == 0) {
                if (i < lines.length - 1) {
                    xml.append("\n");
                }
            } else {
                final boolean containsBirdBeak = line.contains("<");
                final var in = Input.makeInput(line, new Position(null, 0, 0));
                if( !containsBirdBeak ) {
                    closeTags(xml, spcCount(line));
                }
                boolean tabText = true;
                while (in.length() > 0) {
                    if (in.indexOf(">") >= 0 && !containsBirdBeak) {
                        tabText = convertTag(xml, in, tabText);
                    } else {
                        convertText(xml, in, tabText);
                    }
                }
            }
        }
        closeTags(xml, 0);
        return xml.toString();
    }

    private void convertText(StringBuilder xml, Input in, boolean tabText) {
        if (in.indexOf("</") >= 0) {
            cleanTags(xml);
            xml.append(in).append("\n");
        } else if (in.indexOf("<") >= 0) {
            if( !tabClose ) {
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
            tabClose = tabText;
        }
        in.getSB().setLength(0);
    }

    private boolean convertTag(StringBuilder xml, Input in, boolean tabText) throws BadSyntax {
        if (!tabText) {
            xml.append("\n");
        }
        skipWhiteSpaces(in);
        final var column = in.getColumn();
        final var tag = fetchId(in);
        final int outTab;
        if (tags.size() == 0) {
            outTab = 0;
        } else {
            outTab = tags.get(tags.size() - 1).outTab + tabSize;
        }
        tags.add(new Tag(column, tag, outTab));
        skipWhiteSpaces(in);
        final var params = Params.using(null).endWith('>').fetchParameters(in);
        xml.append(spaces(outTab));
        xml.append("<").append(tag);
        convertAttributes(xml, params);
        xml.append(">");
        tabText = in.length() == 0;
        if (tabText) {
            xml.append("\n");
        }
        tabClose = tabText;
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
            if (tabClose) {
                xml.append(spaces(tags.get(tags.size() - 1).outTab));
            }
            xml.append("</")
                .append(tags.remove(tags.size() - 1).tag)
                .append(">")
                .append("\n");
            tabClose = true;
        }
        if( tags.size() > 0 ) {
            tags.remove(tags.size() - 1);
        }
    }

    private void closeLastTag(StringBuilder xml) {
        if (tags.size() > 0 ) {
            xml.append("</")
                .append(tags.remove(tags.size() - 1).tag)
                .append(">")
                .append("\n");
            tabClose = true;
        }

    }
    private void closeTags(StringBuilder xml, int tab) {
        while (tags.size() > 0 && tags.get(tags.size() - 1).tag != null && tags.get(tags.size() - 1).srcTab >= tab) {
            if (tabClose) {
                xml.append(spaces(tags.get(tags.size() - 1).outTab));
            }
            xml.append("</")
                .append(tags.remove(tags.size() - 1).tag)
                .append(">")
                .append("\n");
            tabClose = true;
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
