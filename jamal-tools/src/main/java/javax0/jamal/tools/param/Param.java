package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.MacroReader;
import javax0.jamal.tools.OptionsStore;
import javax0.jamal.tools.Params;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class Param<K> implements Params.Param<K> {
    final public String[] key;
    List<String> value = new ArrayList<>();
    private Processor processor;
    private String macroName;
    private String defaultValue = null;
    /**
     * When calculating the final value the actual string value from the parameter or the user defined macro is needed.
     * When we return a list or a boolean then we do not need the string. It is okay if it is there, and may be used,
     * but it is not a problem, if neither the parameter nor a user defined macro defines the value.
     * <p>
     * In that case the value of the option or an empty list is returned.
     */
    private boolean stringNeeded = true;

    @Override
    public String[] keys() {
        return key;
    }

    private interface ThrowFunction<T> {
        T apply(String s) throws Exception;
    }

    private ThrowFunction<?> converter = s -> s;

    @Override
    public Param<K> orElse(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public Param<Integer> orElseInt(int defaultValue) {
        this.defaultValue = "" + defaultValue;
        this.converter = s -> getInt();
        return (Param<Integer>) this;
    }

    @Override
    public Param<K> as(Function<String, K> converter) {
        this.converter = converter::apply;
        return this;
    }

    @Override
    public <K> Param<K> as(Class<K> klass, Function<String, K> converter) {
        this.converter = converter::apply;
        return (Param<K>)this;
    }

    @Override
    public Param<Integer> asInt() {
        this.converter = s -> getInt();
        return (Param<Integer>) this;
    }

    @Override
    public Param<Boolean> asBoolean() {
        stringNeeded = false;
        this.converter = s -> getBoolean();
        return (Param<Boolean>) this;
    }

    @Override
    public Param<String> asString() {
        this.converter = s -> s;
        return (Param<String>) this;
    }

    @Override
    public Param<List<?>> asList() {
        stringNeeded = false;
        this.converter = s -> getList();
        return (Param<List<?>>) this;
    }

    @Override
    public void inject(Processor processor, String macroName) {
        this.processor = processor;
        this.macroName = macroName;
    }

    public Param(String ...key) {
        this.key = key;
    }

    @Override
    public void set(String value) {
        this.value.add(value);
    }

    /**
     * Get the value of a parameter as a string.
     *
     * @return the single value in an optional, or empty optional if the key was not present on the input and was not
     * defined in any user defined macro
     * @throws BadSyntax if there are multiple values for this key or if the key is not allowed for this macro
     */
    private Optional<String> _get() throws BadSyntax {
        if (value.size() > 0) {
            if (value.size() > 1 && stringNeeded) {
                throw new BadSyntax("The key '" + key[0] + "' must not be multi valued in the macro '" + macroName + "'");
            }
            return Optional.ofNullable(value.get(0));
        }
        final var reader = MacroReader.macro(processor);
        return reader.readValue(key[0]);
    }


    private String getRaw() throws BadSyntax {
        final var opt = _get();
        if (opt.isEmpty() && defaultValue == null && stringNeeded) {
            throw new BadSyntax("The key '" + key[0] + "' for the macro '" + macroName + "' is mandatory");
        }
        return opt.orElse(defaultValue);
    }

    private boolean calculated = false;
    private K cachedValue = null;

    /**
     * Get the value of the parameter.
     *
     * @return the value of the parameter
     * @throws BadSyntax if the parameter evaluation is faulty
     */
    public K get() throws BadSyntax {
        if (processor == null) {
            throw new IllegalArgumentException("The parameter variable '" + key[0] + "' was not processed during parsing.");
        }
        try {
            if (!calculated) {
                cachedValue = (K) converter.apply(getRaw());
                calculated = true;
            }
            return cachedValue;
        } catch (BadSyntax bs) {
            throw bs;
        } catch (Exception e) {
            throw new BadSyntax("There was an exception converting the parameter '" + key[0] + "' for the macro '" + macroName + "'", e);
        }
    }

    /**
     * @return {@code true} if the key was defined on the input or as user defined macro and the value is not "0",
     * "false" or "no", OR if the key was defined as an option, then the value of the option
     * @throws BadSyntax when the underlying call to {@link #_get()} throws
     */
    private boolean getBoolean() throws BadSyntax {
        if (value.size() > 1) {
            throw new BadSyntax("The key '" + key[0] + "' must not be multi valued in the macro '" + macroName + "'");
        }
        if (value.size() > 0) {
            return !value.get(0).equals("false") && !value.get(0).equals("no") && !value.get(0).equals("0");
        } else {
            return OptionsStore.getInstance(processor).is(key[0]);
        }
    }

    /**
     * @return the possibly empty list of the values
     * @throws BadSyntax if there was no parameter defined with the name {@code key} and evaluating the user defined
     *                   macro of the same name throws up.
     */
    private List<String> getList() throws BadSyntax {
        if (value.size() > 0) {
            return value;
        } else {
            return MacroReader.macro(processor).readValue(key[0]).map(List::of).orElse(List.of());
        }
    }

    /**
     * Gets the value assigned to the {@code key} calling {@link #_get()} and converts it to an optional int.
     *
     * @return optional int value of the parameter
     * @throws BadSyntax if the used }{@link #_get()} throws up, or if the value cannot be converted to int
     */
    private int getInt() throws BadSyntax {
        final var string = getRaw();
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException nfe) {
            throw new BadSyntax(key[0] + " is not a number using the macro '" + macroName + "'.");
        }
    }
}
