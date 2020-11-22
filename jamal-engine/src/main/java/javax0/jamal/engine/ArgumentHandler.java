package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A little utility class used solely in {@code UserDefinedMacro} and in {@code ScriptMacro} to ensure the proper use of
 * the macro arguments. In case of user defined macros none of the argument can contain any other argument. This
 * restriction is to avoid non-deterministic behavior or rather a deterministic behaviour (the code cannot really be
 * non-deterministic) that is not intuitive. In user defined macros the parameters, as they appear in the text are
 * replaced by the actual string values that are specified for those parameters based on position. If a parameter name
 * is the prefix of another parameter name, then the macro evaluation will become ambiguous. Should it replace the
 * longer parameter with its value or the shorter one included in the longer one. This type of use would lead to
 * confusion and much less readability. Jamal, honestly, gives so much possibility to create unreadable and cryptic
 * macros, so we just avoid a pitfall that we can.
 */
class ArgumentHandler {

    final String[] parameters;
    private final Identified owner;

    public ArgumentHandler(Identified owner, String[] parameters) {
        this.owner = owner;
        this.parameters = parameters;
    }

    /**
     * Adjust the actual arguments according to the number of parameters expected so that the macro evaluation after
     * this call can safely use the argument array assuming that there are exactly enough number of arguments.
     * <p>
     * The normal behaviour of Jamal is to throw {@code BadSyntax} exception in case the actual number of arguments are
     * different from the number of arguments expected. Starting with version 1.0.3 and later this behaviour is
     * controlled by the option {@code lenient} that can be set using the {@code OptionsStore}
     * in the macro source code. If the option {@code lenient} is present then the actual number of arguments can be
     * different from the number expected. In this case the extra arguments are ignored and the missing arguments are
     * added with the value empty string. (At each macro use there can only be one of those cases, there cannot be more
     * argument than needed and the same time less.)
     * <p>
     * This method adjusts the actual argument array or throws an exception.
     *
     * @param actualValues the arguments passed to the macro.
     * @param lenient      if this parameter is {@code true} then the argument array will be adjusted if the
     *                     number of the parameters is not matching. If this parameter is {@code false} then the
     *                     method will throw {@code BadSyntax} exception for the same case.
     * @return the adjusted array, which may be the same array as the argument {@code actualValues} or a newly allocated
     * array with the values copied from {@code actualValues} and with extra empty strings to have the proper
     * length.
     * @throws BadSyntax if the length of the array {@code actualValues} is not then same as the length of the required
     *                   parameters and the operation is not lenient.
     */
    String[] adjustActualValues(String[] actualValues, boolean lenient) throws BadSyntax {
        if (actualValues.length != parameters.length) {
            if (lenient) {
                if (actualValues.length < parameters.length) {
                    final var adjustedValues = Arrays.copyOf(actualValues, parameters.length);
                    for (int i = 0; i < adjustedValues.length; i++) {
                        if (adjustedValues[i] == null) {
                            adjustedValues[i] = "";
                        }
                    }
                    return adjustedValues;
                }
            } else {
                var badSyntax = new BadSyntax(String.format("Macro '%s' needs %d arguments and got %d",
                        owner.getId(),
                        parameters.length,
                    actualValues.length));
                for (final var actual : actualValues) {
                    badSyntax.parameter(actual);
                }
                throw badSyntax;
            }
        }
        return actualValues;
    }

    /**
     * @param values the values of the parameters
     * @return a map that contains the parameter names as keys and the given values as values paired up in the order as
     * they are specified.
     */
    Map<String, String> buildValueMap(String[] values) {
        final var map = new HashMap<String, String>(values.length);
        for (int i = 0; i < parameters.length; i++) {
            map.put(parameters[i], values[i]);
        }
        return map;
    }
}
