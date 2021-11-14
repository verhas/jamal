package javax0.jamal.tools;

import javax0.jamal.api.Identified;
import javax0.jamal.api.Processor;
import javax0.jamal.api.SpecialCharacters;

import java.util.HashMap;
import java.util.Map;

public class OptionsStore implements Identified {
    public static final String OPTIONS_MACRO_ID = "`options";
    private final Map<String, OptionValue> map = new HashMap<>();

    public static class OptionValue {
        private static final OptionValue FALSE = new OptionValue(false);
        private boolean value;
        private boolean stale = false;

        public boolean is() {
            if (stale) {
                throw new RuntimeException("Internal error using stale OptionValue");
            }
            return value;
        }

        OptionValue(boolean value) {
            this.value = value;
        }
    }

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
        return processor.getRegister()
            .getUserDefined(OPTIONS_MACRO_ID)
            .map(userDefinedMacro -> (OptionsStore) userDefinedMacro)
            .orElseGet(
                () -> {
                    final var options = new OptionsStore();
                    processor.define(options);
                    return options;
                }
            );
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
    public void addOptions(final String... options) {
        for (final var option : options) {
            if (option.length() > 0 && option.charAt(0) == SpecialCharacters.OPTION_NEGATE) {
                final String opt;
                if (option.length() > 1 && option.charAt(1) == SpecialCharacters.GLOBAL_NAME_CHAR) {
                    opt = option.substring(2);
                } else {
                    opt = option.substring(1);
                }
                if (map.containsKey(opt)) {
                    map.get(opt).value = false;
                } else {
                    map.put(opt, new OptionValue(false));
                }
            } else {
                final String opt;
                if (option.length() > 0 && option.charAt(0) == SpecialCharacters.GLOBAL_NAME_CHAR) {
                    opt = option.substring(1);
                } else {
                    opt = option;
                }
                if (map.containsKey(opt)) {
                    map.get(opt).value = true;
                } else {
                    map.put(opt, new OptionValue(true));
                }
            }
        }
    }

    /**
     * Pull the entries from the other options store into this options store. In case this store already has an
     * OptionValue for the option then the value of the other store same option will be copied. If there is no
     * OptionValue for a given key then the whole OptionValue is inherited from the other store. Inherited in this
     * case means that we copy the reference to that OptionValue and after the reference copy this store will "own" that
     * OptionValue just as well as the other store.
     * <p>
     * This method is used when the options are exported to one level up. In that case any reference to an option value
     * from the lower level will be working after the export. Albeit it is probably not a good practice to reference any
     * option value, which is not on the top, global level.
     * <p>
     * If there was already an option value in this option store then we will use the one, where we copy and set the
     * other one stale. Reading the value of a stale option value will throw an exception.
     *
     * @param other
     */
    public void pullFrom(final OptionsStore other) {
        for (final var entry : other.map.entrySet()) {
            if (map.containsKey(entry.getKey())) {
                map.get(entry.getKey()).value = entry.getValue().value;
                entry.getValue().stale = true;
            } else {
                map.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * This method is used when the options are "imported" from level down, and it copies the existing values from the
     * other store to {@code this}. When this copy happens each key will get in this store a newly created OptionValue.
     * This also assumes that this option store is empty.
     * <p>
     * <em>Rationale:</em>
     * When an option or options are set and there is no option store defined on the last level, then a new store is
     * created and the already existing options are inherited from the higher level. It may not be the immediate next
     * level, but some level above. The copy does not need to go higher because the next higher level was created the
     * same way. This method does not care about levels and such. Simply {@code other} is the store from the level up,
     * and this method copies the values.
     *
     * @param other the other options store from where the values are copied
     */
    public void copyFrom(final OptionsStore other) {
        for (final var entry : other.map.entrySet()) {
            map.put(entry.getKey(), new OptionValue(entry.getValue().value));
        }
    }

    /**
     * Decides if a certain option was set in this option store.
     *
     * @param option the option we look for
     * @return {@code true} if the option was set for this store
     */
    public boolean is(final String option) {
        return map.getOrDefault(option, OptionValue.FALSE).is();
    }

    /**
     * Return the value of the option. This method allows some optimization for options defined on the top level.
     * When an is defined this value is contains either {@code true} or {@code false}. This value is returned by the
     * {@link OptionValue#is()} method. When the value of an option changes on the same level then the value in this
     * object is changed. This object is mutable, as a value holder, and it is assigned to the option on the level it
     * is defined. If a code once got the reference to this object it can safely retain the reference and check the
     * value of the option later without any lookup.
     * <p>
     * The use of this method for non-global options may be tricky and most probably will lead to bugs. It is designed
     * to help the caching of global options to speed up the performance of some built in macros and the evaluation of
     * user defined macros (especially to convert the option 'lenient' to a global macro and handle it faster).
     *
     * @param option the name of the option
     * @return the value object of the option that can be queries using the {@link OptionValue#is()} method.
     */
    public OptionValue value(final String option) {
        final String opt = InputHandler.convertGlobal(option);
        if (!map.containsKey(opt)) {
            map.put(opt, new OptionValue(false));
        }
        return map.get(opt);
    }
}
