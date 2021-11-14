package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;
import javax0.jamal.tools.OptionsStore;

import java.util.Arrays;

public class Options implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        final var optArray = in.toString().replaceAll("\\s", "").split("\\|", -1);
        final var options = Arrays.stream(optArray)
            .filter(s -> s.indexOf(SpecialCharacters.GLOBAL_NAME_CHAR) == -1)
            .toArray(String[]::new);

        createNewAndCopyFromOld(processor, options);

        final var globalOptions = Arrays.stream(optArray)
            .filter(s -> s.indexOf(SpecialCharacters.GLOBAL_NAME_CHAR) != -1)
            .toArray(String[]::new);

        storeGlobalOptions(processor, globalOptions);
        return "";
    }

    /**
     * Store a global option in the global options store.
     *
     * @param processor
     * @param globalOptions
     */
    private void storeGlobalOptions(Processor processor, String[] globalOptions) {
        final var optionsStore = processor.getRegister().getUserDefined(SpecialCharacters.GLOBAL_NAME_CHAR + OptionsStore.OPTIONS_MACRO_ID)
            .filter(OptionsStore.class::isInstance)
            .map(OptionsStore.class::cast)
            .orElseGet(
                () -> {
                    final var globalOptionsStore = new OptionsStore();
                    processor.defineGlobal(globalOptionsStore);
                    return globalOptionsStore;
                }
            );
        optionsStore.addOptions(globalOptions);
    }

    /**
     * Create a new options store if there is a need for one and copy all options from the old one. First this method
     * checks if there is an options store on the current writable level. If there is no options store on the current
     * level, then
     * <p>
     * <ol>
     *   <li>Create a new options store.</li>
     *   <li>Get the user defined macro named {@code `options} from the macro registry.
     *       This is an {@code OptionsStore}.</li>
     *   <li>Copy the options values to the new options store.</li>
     *   <li>Merge the new options into the new options store. Now we have all the new values and the existing ones.</li>
     *   <li>Register the new options store.</li>
     * </ol>
     * <p>
     * If the existing store was defined the same level as this one, then it will be replaced.
     * <p>
     * If the existing store was defined in a larger (upper) level, then it will be shadowed and when this context
     * closes then that one will get into effect again.
     *
     * @param processor the processor in which we are working
     * @param options   the options
     */
    private void createNewAndCopyFromOld(final Processor processor, final String[] options) {
        final var register = processor.getRegister();
        final var optionsStore = register
            .getLocalUserDefined(OptionsStore.OPTIONS_MACRO_ID)
            .map(OptionsStore.class::cast)
            .orElseGet(() -> {
                final OptionsStore newStore = new OptionsStore();
                register.<OptionsStore>getUserDefined(OptionsStore.OPTIONS_MACRO_ID)
                    .ifPresent(store -> newStore.copyFrom(store));
                processor.define(newStore);
                return newStore;
            });
        optionsStore.addOptions(options);
    }
}
