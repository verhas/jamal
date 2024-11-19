package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.api.Xml.*;
import javax0.jamal.engine.StackLimiter;
import javax0.jamal.tools.Scanner;

import java.util.List;
import java.util.Map;

@Macro.Stateful
public class Xml implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var clone = Resolver.cloneOption(scanner);
        final var copy = Resolver.copyOption(scanner);
        final var topTag = scanner.str("yamlXmlTopTag", "tag").defaultValue("xml");
        final var attributes = scanner.str("yamlXmlAttributes", "attributes").optional();
        scanner.done();

        final var yamlObject = Resolve.getYaml(processor, in.toString().trim());
        Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        //TODO create a new instance to make it thread safe
        return toXml(topTag.get(), attributes.get(), yamlObject.getObject());
    }

    @Override
    public String getId() {
        return "yaml:xml";
    }

    private StackLimiter stackLimiter;

    public String toXml(final String topTag, String attributes, Object content) throws BadSyntax {
        stackLimiter = new StackLimiter();
        final var sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
        sb.append("<").append(topTag);
        if (attributes != null && attributes.length() > 0) {
            sb.append(" ").append(attributes);
        }
        if (content instanceof Map<?, ?>) {
            mapToXml(sb, (Map) content);
        } else if (content instanceof List<?>) {
            listToXml(sb, topTag, topTag.substring(0, topTag.length() - 1), (List) content);
        } else {
            throw new BadSyntax("You can only convert Map or List structures to XML.");
        }
        sb.append("</").append(topTag).append(">");
        return sb.toString();
    }

    private void mapToXml(StringBuilder sb, Map<String, Object> map) throws BadSyntax {
        stackLimiter.up();
        boolean closed = false;
        boolean ended = false;
        for (final var e : map.entrySet()) {
            final var tag = e.getKey();
            final var value = e.getValue();
            if (value instanceof ATTR) {
                BadSyntax.when(closed, "!text cannot follow content node");
                final var attrs = (ATTR) value;
                if (attrs.size() != 0) {
                    attributesTo(sb, attrs);
                } else {
                    sb.append(" ").append(tag).append("=\"").append(escape(value.toString())).append("\"");
                }
            } else {
                if (!closed) {
                    sb.append(">");
                    closed = true;
                }
                if (value instanceof TEXT) {
                    sb.append(escape(""+value));
                    ended = true;
                } else if (value instanceof CDATATEXT) {
                    sb.append("<![CDATA[").append(value).append("]]>");
                    ended = true;
                } else if (value instanceof CDATA) {
                    sb.append("<").append(tag).append(">");
                    sb.append("<![CDATA[").append(value).append("]]>");
                } else if (value instanceof Map<?, ?>) {
                    sb.append("<").append(tag);
                    mapToXml(sb, (Map) value);
                } else if (value instanceof List<?>) {
                    sb.append("<").append(tag);
                    listToXml(sb, tag, tag.substring(0, tag.length() - 1), (List) value);
                } else {
                    sb.append("<").append(tag).append(">");
                    sb.append(escape(""+value));
                }
            }
            if (closed && !ended) {
                sb.append("</").append(tag).append(">");
            }
        }
        stackLimiter.down();
    }

    private void listToXml(StringBuilder sb, String tagPlural, String tagSingular, List list) throws BadSyntax {
        stackLimiter.up();
        boolean tagged = false;
        boolean closed = false;
        for (final var e : list) {
            if (e instanceof ATTR) {
                BadSyntax.when(closed, "!attr cannot follow content node.");
                attributesTo(sb, (Map<String, String>) e);
            } else if (e instanceof TAG) {
                BadSyntax.when(closed, "!tag cannot follow content node.");
                BadSyntax.when(tagged, "!tag must not be repeated.");
                tagged = true;
                tagSingular = ((TAG) e).id;
            } else {
                if (!closed) {
                    sb.append(">");
                    closed = true;
                }
                BadSyntax.when(tagSingular.length() == 0,  "Cannot create aní XML list for the field '%s' it is too short and no !tag was present.", tagPlural);
                sb.append("<").append(tagSingular);
                if (e instanceof Map<?, ?>) {
                    mapToXml(sb, (Map) e);
                } else if (e instanceof List<?>) {
                    final String ts = tagSingular;
                    BadSyntax.when(tagSingular.length() < 2,  "Cannot create an XML list for the field '%s'", ts);

                    listToXml(sb, tagSingular, tagSingular.substring(0, tagSingular.length() - 1), (List) e);
                } else if (e instanceof CDATA) {
                    sb.append(">");
                    sb.append("<![CDATA[").append(e).append("]]>");
                } else {
                    sb.append(">");
                    sb.append(escape(e.toString()));
                }
                sb.append("</").append(tagSingular).append(">");
            }
        }
        stackLimiter.down();
    }

    private void attributesTo(StringBuilder sb, Map<String, String> attrs) {
        for (final var attr : attrs.entrySet()) {
            sb.append(" ").append(attr.getKey()).append("=\"").append(escape(attr.getValue())).append("\"");
        }
    }

    private static final Map<Character, String> ESCHMAP = Map.of('"', "&quot;", '\'', "&apos;", '<', "&lt;", '>', "&gt;", '&', "&amp;");

    private String escape(String in) {
        final var sb = new StringBuilder();
        for (final var ch : in.toCharArray()) {
            if (ESCHMAP.containsKey(ch)) {
                sb.append(ESCHMAP.get(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
