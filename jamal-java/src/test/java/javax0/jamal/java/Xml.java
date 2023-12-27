package javax0.jamal.java;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple data structure to store XML data and manipulate it.
 * The data contains simplified XML good enough to store POM files.
 * It is not possible to add attributes to tags and content can only be text (bo CData).
 * <p>
 * The underlying implementation is a Map with string keys representing the tag names.
 * The values can be either a string, a list or an Xml (instance of this class).
 * <p>
 * List values are used when a tag is repeated in the XML.
 * The elements of the lists are either strings or Xml instances.
 * <p>
 */
public class Xml implements CharSequence {

    final Map<String, List<CharSequence>> xml = new LinkedHashMap<>();

    public Xml() {
    }

    public Xml(final String tag) {
        xml.put(tag, new ArrayList<>());
    }

    public static Xml tagValue(String tag, CharSequence value) {
        final var xml = new Xml();
        xml.add(path(tag), value);
        return xml;
    }

    public static String[] path(String... tags) {
        return tags;
    }

    public void add(String tag) {
        add(tag, null);
    }

    public void add(String[] tags) {
        add(tags, null);
    }

    public void add(String tag, CharSequence value) {
        add(new String[]{tag}, value);
    }

    public void add(String[] tags, CharSequence value) {
        add(tags, 0, value);
    }

    /**
     * Get the last value of the tag.
     * <p>
     * If the tag is not present then null is returned.
     *
     * @param tag the tag to get the value of
     * @return the value of the tag or null
     */
    public CharSequence get(String tag) {
        final var vlist = xml.get(tag);
        if (vlist == null || vlist.isEmpty()) {
            return null;
        }
        return vlist.get(vlist.size() - 1);
    }

    public void add(String[] tags, int index, CharSequence value) {
        formatted = null;
        final var tag = tags[index];
        final var currentValue = get(tag);
        if (currentValue == null) {
            if (index == tags.length - 1) {
                if (value == null) {
                    xml.put(tag, new ArrayList<>());
                } else {
                    xml.put(tag, new ArrayList<>(List.of(value)));
                }
            } else {
                final var sub = new Xml();
                xml.put(tag, new ArrayList<>(List.of(sub)));
                sub.add(tags, index + 1, value);
            }
        } else {
            if (index == tags.length - 1) {
                xml.get(tag).add(value);
            } else {
                if (currentValue instanceof Xml) {
                    ((Xml) currentValue).add(tags, index + 1, value);
                } else {
                    throw new IllegalArgumentException("Cannot add subtag to a tag that already has a value");
                }
            }
        }
    }

    /**
     * The formatted value of the XML. Every method that modifies the XML should set this value to null.
     */
    private String formatted = null;

    @Override
    public int length() {
        return toString().length();
    }

    @Override
    public char charAt(int index) {
        return toString().charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return toString().subSequence(start, end);
    }

    @Override
    public String toString() {
        if (formatted == null) {
            final var sb = new StringBuilder();
            for (final var entry : xml.entrySet()) {
                final var key = entry.getKey();
                final var values = entry.getValue();
                if (values != null && !values.isEmpty()) {
                    sb.append("<").append(key).append(">");
                    for (final var value : values) {
                        if (value instanceof String) {
                            sb.append(value);
                        } else if (value instanceof Xml) {
                            sb.append(value);
                        }
                    }
                    sb.append("</").append(key).append(">");
                } else {
                    sb.append("<").append(key).append("/>");
                }
            }
            formatted = sb.toString();
        }
        return formatted;
    }



}
