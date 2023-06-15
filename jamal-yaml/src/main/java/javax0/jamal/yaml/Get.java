package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import ognl.Ognl;
import ognl.OgnlException;

import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;
import static javax0.jamal.tools.Params.holder;


public class Get implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var copy = Resolver.copyOption();
        final var from = holder("yamlDataSource", "from").asString();
        Scan.using(processor).from(this).between("()").keys(clone, copy, from).parse(in);
        final var fromId = Set.getFromId(in,from,this);
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

    @Override
    public String getId() {
        return "yaml:get";
    }
}
