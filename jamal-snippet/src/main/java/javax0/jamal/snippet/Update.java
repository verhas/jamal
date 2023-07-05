package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@Deprecated(since = "2.4.0", forRemoval = true)
public class Update implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var head = Params.<String>holder("head").orElse("");
        final var tail = Params.<String>holder("tail").orElse("");

        final var start = Params.<Pattern>holder("start").orElse(
                "^\\s*" +
                        Pattern.quote(processor.getRegister().open()) +
                        "\\s*(?:#|@)\\s*snip\\s+([$_:a-zA-Z][$_:a-zA-Z0-9]*)\\s*$").asPattern();
        final var stop = Params.<Pattern>holder("stop").orElse(
                "^\\s*" + Pattern.quote(processor.getRegister().close()) + "\\\\?\\s*$").asPattern();
        Scan.using(processor).from(this).firstLine().keys(head, tail, start, stop).parse(in);

        BadSyntax.when(in.getPosition().file == null,
                "Cannot invoke update from an environment that has no file name");

        return "";
    }

    @Override
    public String getId() {
        return "snip:update";
    }
}
