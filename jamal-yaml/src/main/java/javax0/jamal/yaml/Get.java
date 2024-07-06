package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import ognl.Ognl;
import ognl.OgnlException;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

@Macro.Name("yaml:get")
public class Get implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var clone = Resolver.cloneOption(scanner);
        final var copy = Resolver.copyOption(scanner);
        final var from = scanner.str("yamlDataSource", "from").optional();
        scanner.done();
        final var fromId = Set.getFromId(in, from, this);
        skipWhiteSpaces(in);
        try {
            final var expression = Ognl.parseExpression(in.toString());
            final var yamlObject = Resolve.getYaml(processor, fromId);
            Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
            return String.valueOf(Ognl.getValue(expression, yamlObject.getObject()));
        } catch (OgnlException e) {
            throw new BadSyntax("Syntax error in the OGNL expression '" + in + "'", e);
        }
    }
}
