package javax0.jamal.scriptbasic;

import com.scriptbasic.api.ScriptBasic;
import com.scriptbasic.api.ScriptBasicException;
import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

import java.io.StringWriter;

public class Basic implements Macro {
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        try {
            final var engine = ScriptBasic.engine();
            final var output = new StringWriter();
            engine.setOutput(output);
            final var code = in.toString().trim();
            if (!code.contains("\n") &&
                (code.length() < 5 || !code.substring(0, 5).equalsIgnoreCase("PRINT"))) {
                engine.eval("PRINT " + code);
            } else {
                engine.eval(code);
            }
            return engine.getOutput().toString();
        } catch (ScriptBasicException e) {
            throw new BadSyntax("Syntax exception in the BASIC code",e);
        }
    }

    public String getId() {
        return "script::basic";
    }
}
