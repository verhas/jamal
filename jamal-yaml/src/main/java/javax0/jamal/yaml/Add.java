package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import javax0.jamal.tools.param.StringParameter;
import ognl.Ognl;
import ognl.OgnlException;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;

@Macro.Name("yaml:add")
public
class Add implements Macro, InnerScopeDependent, Scanner.FirstLine {
    final Yaml yaml = YamlFactory.newYaml();

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var to = scanner.str("yamlDataTarget", "to");
        final var key = scanner.str(null, "key").optional();
        final var flatten = scanner.bool(null, "flat", "flatten");
        scanner.done();
        final var dotIndex = to.get().indexOf('.');
        final String id = getId(to, dotIndex);
        final Object expression = getOgnlExpression(to, dotIndex);
        final Object yamlStructure = getNewYamlPart(in);
        final var yamlObject = Resolve.getYaml(processor, id);
        final Object anchor = getAnchor(expression, to, yamlObject);
        assertConsistency(to, key, flatten, anchor);
        if (anchor instanceof Map) {
            if (flatten.is()) {
                BadSyntax.when(!(yamlStructure instanceof Map<?, ?>), "You can add only a Map to a Map when flat(ten) for '%s'", to.get());
                ((Map<Object, Object>) anchor).putAll((Map<?, ?>) yamlStructure);
            } else {
                ((Map<Object, Object>) anchor).put(key.get(), yamlStructure);
            }
        } else {
            if (flatten.is()) {
                BadSyntax.when(!(yamlStructure instanceof List<?>) || !(anchor instanceof List<?>), "You can add only a List to a List when flat(tten) for '%s'", to.get());
                ((List<Object>) anchor).addAll((List<?>) yamlStructure);
            } else {
                ((List<Object>) anchor).add(yamlStructure);
            }
        }
        yamlObject.resolved = false;
        return "";
    }

    private void assertConsistency(StringParameter to, StringParameter key, BooleanParameter flatten, Object anchor) throws BadSyntax {
        BadSyntax.when(key.get() == null && anchor instanceof Map && !flatten.is(), "You cannot '%s' without a 'key' parameter to a Map for '%s'", getId(), to.get());
        BadSyntax.when(key.get() != null && anchor instanceof List, "You cannot '%s' with a 'key' parameter to a List for '%s'", getId(), to.get());
        BadSyntax.when(key.get() != null && flatten.is(), "You cannot '%s' with a 'key' parameter when flattening for '%s'", getId(), to.get());
        BadSyntax.when((!(anchor instanceof Map)) && !(anchor instanceof List), "You can '%s' only to a List or Map for '%s'\nThe actual class is %s", getId(), to.get(), anchor.getClass());
    }

    private Object getAnchor(Object expression, StringParameter to, YamlObject yamlObject) throws BadSyntax {
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
            throw new BadSyntax("Cannot '" + getId() + "' into the OGNL expression '" + to.get() + "'", exception);
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

    private Object getOgnlExpression(StringParameter to, int dotIndex) throws BadSyntax {
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

    private String getId(StringParameter to, int dotIndex) throws BadSyntax {
        final String id;
        if (dotIndex == -1) {
            id = to.get();
        } else {
            id = to.get().substring(0, dotIndex);
        }
        return id;
    }

}
