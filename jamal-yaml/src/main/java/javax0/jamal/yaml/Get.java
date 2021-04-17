package javax0.jamal.yaml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import ognl.Ognl;
import ognl.OgnlException;

import static javax0.jamal.tools.Params.holder;


public class Get implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var clone = Resolver.cloneOption();
        final var from = holder("yamlDataSource", "from").asString();
        Params.using(processor).keys(clone, from).between("()").parse(in);
        final var id = from.get();
        InputHandler.skipWhiteSpaces(in);
        try {
            final var expression = Ognl.parseExpression(in.toString());
            final var yamlObject = Resolve.getYaml(processor, id);
            Resolver.resolve(yamlObject, processor, clone.get());
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
