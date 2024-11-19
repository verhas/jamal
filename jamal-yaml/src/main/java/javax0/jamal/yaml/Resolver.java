package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.api.Ref;
import javax0.jamal.engine.StackLimiter;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Set;
import java.util.*;

/**
 * A resolver object can resolve the Jamal Yaml macro references in a Yaml structure.
 */
@SuppressWarnings("unchecked")
class Resolver {
    final Yaml yaml = YamlFactory.newYaml();
    /**
     * Those objects that have been resolved, or their resolution is currently going. Since there is no IdentityHashSet,
     * we use a map, the value is always null.
     */
    final Map<Object, Object> resolved = new IdentityHashMap<>();
    /**
     * Those yaml:define defined macros that are already resolved or their resolution is under progress.
     */
    final Map<String, Object> resolvedRefs = new HashMap<>();
    final Set<String> usedRefs = new HashSet<>();
    final Processor processor;
    /**
     * Cloned resolution is done in two passes. The first phase
     */
    final boolean clone;
    final boolean copy;

    /**
     * Creates a new resolver.
     *
     * @param processor the processor instance to be used to get access to the Yaml user defined macros in the
     *                  references.
     * @param clone     {@code true} if the resolving has to clone the data.
     */
    Resolver(Processor processor, boolean clone, boolean copy) {
        this.processor = processor;
        this.clone = clone;
        this.copy = copy;
    }

    /**
     * Resolve the {@code yamObject} if it was not resolved yet.
     *
     * @param yamlObject the object to resolve
     * @param processor  the processor instance to be used to get access to the Yaml user defined macros in the *
     *                   references.
     * @param clone      {@code true} if the resolving has to clone the data.
     * @param copy       {@code true} to create copies for referenced parts
     * @throws BadSyntax if some of the referenced Yaml objects does not exist
     */
    static void resolve(YamlObject yamlObject, Processor processor, boolean clone, boolean copy) throws BadSyntax {
        if (!yamlObject.resolved) {
            yamlObject.setContent(new Resolver(processor, clone, copy).resolve(yamlObject.getId(), yamlObject.getObject()));
            yamlObject.resolved = true;
        }
    }

    static BooleanParameter cloneOption(final Scanner.ScannerObject scanner) {
        return scanner.bool("yamlResolveClone", "clone");
    }

    static BooleanParameter copyOption(final Scanner.ScannerObject scanner) {
        return scanner.bool("yamlResolveCopy", "copy");
    }

    private StackLimiter stackLimiter;

    public Object resolve(String id, Object content) throws BadSyntax {
        resolvedRefs.clear();
        collectRefs(content);
        dereferenceRefs();
        if (resolvedRefs.containsKey(id)) {
            return resolvedRefs.get(id);
        } else {
            stackLimiter = new StackLimiter();
            _resolve(content);
            return content;
        }
    }


    /**
     * Go through the whole data structure and in case there is a reference then fetch it and store is in the {@code
     * resolvedRefs} map using the macro name as a key and the actual value.
     *
     * @param content the yaml content we search for references to other yaml content macros
     * @throws BadSyntax if some of the referenced macros are not defined
     *                   <p>
     *                   // TODO alter the structure to loop from recursion
     */
    private void collectRefs(Object content) throws BadSyntax {
        if (content instanceof List) {
            for (final var item : (List<Object>) content) {
                collectRefs(item);
            }
        } else if (content instanceof Map) {
            for (final var entry : ((Map<Object, Object>) content).entrySet()) {
                collectRefs(entry.getKey());
                collectRefs(entry.getValue());
            }
        } else if (content instanceof Ref) {
            Ref ref = (Ref) content;
            if (!resolvedRefs.containsKey(ref.id)) {
                var yaml = Resolve.getYaml(processor, ref.id).getObject();
                while (yaml instanceof Ref) {
                    yaml = Resolve.getYaml(processor, ((Ref) yaml).id).getObject();
                }
                if (clone) {
                    yaml = clone(yaml);
                }
                resolvedRefs.put(ref.id, yaml);
                collectRefs(yaml);
            }
        }/*else there is nothing to do, either some arbitrary object or just a string, or number */
    }

    /**
     * Dereference the references in the structures that were referenced from the resolved yaml object.
     */
    private void dereferenceRefs() throws BadSyntax {
        resolved.clear();
        for (final var entry : resolvedRefs.entrySet()) {
            stackLimiter = new StackLimiter();
            _resolve(entry.getValue());
        }
    }

    private void _resolve(Object content) throws BadSyntax {
        stackLimiter.up();
        if (resolved.containsKey(content)) {
            stackLimiter.down();
            return;
        }
        resolved.put(content, null);
        if (content instanceof List) {
            resolveList((List<Object>) content);
            stackLimiter.down();
            return;
        }
        if (content instanceof Map) {
            resolveMap((Map<Object, Object>) content);
            stackLimiter.down();
            return;
        }
        stackLimiter.down();
    }

    private Object resolveRef(Object obj) {
        if (obj instanceof Ref) {
            final var id = ((Ref) obj).id;
            if (copy && usedRefs.contains(id)) {
                return clone(resolvedRefs.get(id));
            } else {
                usedRefs.add(id);
                return resolvedRefs.get(id);
            }
        } else {
            return obj;
        }
    }

    private Object clone(Object content) {
        final var out = new StringWriter();
        yaml.dump(content, out);
        return yaml.load(out.toString());
    }

    private void resolveMap(Map<Object, Object> map) throws BadSyntax {
        for (final Map.Entry<Object, Object> e : map.entrySet()) {
            final var key = resolveRef(e.getKey());
            final var value = resolveRef(e.getValue());
            _resolve(key);
            _resolve(value);
            map.put(key, value);
        }
    }

    private void resolveList(List<Object> list) throws BadSyntax {
        final var arrayList = (ArrayList<Object>) list;
        for (int i = 0; i < arrayList.size(); i++) {
            final var value = resolveRef(arrayList.get(i));
            _resolve(value);
            arrayList.set(i, value);
        }
    }
}
