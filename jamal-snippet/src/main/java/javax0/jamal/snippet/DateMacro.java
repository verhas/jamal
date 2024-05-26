package javax0.jamal.snippet;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

@Name("date")
public class DateMacro implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        InputHandler.skipWhiteSpaces(in);
        final var formatter = new SimpleDateFormat(in.toString());
        return formatter.format(new Date());
    }
}
