package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Resolve implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        Scan.using(processor).from(this).between("()").keys(clone, copy).parse(in);

        for (final var id : Arrays.stream(in.toString().split(",")).map(String::trim).collect(Collectors.toSet())) {
            final var yamlObject = getYaml(processor, id);
            Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        }
        return "";
    }

    static YamlObject getYaml(Processor processor, String id) throws BadSyntax {
        final var identified = processor.getRegister().getUserDefined(id).orElseThrow(
                () -> new BadSyntax("Cannot resolve yaml '" + id + "', does not exists"));
        BadSyntax.when(!(identified instanceof YamlObject),  "The user defined macro '%s' is not a YAML structure", id);
        return (YamlObject) identified;
    }

    @Override
    public String getId() {
        return "yaml:resolve";
    }
}
