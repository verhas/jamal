package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

public class Rot13 implements Macro {

    private static final String source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String target = "NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm";

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        InputHandler.skipWhiteSpaces(in);
        final StringBuilder sb = new StringBuilder(in.toString());
        for (int i = 0; i < sb.length(); i++) {
            final int index = source.indexOf(sb.charAt(i));
            if (index != -1) {
                sb.setCharAt(i, target.charAt(index));
            }
        }
        return sb.toString();
    }

}
