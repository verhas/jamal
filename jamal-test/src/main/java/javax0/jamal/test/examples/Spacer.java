package javax0.jamal.test.examples;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

// snippet Spacer
public class Spacer implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        InputHandler.skipWhiteSpaces(in);
        if (in.length() > 0) {
            final var result = javax0.jamal.tools.Input.makeInput("", in.getPosition());
            boolean lineStart = true;
            while (in.length() > 0) {
                if (!lineStart)
                    result.append(' ');
                lineStart = in.charAt(0) == '\n';
                InputHandler.move(in, 1, result);
            }
            return result.toString();
        } else {
            return "";
        }
    }
}
// end snippet