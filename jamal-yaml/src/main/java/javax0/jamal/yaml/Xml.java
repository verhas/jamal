package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
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
        sb.append(">");
        if (content instanceof Map<?, ?>) {
            mapToXml(sb, (Map) content);
        }
        sb.append("</").append(topTag).append(">");
        return sb.toString();
    }

    private void mapToXml(StringBuilder sb, Map<String, Object> map) throws BadSyntax {
        stackLimiter.up();
        for (final var e : map.entrySet()) {
            sb.append("<").append(e.getKey()).append(">");
            if (e.getValue() instanceof Map<?, ?>) {
                mapToXml(sb, (Map) e.getValue());
            } else if (e.getValue() instanceof List<?>) {
                if (e.getKey().length() < 2) {
                    throw new BadSyntax("Cannot create an XML list for the field '" + e.getKey() + "'");
                }
                listToXml(sb, e.getKey().substring(0, e.getKey().length() - 1), (List) e.getValue());
            } else {
                sb.append(e.getKey());
            }
            sb.append("</").append(e.getKey()).append(">");
        }
        stackLimiter.down();
    }

    private void listToXml(StringBuilder sb, String tagSingular, List list) throws BadSyntax {
        stackLimiter.up();
        for (final var e : list) {
            sb.append("<").append(tagSingular).append(">");
            if (e instanceof Map<?, ?>) {
                mapToXml(sb, (Map) e);
            } else if (e instanceof List<?>) {
                if (tagSingular.length() < 2) {
                    throw new BadSyntax("Cannot create an XML list for the field '" + tagSingular + "'");
                }

                listToXml(sb, tagSingular.substring(0, tagSingular.length() - 1), (List) e);
            } else {
                sb.append(e);
            }
            sb.append("</").append(tagSingular).append(">");
        }
        stackLimiter.down();
    }
}
