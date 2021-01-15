package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Escape implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        if (in.charAt(0) != '`') {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters.", in.getPosition());
        }
        InputHandler.skip(in, 1);
        final var endOfEscape = in.indexOf("`");
        if (endOfEscape == -1) {
            throw new BadSyntaxAt("The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", in.getPosition());
        }
        final var escapeSequence = "`" + in.subSequence(0, endOfEscape).toString() + "`";
        InputHandler.skip(in, escapeSequence.length() - 1);
        final var endOfString = in.indexOf(escapeSequence);
        if (endOfString == -1) {
            throw new BadSyntaxAt("I cannot find the escape string at the end of the macro: " + escapeSequence, in.getPosition());
        }
        final var escapedString = in.substring(0,endOfString);
        InputHandler.skip(in,escapedString);
        InputHandler.skip(in,escapeSequence);
        InputHandler.skipWhiteSpaces(in);
        if( in.length() > 0 ){
            throw new BadSyntaxAt("There are extra characters in the use of {@escape } after the closing escape sequence: " + escapeSequence, in.getPosition());
        }
        return escapedString;
    }
}
