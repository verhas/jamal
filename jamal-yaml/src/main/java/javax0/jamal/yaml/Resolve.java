package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;

import java.util.Arrays;
import java.util.stream.Collectors;

@Macro.Name("yaml:resolve")
public
class Resolve implements Macro, InnerScopeDependent, Scanner {

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var clone = Resolver.cloneOption(scanner);
        final var copy = Resolver.copyOption(scanner);
        scanner.done();

        for (final var id : Arrays.stream(in.toString().split(",")).map(String::trim).collect(Collectors.toSet())) {
            final var yamlObject = getYaml(processor, id);
            Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        }
        return "";
    }

    static YamlObject getYaml(Processor processor, String id) throws BadSyntax {
        final var identified = processor.getRegister().getUserDefined(id).orElseThrow(
                () -> new BadSyntax("Cannot resolve yaml '" + id + "', does not exists"));
        BadSyntax.when(!(identified instanceof YamlObject), "The user defined macro '%s' is not a YAML structure", id);
        return (YamlObject) identified;
    }

}
