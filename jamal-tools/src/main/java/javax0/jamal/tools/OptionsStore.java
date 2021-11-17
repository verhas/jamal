package javax0.jamal.tools;

import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;

import java.util.Arrays;

public class OptionsStore implements Identified {
    final Processor processor;

    private OptionsStore(Processor processor) {
        this.processor = processor;
    }


    /**
     * Get the options store that works with this processor.
     *
     * @param processor the processor of which we need the options store
     * @return the options store.
     */
    public static OptionsStore getInstance(Processor processor) {
        return new OptionsStore(processor);
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
        return null;
    }

    /**
     * Add the options to the options store. If an option starts with the {@code ~} character then it will be removed
     * from the options. This way it is possible to switch off an option.
     *
     * @param options the options to add to the store
     */
    public void addOptions(final String... options) {
        Arrays.stream(options)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.charAt(0) == SpecialCharacters.OPTION_NEGATE)
            .map(s -> s.substring(1))
            .forEach(this::removeOption);
        Arrays.stream(options)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .filter(s -> s.charAt(0) != SpecialCharacters.OPTION_NEGATE)
            .forEach(this::addOption);
    }

    private void removeOption(String option) {
        setOption(option, false);
    }

    private void addOption(String option) {
        setOption(option, true);
    }

    private void setOption(String name, boolean value) {
        final var m = processor.getRegister()
            .getUserDefined(name)
            .filter(mac -> mac instanceof Option)
            .map(Option.class::cast)
            .orElseGet(() -> {
                final var isGlobal = InputHandler.isGlobalMacro(name);
                final var local = InputHandler.convertGlobal(name);
                final var option = new Option(local);
                if (isGlobal) {
                    processor.defineGlobal(option);
                } else {
                    processor.define(option);
                }
                return option;
            });
        m.set(value);
    }

    /**
     * Decides if a certain option was set in this option store.
     *
     * @param option the option we look for
     * @return {@code true} if the option was set for this store
     */
    public boolean is(final String option) {
        return processor.getRegister()
            .getUserDefined(option)
            .filter(m -> m instanceof Option)
            .map(Option.class::cast)
            .map(Option::getObject)
            .orElse(false);
    }
}
