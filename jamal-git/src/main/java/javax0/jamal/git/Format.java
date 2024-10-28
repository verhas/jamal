package javax0.jamal.git;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;

import java.text.SimpleDateFormat;

@Macro.Name("git:format")
public class Format implements Macro, Scanner {
    @Override
    public String evaluate(Input in, javax0.jamal.api.Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet format_parameters
        final var time = scanner.str(null, "time");
        // is the time as returned by some of the git commands, in number of seconds since the epoch.
        // end snippet
        scanner.done();

        final var date = new java.util.Date(Long.parseLong(time.get())*1000);
        InputHandler.skipWhiteSpaces(in);
        final var sdf = new SimpleDateFormat(in.toString());
        return sdf.format(date);
    }
}
