package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Xml.ATTR;
import javax0.jamal.api.Xml.CDATA;
import javax0.jamal.api.Xml.CDATATEXT;
import javax0.jamal.api.Xml.TAG;
import javax0.jamal.api.Xml.TEXT;
import javax0.jamal.engine.StackLimiter;
import javax0.jamal.tools.Params;

import java.util.List;
import java.util.Map;

public class Xml implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        final var topTag = Params.<String>holder("yamlXmlTopTag", "tag").orElse("xml");
        final var attributes = Params.<String>holder("yamlXmlAttributes", "attributes").orElseNull();
        Params.using(processor).keys(clone, copy, topTag, attributes).between("()").parse(in);

        final var yamlObject = Resolve.getYaml(processor, in.toString().trim());
        Resolver.resolve(yamlObject, processor, clone.is(), copy.is());

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
                if (closed) {
                    throw new BadSyntax("!!javax0.jamal.api.Xml$Attr cannot follow content node");
                }
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
                    sb.append(escape(value.toString()));
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
                    sb.append(escape(value.toString()));
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
                if (closed) {
                    throw new BadSyntax("!!javax0.jamal.api.Xml$ATTR cannot follow content node.");
                }
                attributesTo(sb, (Map<String, String>) e);
            } else if (e instanceof TAG) {
                if (closed) {
                    throw new BadSyntax("!!javax0.jamal.api.Xml$TAG cannot follow content node.");
                }
                if( tagged ){
                    throw new BadSyntax("!!javax0.jamal.api.Xml$TAG must not be repeated.");
                }
                tagged = true;
                tagSingular = ((TAG) e).id;
            } else {
                if (!closed) {
                    sb.append(">");
                    closed = true;
                }
                if (tagSingular.length() == 0) {
                    throw new BadSyntax("Cannot create an XML list for the field '" + tagPlural + "' it is too short and no !!javax0.jamal.api.Xml$TAG was present.");
                }
                sb.append("<").append(tagSingular);
                if (e instanceof Map<?, ?>) {
                    mapToXml(sb, (Map) e);
                } else if (e instanceof List<?>) {
                    if (tagSingular.length() < 2) {
                        throw new BadSyntax("Cannot create an XML list for the field '" + tagSingular + "'");
                    }

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
