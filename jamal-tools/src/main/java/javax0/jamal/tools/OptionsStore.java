package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        getNegatedParameters(options)
                .forEach(s -> getOption(s).set(false));
        getPositiveParameters(options)
                .forEach(s -> getOption(s).set(true));
    }

    private static Stream<String> getParameters(String[] options) {
        return Arrays.stream(options)
                .map(String::trim)
                .filter(s -> !s.isEmpty());
    }

    private static List<String> getNegatedParameters(String[] options) {
        return getParameters(options)
                .filter(s -> s.charAt(0) == SpecialCharacters.OPTION_NEGATE)
                .map(s -> s.substring(1)).collect(Collectors.toList());
    }

    private static List<String> getPositiveParameters(String[] options) {
        return getParameters(options)
                .filter(s -> s.charAt(0) != SpecialCharacters.OPTION_NEGATE).collect(Collectors.toList());
    }

    public void pushOptions(final String... options) {
        getNegatedParameters(options)
                .forEach(s -> getOption(s).push(false));
        getPositiveParameters(options)
                .forEach(s -> getOption(s).push(true));
    }


    public void popOptions(final String... options) throws BadSyntax {
        BadSyntax.when(!getNegatedParameters(options).isEmpty(), "Cannot pop negated options");
        for (final var s : getPositiveParameters(options)) {
            getOption(s).pop();
        }
    }

    private static Optional<Option> cast(Optional<Identified> stream) {
        return stream.filter(m -> m instanceof Option).map(Option.class::cast);
    }

    /**
     * Get the option object if it already exists or create a new one.
     * If the name is global, then it will be looked up in the global scope only.
     * If the name is not global, then it will be looked up in the local scope only.
     * In this latter case, a new object will be created if the local scope does not contain the option even if the
     * global or any higher level scope contains the option.
     *
     * @param name the name of the option
     * @return the option object
     */
    private Option getOption(String name) {
        if (InputHandler.isGlobalMacro(name)) {
            return cast(processor.getRegister()
                    .getUserDefined(name)).orElseGet(() -> {
                final var option = new Option(InputHandler.convertGlobal(name));
                processor.defineGlobal(option);
                return option;
            });
        } else {
            return cast(processor.getRegister()
                    .getUdMacroLocal(name)).orElseGet(() -> {
                final var option = new Option(name);
                option.set(is(name));
                processor.define(option);
                return option;
            });
        }
    }

    /**
     * Decides if a certain option was set in this option store.
     *
     * @param option the option we look for
     * @return {@code true} if the option was set for this store
     */
    public boolean is(final String option) {
        return cast(processor.getRegister()
                .getUserDefined(option))
                .map(Option::getObject)
                .orElse(false);
    }
}
