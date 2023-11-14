package javax0.jamal.engine;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;

/**
 * A little utility class used solely in {@code UserDefinedMacro} and in {@code ScriptMacro} to ensure the proper use of
 * the macro arguments. In case of user defined macros none of the argument can contain any other argument. This
 * restriction is to avoid non-deterministic behavior. TO be honest the behaviour would not be non-deterministic but
 * rather a deterministic behaviour that is not intuitive. In user defined macros the parameters, as they appear in the
 * text are replaced by the actual string values that are specified for those parameters based on position. If a
 * parameter name contains another parameter name, then the macro evaluation will become ambiguous. Should it replace
 * the longer parameter with its value or the shorter one included in the longer one. This type of use would lead to
 * confusion and much less readability. Jamal, honestly, gives so much possibility to create unreadable and cryptic
 * macros, so we just avoid a pitfall that we can.
 */
class ArgumentHandler {

    final String[] parameters;
    private final Identified owner;
    private final int max;
    private final int min;
    private static final String ELIPSIS = "...";

    /**
     * Constructs an ArgumentHandler object with the specified owner and parameters.
     * <p>
     * This constructor initializes the ArgumentHandler with the provided owner and an array of parameters.
     * It parses the parameters to determine the minimum and maximum number of arguments accepted.
     * The special syntax '...' (ellipsis) is used to indicate variable argument lengths.
     *
     * @param owner      The identified owner of the ArgumentHandler.
     * @param parameters An array of Strings representing the parameters.
     * @throws BadSyntax If the syntax of the parameters is incorrect. This exception is thrown in the following cases:
     *                   - If more than one parameter contains the '...' syntax.
     *                   - If the '...' syntax is used improperly, such as having 'xxx...' in a single parameter macro,
     *                   or having a 'xxx...' argument that is not the last one in the array.
     *                   <p>
     *                   The constructor performs the following operations:
     *                   1. If only one parameter is provided, and it equals '...', sets min to 0 and max to Integer.MAX_VALUE.
     *                   2. If multiple parameters are provided, it checks each parameter for the presence of '...' at the start or end.
     *                   - If '...' is found at the start (indicating a minimum number of arguments), the method sets the 'min' field.
     *                   - If '...' is found at the end (indicating an unlimited maximum number of arguments), the method sets the 'max' field.
     *                   3. If no special syntax is found, it sets 'min' and 'max' to the length of the 'parameters' array.
     */
    public ArgumentHandler(Identified owner, String[] parameters) throws BadSyntax {
        this.owner = owner;
        if (parameters.length == 1 && parameters[0].equals(ELIPSIS)) {
            min = 0;
            max = Integer.MAX_VALUE;
            this.parameters = new String[0];
            return;
        }
        int min = -1, max = -1;
        if (parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].startsWith(ELIPSIS)) {
                    if (min == -1) {
                        min = i;
                        parameters[i] = parameters[i].substring(ELIPSIS.length()).trim();
                    } else {
                        throw new BadSyntax("There can only be one '...xxx' argument in a define.");
                    }
                }
                if (parameters[i].endsWith(ELIPSIS)) {
                    BadSyntax.when(parameters.length == 1, "One parameter macro cannot have 'xxx...' argument.");
                    if (max == -1 && i == parameters.length - 1) {
                        max = Integer.MAX_VALUE;
                        parameters[i] = parameters[i].substring(0, parameters[i].length() - ELIPSIS.length()).trim();
                    } else {
                        throw new BadSyntax("There can only be one 'xxx...' argument in a define, and it has to be the last one.");
                    }
                }
            }

            this.min = min == -1 ? parameters.length : min;
            this.max = max == -1 ? parameters.length : max;
        } else {
            this.min = this.max = 0;
        }
        this.parameters = parameters;
    }

    /**
     * Adjust the actual arguments according to the number of parameters expected so that the macro evaluation after
     * this call can safely use the argument array assuming that there are exactly enough number of arguments.
     * <p>
     * The normal behaviour of Jamal is to throw {@code BadSyntax} exception in case the actual number of arguments are
     * different from the number of arguments expected. Starting with version 1.0.3 and later this behaviour is
     * controlled by the option {@code lenient} that can be set using the {@code OptionsStore} in the macro source code.
     * If the option {@code lenient} is present then the actual number of arguments can be different from the number
     * expected. In this case the extra arguments are ignored and the missing arguments are added with the value empty
     * string. (At each macro use there can only be one of those cases, there cannot be more argument than needed and
     * the same time less.)
     * <p>
     * This method adjusts the actual argument array or throws an exception.
     *
     * @param actualValues the arguments passed to the macro.
     * @param lenient      if this parameter is {@code true} then the argument array will be adjusted if the number of
     *                     the parameters is not matching. If this parameter is {@code false} then the method will throw
     *                     {@code BadSyntax} exception for the same case.
     * @return the adjusted array, which may be the same array as the argument {@code actualValues} or a newly allocated
     * array with the values copied from {@code actualValues} and with extra empty strings to have the proper length.
     * @throws BadSyntax if the length of the array {@code actualValues} is not then same as the length of the required
     *                   parameters, and the operation is not lenient.
     */
    String[] adjustActualValues(String[] actualValues, boolean lenient) throws BadSyntax {
        if (actualValues.length != parameters.length) {
            if (lenient) {
                final String[] adjustedValues = ArgumentHandler.this.adjustActualValues(actualValues);
                if (adjustedValues != null) return adjustedValues;
            } else {
                if (isFantomParameter(actualValues)) {
                    return new String[0];
                } else {
                    if (actualValues.length < min || actualValues.length > max) {
                        final BadSyntax badSyntax;
                        if (min == max) {
                            if (Objects.equals(owner.getId(), Identified.DEFAULT_MACRO) && parameters.length > 0 &&
                                    (parameters[0].equals(Identified.MACRO_NAME_ARG1) || parameters[0].equals(Identified.MACRO_NAME_ARG2))) {
                                badSyntax = new BadSyntax(format("Macro '%s' needs %d arguments and got %d",
                                        actualValues[0], parameters.length, actualValues.length));
                            } else {
                                badSyntax = new BadSyntax(format("Macro '%s' needs %d arguments and got %d",
                                        owner.getId(), parameters.length, actualValues.length));
                            }
                        } else {
                            badSyntax = new BadSyntax(format("Macro '%s' needs (%s ... %s) arguments and got %d",
                                    owner.getId(), "" + min, (max == Integer.MAX_VALUE ? "inf" : "" + max), actualValues.length));
                        }
                        for (final var actual : actualValues) {
                            badSyntax.parameter(actual);
                        }
                        throw badSyntax;
                    } else {
                        final String[] adjustedValues = adjustActualValues(actualValues);
                        if (adjustedValues != null) return adjustedValues;
                    }
                }
            }
        }
        return actualValues;
    }

    private String[] adjustActualValues(String[] actualValues) {
        if (actualValues.length < parameters.length) {
            final var adjustedValues = Arrays.copyOf(actualValues, parameters.length);
            for (int i = 0; i < adjustedValues.length; i++) {
                if (adjustedValues[i] == null) {
                    adjustedValues[i] = "";
                }
            }
            return adjustedValues;
        }
        return null;
    }

    /**
     * When a macro is invoked and there are macros in the parameters of the macro that all evaluate to zero string,
     * then it is parsed as a single space only parameter, but it is not a real parameter. For example
     *
     * <pre>{@code
     *      [@define a=[b]]
     *      [a [@define b=this is b]]
     * }</pre>
     * <p>
     * Sees that there is one parameter, {@code [@define b=this is b]} but this evaluates to an empty string so it is
     * not really a parameter. It is a phantom parameter. When the user defined macro does not expect any parameter, and
     * it gets one empty string, or space only parameter then this will be treated as okay: no parameter.
     *
     * @param actualValues the actual parameter values
     * @return {@code true} if the one parameter is not a fantom parameter
     */
    private boolean isFantomParameter(String[] actualValues) {
        if (parameters.length != 0 || actualValues.length != 1) {
            return false;
        }
        for (final var ch : actualValues[0].toCharArray()) {
            if (!Character.isWhitespace(ch)) {
                return false;
            }
        }
        return true;
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
