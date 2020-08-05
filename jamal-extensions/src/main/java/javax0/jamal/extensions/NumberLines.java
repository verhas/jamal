package javax0.jamal.extensions;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class NumberLines implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var format = UDMacro.macro("$format").from(processor).orElse("%d. ");
        final var startS = UDMacro.macro("$start").from(processor).orElse("1");
        final var stepS = UDMacro.macro("$step").from(processor).orElse("1");
        final int start;
        try {
            start = Integer.parseInt(startS);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax("$start is not a number");
        }
        final int step;
        try {
            step = Integer.parseInt(stepS);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax("$step is not a number");
        }
        InputHandler.skipWhiteSpaces2EOL(in);
        int i = 0;
        final var sb = in.getSB();
        int lineNr = start;
        while (i > -1) {
            final var formattedNr = String.format(format, lineNr);
            sb.insert(i, formattedNr);
            i += formattedNr.length();
            lineNr += step;
            i = sb.indexOf("\n", i);
            if( i != -1 ){
                i++;
            }
            if( i >= sb.length() ){
                break;
            }
        }
        return in.toString();
    }
}
