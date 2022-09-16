package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.lang.System.Logger.Level;

public class Log implements Macro {

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final Params.Param<String> levelString = Params.<String>holder(null, "level").orElse("info").asString();
        Scan.using(processor).from(this).between("[]").keys(levelString).parse(in);
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
