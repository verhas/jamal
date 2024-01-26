package javax0.jamal.java;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple data structure to store XML data and manipulate it.
 * The data contains simplified XML good enough to store POM files.
 * It is not possible to add attributes to tags and content can only be text.
 * <p>
 * The underlying implementation is a Map with string keys representing the tag names.
 * The values are always lists of
 * <ul>
 *     <li>strings, or</li>
 *     <li>Xml values.</li>
 * </ul>
 *
 * <p>
 * List is used because a tag can appear several times at a certain level in the XML.
 * When a tag appears only one time, the list has one element.
 * Empty tags are represented with an empty list.
 * <p>
 * Using this representation, you can reserve the order of the elements, but you cannot reserve the position of a tag
 * relative to other tags. The order of the different tags is not reserved.
 * <p>
 */
public class Xml implements CharSequence {

    final Map<String, List<CharSequence>> xml = new LinkedHashMap<>();

    public Xml() {
    }

    public Xml(final String tag) {
        xml.put(tag, new ArrayList<>());
    }

    /**
     * Return a new Xml instance that represents
     * <pre>{@code
     * <tag>value</tag>
     * }</pre>
     *
     * @param tag the name of the tag
     * @param value the value in the tag
     * @return the new Xml object
     */
    public static Xml tagValue(String tag, CharSequence value) {
        final var xml = new Xml();
        xml.add(path(tag), value);
        return xml;
    }

    /**
     * Return the string array that contains the arguments.
     * This method can be used to represent the path of tags in a call expecting a string array.
     *
     * @param tags the path of the tags
     * @return the string array
     */
    public static String[] path(String... tags) {
        return tags;
    }

    public void add(final Xml sub){
        for( final var entry : sub.xml.entrySet()){
            for( final var value : entry.getValue()){
                add(entry.getKey(),value);
            }
        }
    }

    /**
     * Add an empty top tag to an existing Xml.
     * @param tag the tag to add.
     */
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
     * If the tag is not present, then null is returned.
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

    public <T> T get(Class<T> klass, String tag) {
        return klass.cast(get(tag));
    }

    /**
     * Add a new element or update an existing element in the XML structure represented by the Xml class.
     *
     * @param tags  An array of strings representing a path of tags in the XML structure. Each element in the array is a
     *              tag name, and the sequence of names represents the hierarchical path in the XML tree.
     * @param index The current position in the tags array that the method is processing. It is used in recursive calls
     *              to navigate through the array.
     * @param value The value to be associated with the tag specified by the last element in the tags array.
     *              If this value is null, an empty list will be added to the Xml.
     */
    public void add(String[] tags, int index, CharSequence value) {
        formatted = null;
        final var tag = tags[index];
        final var currentValue = get(tag);
        if (currentValue == null) {
            if (isLastTag(tags, index)) {
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
            if (isLastTag(tags, index)) {
                    xml.get(tag).add(value);
            } else {
                if (currentValue instanceof Xml) {
                    ((Xml) currentValue).add(tags, index + 1, value);
                } else {
                    throw new IllegalArgumentException("Cannot add subtag to a tag that already has a text value");
                }
            }
        }
    }

    private static boolean isLastTag(String[] tags, int index) {
        return index == tags.length - 1;
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
