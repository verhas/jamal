package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;
import ognl.Ognl;
import ognl.OgnlException;

import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.InputHandler.startsWith;


public class Set implements Macro, InnerScopeDependent, Scanner {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in,processor);
        final var clone = Resolver.cloneOption(scanner);
        final var copy = Resolver.copyOption(scanner);
        final var from = scanner.str("yamlDataSource", "from").optional();
        scanner.done();

        skipWhiteSpaces(in);
        final var id = fetchId(in);
        BadSyntax.when(in.length() == 0 || in.charAt(0) != '=', "There is no '=' after the identifier in '%s'", getId());
        skip(in, 1);
        skipWhiteSpaces(in);

        final String fromId = getFromId(in, from, this);

        try {
            final var yamlObject = Resolve.getYaml(processor, fromId);
            final var expression = Ognl.parseExpression(in.toString());
            Resolver.resolve(yamlObject, processor, clone.is(), copy.is());
            final var newObject = new YamlObject(processor, id, Ognl.getValue(expression, yamlObject.getObject()));
            processor.getRegister().define(newObject);
            processor.getRegister().export(id);
            return "";
        } catch (OgnlException e) {
            throw new BadSyntax("Syntax error in the OGNL expression '" + in + "'", e);
        }
    }

    static String getFromId(Input in, StringParameter from, Macro me) throws BadSyntax {
        final String fromId;
        if (!from.isPresent() || from.get() == null) {
            if (startsWith(in, "/") == 0) {
                skip(in, 1);
                fromId = fetchId(in);
                BadSyntax.when(startsWith(in, ".") == -1, "The macro name at the start of the OGNL expression must be followed by a . (dot) character in the macro %s", me.getId());
                skip(in, 1);
            } else {
                throw new BadSyntax(String.format("The 'from' macro name is not specified in the macro %s", me.getId()));
            }
        } else {
            fromId = from.get();
        }
        return fromId;
    }

    @Override
    public String getId() {
        return "yaml:set";
    }
}
