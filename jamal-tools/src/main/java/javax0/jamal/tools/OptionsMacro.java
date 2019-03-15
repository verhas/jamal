package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OptionsMacro implements UserDefinedMacro {
    public static final String OPTIONS_MACRO_ID = "`options";
    private final Set<String> optionSet = new HashSet<>();

    private static final OptionsMacro NO_OPTIONS = new OptionsMacro();

    public static OptionsMacro getInstance(Processor processor){
        final var optionsMacro = processor.getRegister().getUserMacro(OPTIONS_MACRO_ID);
        return optionsMacro.map(userDefinedMacro -> (OptionsMacro) userDefinedMacro).orElse(NO_OPTIONS);
    }

    /**
     * The name of the macro is {@code `options} that starts with a backtick. This is a character that is not allowed
     * in a macro name. This way the macro instances will be stored in the macro register when it gets registered
     * programmatically, but the macro source cannot reference it and also the built-in macro {@code define} will
     * not overwrite it.
     *
     * @return the constant string {@code `options}
     */
    @Override
    public String getId() {
        return OPTIONS_MACRO_ID;
    }

    /**
     * Since this is a pseudo option it is not a script option.
     *
     * @param scriptType anything, invocation will thro exception anyway
     */
    @Override
    public void setScriptType(String scriptType) {
        throw new IllegalArgumentException("setScriptType must not be invoked on OptionsMacro");
    }

    public void addOptions(String... options) {
        optionSet.addAll(List.of(options));
    }
    public void addOptions(Set<String> options) {
        optionSet.addAll(options);
    }
    public Set<String> getOptions(){
        return optionSet;
    }

    public boolean is(String option){
        return optionSet.contains(option);
    }

    /**
     * This method will be invoked programmatically and never from the macro evaluation.
     *
     * @param actualValues ignored
     * @return does not return
     */
    @Override
    public String evaluate(String... actualValues) {
        throw new IllegalArgumentException("evaluate must not be invoked on OptionsMacro");
    }
}
