package javax0.jamal.py;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.io.IOException;

public class PythonMacro implements Macro {

    final PythonInterpreter interpreter;
    final String id;
    final String function;

    public PythonMacro(PythonInterpreter interpreter, String id, String function) {
        this.interpreter = interpreter;
        this.id = id;
        this.function = function;
    }


    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var escapedInput = in.toString().replaceAll("\"", "\\\\\"");
        try {
            return interpreter.execute(String.format("%s(\"\"\"%s\"\"\")\n", function, escapedInput));
        } catch (IOException e) {
            throw new BadSyntax("Error while executing python code for " + id, e);
        }
    }

    @Override
    public String getId() {
        return id;
    }
}
