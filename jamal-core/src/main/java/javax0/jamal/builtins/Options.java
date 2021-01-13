package javax0.jamal.builtins;

import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.OptionsStore;

import java.util.Arrays;

public class Options implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) {
        final var options = Arrays.stream(in.toString().split("\\|", -1))
            .map(String::trim)
            .toArray(String[]::new);

        copyOptionsFromOldStore(processor, options);
        return "";
    }

    /**
     * ,
     * <p>
     * <ol>
     *   <li>Create a new options store.</li>
     *   <li>Get the user defined macro named {@code `options} from the macro registry. This is an OptionsStore.</li>
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
    private void copyOptionsFromOldStore(final Processor processor, final String[] options) {
        final OptionsStore optionsStore = new OptionsStore();
        processor
            .getRegister()
            .<OptionsStore>getUserDefined(OptionsStore.OPTIONS_MACRO_ID)
            .ifPresent(store -> optionsStore.addOptions(store.getOptions()));
        optionsStore.addOptions(options);
        processor.define(optionsStore);
    }
}
