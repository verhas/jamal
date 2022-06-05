package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Optional;
import java.util.regex.Pattern;

public class Mock implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        // snippet mock_options
        Params.Param<String> id = Params.<String>holder(null, "macro", "id").asString(); //| the identifier of the macro.
        //|This option is mandatory and has to define the identifier of the macro to be mocked.
        Params.Param<String> when = Params.<String>holder(null, "when").orElseNull(); //| regular expression when to apply the mock.
        //|This option is not mandatory.
        //|In case it is specified, the mock response will only be used when the input of the macro matches the regular expression specified.
        //|If the option is missing the mock response will always be matched and used when it gets activated regardless of the input of the macro.
        Params.Param<Integer> repeat = Params.<Integer>holder(null, "repeat", "times").orElseInt(1); //| how many times the mock can be used.
        //|Can specify how many times the mock can be used.
        //|It is an error to use a negative number.
        //|You can use zero to switch off the mock response in your text temporarily without deleting it.
        Params.Param<Boolean> infinite = Params.<Boolean>holder(null, "inf", "infinite", "forever").asBoolean(); //| if the mock be used infinite number of times.
        //|Can be used to specify that the mock response can be used unlimited number of times.
        // end snippet
        Params.using(processor).from(this).between("()").keys(id, when, repeat, infinite).parse(in);

        if(repeat.isPresent() && infinite.isPresent()){
            throw new BadSyntax("You cannot use options 'repeat' and 'infinite' at the same time.");
        }
        if(repeat.isPresent() && repeat.get() < 0 ){
            throw new BadSyntax("The option 'repeat' should be non-negative.");
        }

        final var register = processor.getRegister();
        final var macro = register.getMacroLocal(id.get())
                .filter(m -> m instanceof MockImplementation).map(m -> (MockImplementation) m);
        final MockImplementation mock;
        if (macro.isPresent()) {
            mock = macro.get();
        } else {
            final Optional<Macro> shadowedMacro = register.getMacro(id.get());
            mock = new MockImplementation(id.get(), shadowedMacro.orElse(null));
            register.define(mock);
        }
        final Pattern inputCheck = when.isPresent() ? Pattern.compile(when.get()) : null;
        mock.response(in.toString(), when.isPresent(), inputCheck, infinite.is(), repeat.get());
        return "";
    }
}
