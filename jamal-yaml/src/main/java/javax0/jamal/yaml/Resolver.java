package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Ref;
import javax0.jamal.tools.Params;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A resolver object can resolve the Jamal Yaml macro references in a Yaml structure.
 */
class Resolver {
    final Yaml yaml = new Yaml();

    final Map<Object, Object> resolved = new IdentityHashMap<>();
    final Map<String, Object> resolvedRefs = new HashMap<>();
    final Processor processor;
    int phase = 1;
    boolean clone;

    /**
     * Creates a new resolver.
     *
     * @param processor the processor instance to be used to get access to the Yaml user defined macros in the
     *                  references.
     * @param clone     {@code true} if the resolving has to clone the data.
     */
    Resolver(Processor processor, boolean clone) {
        this.processor = processor;
        this.clone = clone;
    }

    /**
     * Resolve the {@code yamObject} if it was not resolved yet.
     *
     * @param yamlObject the object to resolve
     * @param processor  the processor instance to be used to get access to the Yaml user defined macros in the *
     *                   references.
     * @param clone      {@code true} if the resolving has to clone the data.
     * @throws BadSyntax
     */
    static void resolve(YamlObject yamlObject, Processor processor, boolean clone) throws BadSyntax {
        if (!yamlObject.resolved) {
            yamlObject.setContent(new Resolver(processor, clone).resolve(new javax0.jamal.api.Ref(yamlObject.getId())));
            yamlObject.resolved = true;
        }
    }

    static Params.Param<Boolean> cloneOption() {
        return Params.holder("yamlResolveClone", "clone").asBoolean();
    }

    public Object resolve(Object content) throws BadSyntax {
        final var resObject = _resolve(content);
        if (clone) {
            clone = false;
            phase = 2;
            return _resolve(resObject);
        } else {
            return resObject;
        }
    }

    private Object _resolve(Object content) throws BadSyntax {
        if (resolved.containsKey(content)) {
            return content;
        }
        resolved.put(content, null);
        if (content instanceof List) {
            return resolveList((List<Object>) content);
        }
        if (content instanceof Map) {
            return resolveMap((Map<Object, Object>) content);
        }
        if (content instanceof javax0.jamal.api.Ref) {
            return resolveRef((javax0.jamal.api.Ref) content);
        }
        return content;
    }

    private Object resolveRef(Ref ref) throws BadSyntax {
        final var id = ref.id;
        if (resolvedRefs.containsKey(id)) {
            return resolvedRefs.get(id);
        }
        final var yamlObject = Resolve.getYaml(processor, id);
        if (clone) {
            final Object newContent = clone(id, yamlObject.getObject());
            return newContent;
        } else {
            if (phase == 1) {
                resolvedRefs.put(id, yamlObject.getObject());
                return _resolve(yamlObject.getObject());
            } else {
                final Object newContent = clone(id, yamlObject.getObject());
                return _resolve(newContent);
            }
        }
    }

    private Object clone(String id, Object content) {
        final var out = new StringWriter();
        yaml.dump(content, out);
        final var newContent = yaml.load(out.toString());
        resolvedRefs.put(id, newContent);
        return newContent;
    }

    private Map<?, ?> resolveMap(Map<Object, Object> content) throws BadSyntax {
        if (clone) {
            return resolveMapCloning(content);
        } else {
            return resolveMapNonCloning(content);
        }
    }

    private Map<Object, Object> resolveMapCloning(Map<Object, Object> content) throws BadSyntax {
        final var newMap = new LinkedHashMap<>();
        for (final Map.Entry<Object, Object> e : content.entrySet()) {
            final var key = _resolve(e.getKey());
            final var value = _resolve(e.getValue());
            newMap.put(key, value);
        }
        return newMap;
    }

    private Map<Object, Object> resolveMapNonCloning(Map<Object, Object> content) throws BadSyntax {
        final var tempMap = new LinkedHashMap<>();
        for (final Map.Entry<Object, Object> e : content.entrySet()) {
            final var key = _resolve(e.getKey());
            final var value = _resolve(e.getValue());
            if (key != e.getKey() || value != e.getValue()) {
                tempMap.put(key, value);
            } else {
                tempMap.put(e.getKey(), e.getValue());
            }
        }
        content.clear();
        for (final Map.Entry<Object, Object> tempe : tempMap.entrySet()) {
            content.put(tempe.getKey(), tempe.getValue());
        }
        return content;
    }


    private List<?> resolveList(List<Object> content) throws BadSyntax {
        if (clone || !(content instanceof ArrayList)) {
            final var newList = new LinkedList<>();
            for (final var e : content) {
                final var newE = _resolve(e);
                newList.add(newE);
            }
            return newList;
        } else {
            final var alContent = (ArrayList<Object>) content;
            for (int i = 0; i < alContent.size(); i++) {
                final var e = alContent.get(i);
                final var newE = _resolve(e);
                if (newE != e)
                    alContent.set(i, newE);
            }
            return content;
        }
    }

}
