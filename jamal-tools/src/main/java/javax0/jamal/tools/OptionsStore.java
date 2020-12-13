package javax0.jamal.tools;

import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;

import java.util.HashSet;
import java.util.Set;

public class OptionsStore implements Identified {
    public static final String OPTIONS_MACRO_ID = "`options";
    private static final OptionsStore NO_OPTIONS = new OptionsStore();
    private final Set<String> optionSet = new HashSet<>();

    /**
     * Get the options store that belongs to this processor at this very moment.
     * <p>
     * Note that this is not safe to store the result of this method for longer time, because the actual store may be
     * changed when the scope of the processor is going up closing a macro nesting level.
     *
     * @param processor the processor of which we need the options store
     * @return the options store. If there was no option defined for this processor then it returns a constant empty
     * options store
     */
    public static OptionsStore getInstance(Processor processor) {
        final var optionsMacro = processor.getRegister().getUserDefined(OPTIONS_MACRO_ID);
        return optionsMacro.map(userDefinedMacro -> (OptionsStore) userDefinedMacro).orElse(NO_OPTIONS);
    }

    /**
     * The name of the macro is {@code `options} that starts with a backtick. This is a character that is not allowed in
     * a macro name. This way the macro instances will be stored in the macro register when it gets registered
     * programmatically, but the macro source cannot reference it and also the built-in macro {@code define} will not
     * overwrite it.
     *
     * @return the constant string {@code `options}
     */
    @Override
    public String getId() {
        return OPTIONS_MACRO_ID;
    }

    /**
     * Add the options to this options store. If an option starts with the {@code ~} character then it will be removed
     * from the options. This way it is possible to switch off an option.
     *
     * @param options the options to add to the store
     */
    public void addOptions(String... options) {
        for (final var option : options) {
            if (option.length() > 0 && option.charAt(0) == '~') {
                optionSet.remove(option.substring(1));
            } else {
                optionSet.add(option);
            }
        }
    }

    /**
     * Add the options to this options store. Note that the set of options in this case come from an already existing
     * options store, and thus the strings MUST NOT and CANNOT contain the leading {@code ~} character.
     *
     * @param options the options to add to the store
     */
    public void addOptions(Set<String> options) {
        optionSet.addAll(options);
    }

    /**
     * Get the set of options.
     *
     * @return the options set
     */
    public Set<String> getOptions() {
        return optionSet;
    }

    /**
     * Decides if a certain option was set in this option store.
     *
     * @param option the option we look for
     * @return {@code true} if the option was set for this store
     */
    public boolean is(String option) {
        return optionSet.contains(option);
    }
}
