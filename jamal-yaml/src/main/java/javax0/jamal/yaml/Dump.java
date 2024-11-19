package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;


public class Dump implements Macro, InnerScopeDependent, Scanner {
    final Yaml yaml = YamlFactory.newYaml();

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var scanner = newScanner(input,processor);
        final var clone = Resolver.cloneOption(scanner);
        final var copy = Resolver.copyOption(scanner);
        scanner.done();

        InputHandler.skipWhiteSpaces(input);
        final var id = InputHandler.fetchId(input);
        InputHandler.skipWhiteSpaces(input);
        final var from = InputHandler.fetchId(input);
        BadSyntax.when(!"to".equals(from), "Yaml:dump needs a 'to' after the identifier");
        InputHandler.skipWhiteSpaces(input);
        var reference = input.getReference();
        var fileName = FileTools.absolute(reference, input.toString().trim());

        final var yamlObject = Resolve.getYaml(processor, id);
        Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
        try (final var writer = new FileWriter(fileName)) {
            yaml.dump(yamlObject.getObject(), writer);
        } catch (IOException ioe) {
            throw new BadSyntax("Not possible to dump yaml '" + id + "' into the file '" + fileName + "'", ioe);
        }
        return "";
    }

    @Override
    public String getId() {
        return "yaml:dump";
    }
}
