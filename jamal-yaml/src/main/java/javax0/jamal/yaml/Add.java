package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import ognl.Ognl;
import ognl.OgnlException;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

import static javax0.jamal.tools.Params.holder;

public class Add implements Macro, InnerScopeDependent {
    final Yaml yaml = new Yaml();

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var to = holder("yamlDataTarget", "to").asString();
        final var key = holder(null, "key").orElseNull();
        final var flatten = holder(null, "flat", "flatten").asBoolean();
        Params.using(processor).keys(to, key, flatten).parse(in);
        final var dotIndex = to.get().indexOf('.');
        final String id = getId(to, dotIndex);
        final Object expression = getOgnlExpression(in, to, dotIndex);
        final Object yamlStructure = getNewYamlPart(in);
        final var yamlObject = Resolve.getYaml(processor, id);
        final Object anchor = getAnchor(expression, to, yamlObject);
        assertConsistency(to, key, flatten, anchor);
        if (anchor instanceof Map) {
            if (flatten.is()) {
                if (yamlStructure instanceof Map<?, ?>) {
                    ((Map<Object, Object>) anchor).putAll((Map<?, ?>) yamlStructure);
                } else {
                    throw new BadSyntax("You can add only a Map to a Map when flat(ten) for '" + to.get() + "'");
                }
            } else {
                ((Map<Object, Object>) anchor).put(key.get(), yamlStructure);
            }
        } else {
            if (flatten.is()) {
                if (yamlStructure instanceof List<?>) {
                    ((List<Object>) anchor).addAll((List<?>) yamlStructure);
                } else {
                    throw new BadSyntax("You can add only a List to a List when flat(tten) for '" + to.get() + "'");
                }
            } else {
                ((List<Object>) anchor).add(yamlStructure);
            }
        }
        yamlObject.resolved = false;
        return "";
    }

    private void assertConsistency(Params.Param<String> to, Params.Param<Object> key, Params.Param<Boolean> flatten, Object anchor) throws BadSyntax {
        if (key.get() == null && anchor instanceof Map && !flatten.is()) {
            throw new BadSyntax("You cannot '" + getId() + "' without a 'key' parameter to a Map for '" + to.get() + "'");
        }
        if (key.get() != null && anchor instanceof List) {
            throw new BadSyntax("You cannot '" + getId() + "' with a 'key' parameter to a List for '" + to.get() + "'");
        }
        if (key.get() != null && flatten.is()) {
            throw new BadSyntax("You cannot '" + getId() + "' with a 'key' parameter when flattening for '" + to.get() + "'");
        }
        if ((!(anchor instanceof Map)) && !(anchor instanceof List)) {
            throw new BadSyntax("You can '" + getId() + "' only to a List or Map for '" + to.get() + "'\n" +
                "The actual class is " + anchor.getClass());
        }
    }

    private Object getAnchor(Object expression, Params.Param<String> to, YamlObject yamlObject) throws BadSyntax {
        Object anchor;
        Exception exception = null;
        try {
            if (expression == null) {
                anchor = yamlObject.getObject();
            } else {
                anchor = Ognl.getValue(expression, yamlObject.getObject());
            }
        } catch (OgnlException e) {
            anchor = null;
            exception = e;
        }
        if (anchor == null) {
            throw new BadSyntax("Cannot '" + getId() + "' into the OGN expression '" + to.get() + "'", exception);
        }
        return anchor;
    }

    private Object getNewYamlPart(Input in) throws BadSyntax {
        final Object yamlStructure;
        try {
            yamlStructure = yaml.load(in.toString());
        } catch (Exception e) {
            throw new BadSyntax("Cannot load YAML data.", e);
        }
        return yamlStructure;
    }

    private Object getOgnlExpression(Input in, Params.Param<String> to, int dotIndex) throws BadSyntax {
        final Object expression;
        if (dotIndex == -1) {
            expression = null;
        } else {
            try {
                expression = Ognl.parseExpression(to.get().substring(dotIndex + 1));
            } catch (OgnlException e) {
                throw new BadSyntax("Syntax error in the OGNL expression '" + to.get() + "'", e);
            }
        }
        return expression;
    }

    private String getId(Params.Param<String> to, int dotIndex) throws BadSyntax {
        final String id;
        if (dotIndex == -1) {
            id = to.get();
        } else {
            id = to.get().substring(0, dotIndex);
        }
        return id;
    }

    @Override
    public String getId() {
        return "yaml:add";
    }
}
