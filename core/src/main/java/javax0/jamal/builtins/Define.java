package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.engine.UserDefinedMacro;

import static javax0.jamal.tools.InputHandler.*;

public class Define implements Macro {
    @Override
    public String evaluate(StringBuilder input, Processor processor) throws BadSyntax {
        skipWhiteSpaces(input);
        var id = fetchId(input);
        skipWhiteSpaces(input);
        final String[] params;
        if (firstCharIs(input, '(')) {
            skip(input,1);
            var closingParen = input.indexOf(")");
            if( !contains(closingParen)){
                throw new BadSyntax();
            }
            var param = input.substring(0,closingParen);
            skip(input,closingParen+1);
            skipWhiteSpaces(input);
            params = param.split(",");
            trim(params);
        }else{
            params = new String[0];
        }
        if( !firstCharIs(input,'=')){
            throw new BadSyntax();
        }
        skip(input,1);
        var macro = new UserDefinedMacro(id,input.toString(),params);
        processor.getRegister().put(macro);
        return "";
    }

    // should be implemented in default method reading annotation of the class and using the name of the class
    @Override
    public String getId() {
        return "define";
    }
}
