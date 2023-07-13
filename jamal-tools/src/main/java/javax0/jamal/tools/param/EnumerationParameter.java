package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

public class EnumerationParameter extends AbstractTypedParameter<Boolean> {

    public EnumerationParameter(Params.Param<Boolean> param, Class<?> enumClass) {
        super(param);
        this.enumClass = enumClass;
    }

    private final Class<?> enumClass;
    private Object enumDefault = null;

    public <K> EnumerationParameter defaultValue(K enumDefault) {
        if (enumDefault != null && !enumDefault.getClass().isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(String.format("The parameter '%s' is not '%s' type", param.name(), enumDefault.getClass().getName()));
        }
        this.enumDefault = enumDefault;
        return this;
    }

    /**
     * Get the value of the parameter as an optional enumeration object.
     * If the parameter was not specified, then the returned optional will contain the default value if it was
     * specified, or it will be empty.
     *
     * @param klass the enumeration type. Has to be the same time specified in the parameter of the method
     *              {@link javax0.jamal.tools.Scanner.ScannerObject#enumeration(Class) scanner.enumeration()}.
     *              If the type is different, a runtime exception is thrown.
     * @param <K>   the type of the enumeration
     * @return the value of the parameter as an optional enumeration object.
     * @throws BadSyntax if the parameter was specified multiple times.
     */
    public <K> K get(Class<K> klass) throws BadSyntax {
        BadSyntax.when(enumClass == null, "The parameter '%s' is not an enumeration", param.name());
        if (enumDefault == null) {
            throw new IllegalArgumentException("The parameter '" + param.name() + "' has no default value.");
        }
        if (!enumClass.isAssignableFrom(klass)) {
            throw new IllegalArgumentException(String.format("The parameter '%s' is not '%s' type", param.name(), klass.getName()));
        }
        if (param.is()) {
            try {
                final var enumValue = Enum.valueOf((Class<Enum>) enumClass, param.name());
                return (K) enumValue;
            } catch (IllegalArgumentException e) {
                throw new BadSyntax("The value '" + param.get() + "' is not a valid value for the enumeration " + enumClass.getName());
            }
        } else {
            return (K) enumDefault;
        }
    }
}
