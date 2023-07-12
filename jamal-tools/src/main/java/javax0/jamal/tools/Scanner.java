package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.param.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Macros that use parameters can implement this interface to get the parameters parsed in an easier way.
 * <p>
 * The introduction of this interface was motivated by the fact that the macro definitions had to specify
 * parameters to the scanning redundantly. After calling the {@code Scan.using()} method they had to call
 * {@code from(this)} to pass the current instance of the macro to the scanner.
 * <p>
 * In addition to that, the call also had to list all the parameter variables so that the scanner could parse
 * the values and store them into the different objects. It was checked during run time and threw an exception
 * when an object not passed to the parser was used, but many times this error was triggered during the development.
 * <p>
 * When using this interface, the macro parameter handling is simplified. The macro is supposed to "implement" this
 * interface, or one of the sub interfaces. These also provide a declarative way to specify the delimiters of the
 * parameters. The scanner is created by calling the {@code newScanner()} method of the macro. The scanner object
 * returned has methods to specify the parameters.
 * <p>
 * When a parameter is returned, the scanner object also stores it in a list. When the {@code done()} method is called
 * the scanner object passes the list of parameters to the scanner, and the scanner parses the input and stores the
 * values in the parameter objects. This way, there is no need to specify the parameter objects twice, once in the
 * macro definition and once in the scanner call.
 * <p>
 * The delimiter specification works so that the scanner object will use {@code ()} as the delimiters. However, when the
 * class implements the {@link FirstLine} interface then the parameter parsing is delimited by the first line of.
 * When the class implements the {@link WholeInput} interface then the parameter parsing goes to the end of the input.
 * Changing the delimiter is simply changing the interface name the macro implements.
 */
public interface Scanner {

    /**
     * The object that holds the parsing parameters, like the processor and the input and does the parsing.
     * The name of the class is not user-friendly, but it is not supposed to be used by the user. A new instance
     * is created by the {@link #newScanner(Input, Processor)} method. Since this method is defined in the surrounding
     * interface implemented by the macro there is no need to pass the {@code this} reference to the scanner.
     */
    class ScannerObject {
        private final Processor processor;
        private final Input in;
        private final Identified macro;
        private Function<Params, Params> setDelimiters;

        private final ArrayList<Params.Param<?>> params = new ArrayList<>();

        private Params.ExtraParams extraParams = null;

        public ScannerObject(Processor processor, Input in, Identified macro) {
            this.processor = processor;
            this.in = in;
            this.macro = macro;
        }

        /**
         * Define an arbitrary type parameter. Use this method if none of the other methods is suitable.
         *
         * @param keys the name and the aliases of the parameter
         * @param <T>  the parameter type
         * @return the parameter object
         */
        public <T> Params.Param<T> param(String... keys) {
            final var param = Params.<T>holder(keys);
            params.add(param);
            return param;
        }

        /**
         * Define a list of strings parameter.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public Params.Param<List<String>> list(String... keys) {
            final var param = Params.<Boolean>holder(keys).asBoolean();
            params.add(param);
            return param.asList(String.class);
        }

        /**
         * Define a boolean parameter with the names of an enum class.
         *
         * @param klass is the enumeration class. The names of the enum constants are used as the aliases of the parameter.
         * @return the parameter object
         */
        public <K> EnumerationParameter enumeration(Class<K> klass) {
            if (klass.getEnumConstants().length == 0) {
                throw new IllegalArgumentException("The enumeration class " + klass.getName() + " has no constants.");
            }
            final var keys = new String[klass.getEnumConstants().length + 1];
            keys[0] = null; // the first element is the name, the enum names are all aliases
            final var i = new Object() {
                int i = 1;
            };
            Arrays.stream(klass.getEnumConstants())
                    .map(e -> (Enum<?>) e)
                    .map(Enum::name).forEach(s -> keys[i.i++] = s);
            final var param = Params.<Boolean>holder(keys).asBoolean();
            params.add(param);
            final var eparam = new EnumerationParameter(param);
            eparam.setEnum(klass);
            return eparam;
        }

        /**
         * Define a boolean parameter.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public BooleanParameter bool(String... keys) {
            final var param = Params.<Boolean>holder(keys).asBoolean();
            params.add(param);
            return new BooleanParameter(param);
        }

        /**
         * Define a string parameter.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public StringParameter str(String... keys) {
            final var param = Params.<String>holder(keys).asString();
            params.add(param);
            return new StringParameter(param);
        }

        /**
         * Define a file parameter. The file name is resolved relative to the directory of the input.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public Params.Param<String> file(String... keys) {
            final var param = Params.<String>holder(keys).asString();
            params.add(param);
            return param.as(s -> FileTools.absolute(in.getReference(), s));
        }

        /**
         * Define a regular expression pattern parameter.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public PatternParameter pattern(String... keys) {
            final var param = Params.<Pattern>holder(keys).asPattern();
            params.add(param);
            return new PatternParameter(param);
        }

        /**
         * Define an integer parameter.
         *
         * @param keys the name and the aliases of the parameter
         * @return the parameter object
         */
        public IntegerParameter number(String... keys) {
            final var param = Params.<Pattern>holder(keys).asInt();
            params.add(param);
            return new IntegerParameter(param);
        }

        /**
         * Signal that the parsing accepts just any parameter and does not check the parameter names.
         * These extra parameters are stored in a map and can be queried from the returned object.
         *
         * @return the extra parameter object
         */
        public Params.ExtraParams extra() {
            extraParams = new Params.ExtraParams();
            return extraParams;
        }

        /**
         * Finish the parameter parsing.
         * This method should be called after all the parameters are defined.
         *
         * @throws BadSyntax if the input does not match the parameter definitions
         */
        public void done() throws BadSyntax {

            final var p = setDelimiters.apply(Params.using(processor).from(macro));
            final Params z;
            if (extraParams == null) {
                z = p.keys(params.toArray(new Params.Param<?>[0]));
            } else {
                z = p.keys(extraParams, params.toArray(new Params.Param<?>[0]));
            }
            z.parse(in);
        }

        public ScannerObject delimiterSetter(Function<Params, Params> setDelimiters) {
            this.setDelimiters = setDelimiters;
            return this;
        }
    }

    private static ScannerObject nso(Processor processor, Input in, Scanner it) {
        if (!(it instanceof Identified)) {
            throw new IllegalArgumentException("The Scanner interface can only be used by Macros.");
        }
        return new ScannerObject(processor, in, (Identified) it);
    }

    /**
     * The scanner interface that uses the {@code ()} characters as delimiters.
     */
    default ScannerObject newScanner(Input in, Processor processor) {
        return nso(processor, in, this).delimiterSetter(p -> p.between("()"));
    }

    interface Core extends Scanner {
        default ScannerObject newScanner(Input in, Processor processor) {
            return nso(processor, in, this).delimiterSetter(p -> p.between("[]"));
        }
    }

    /**
     * The scanner interface that uses the first line of the input.
     */
    interface FirstLine extends Scanner {
        default ScannerObject newScanner(Input in, Processor processor) {
            return nso(processor, in, this).delimiterSetter(Function.identity());
        }
    }

    /**
     * The scanner interface that uses the whole input.
     */
    interface WholeInput extends Scanner {
        default ScannerObject newScanner(Input in, Processor processor) {
            return nso(processor, in, this).delimiterSetter(Params::tillEnd);
        }
    }
}
