package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.lang.System.Logger.Level;

public class Log implements Macro, Scanner.Core {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var levelString = scanner.str(null, "level").defaultValue("info");
        scanner.done();
        final Level level;
        try {
            level = Level.valueOf(levelString.get());
        } catch (Exception e) {
            throw new BadSyntax("The level " + levelString.get() + " is not a valid level");
        }
        processor.logger().log(level, in.getPosition(), in.toString());
        return "";
    }
}
/*template jm_log
{template |log|log [$O$] $C$|log a message|
  {variable |O|"log=info"}
  {variable |C|"..."}
}
 */