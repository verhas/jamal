package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.regex.Pattern;

public class Mock implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        Params.Param<String> id = Params.<String>holder(null, "macro", "id").asString();
        Params.Param<String> when = Params.<String>holder(null, "when").orElseNull();
        Params.Param<Integer> repeat = Params.<Integer>holder(null, "repeat", "times").orElseInt(1);
        Params.Param<Boolean> infinite = Params.<Boolean>holder(null, "inf", "infinite", "forever").asBoolean();
        Params.using(processor).from(this).between("()").keys(id, when, repeat, infinite).parse(in);

        final var register = processor.getRegister();
        final var macro = register.getMacroLocal(id.get())
                .filter(m -> m instanceof MockImplementation).map(m -> (MockImplementation) m);
        final MockImplementation mock;
        if (macro.isPresent()) {
            mock = macro.get();
        } else {
            mock = new MockImplementation(id.get());
            register.define(mock);
        }
        final Pattern inputCheck = when.isPresent() ? Pattern.compile(when.get()) : null;
        mock.response(in.toString(), when.isPresent(), inputCheck, infinite.is(),repeat.get());
        return "";
    }
}
