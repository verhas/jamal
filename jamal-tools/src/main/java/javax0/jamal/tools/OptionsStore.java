package javax0.jamal.tools;

import javax0.jamal.api.Processor;
import javax0.jamal.api.Identified;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OptionsStore implements Identified {
    public static final String OPTIONS_MACRO_ID = "`options";
    private static final OptionsStore NO_OPTIONS = new OptionsStore();
    private final Set<String> optionSet = new HashSet<>();

    public static OptionsStore getInstance(Processor processor) {
        final var optionsMacro = processor.getRegister().getUserDefined(OPTIONS_MACRO_ID);
        return optionsMacro.map(userDefinedMacro -> (OptionsStore) userDefinedMacro).orElse(NO_OPTIONS);
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

    public void addOptions(String... options) {
        optionSet.addAll(Arrays.asList(options));
    }

    public void addOptions(Set<String> options) {
        optionSet.addAll(options);
    }

    public Set<String> getOptions() {
        return optionSet;
    }

    public boolean is(String option) {
        return optionSet.contains(option);
    }
}
