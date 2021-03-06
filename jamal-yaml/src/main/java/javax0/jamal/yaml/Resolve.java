package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import org.yaml.snakeyaml.Yaml;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Resolve implements Macro, InnerScopeDependent {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        Params.using(processor).keys(clone, copy).between("()").parse(in);

        for (final var id : Arrays.stream(in.toString().split(",")).map(String::trim).collect(Collectors.toSet())) {
            final var yamlObject = getYaml(processor, id);
            Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        }
        return "";
    }

    static YamlObject getYaml(Processor processor, String id) throws BadSyntax {
        final var identified = processor.getRegister().getUserDefined(id).orElseThrow(
            () -> new BadSyntax("Cannot resolve yaml '" + id + "', does not exists"));
        if (!(identified instanceof YamlObject)) {
            throw new BadSyntax("The user defined macro '" + id + "' is not a YAML structure");
        }
        return (YamlObject) identified;
    }

    @Override
    public String getId() {
        return "yaml:resolve";
    }
}
