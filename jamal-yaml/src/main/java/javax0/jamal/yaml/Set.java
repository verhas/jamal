package javax0.jamal.yaml;

import javax0.jamal.api.*;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import ognl.Ognl;
import ognl.OgnlException;

import static javax0.jamal.tools.InputHandler.*;
import static javax0.jamal.tools.Params.holder;


public class Set implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        final var from = holder("yamlDataSource", "from").orElseNull().asString();
        Scan.using(processor).from(this).between("()").keys(clone, copy, from).parse(in);

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

    static String getFromId(Input in, Params.Param<String> from, Macro me) throws BadSyntax {
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
