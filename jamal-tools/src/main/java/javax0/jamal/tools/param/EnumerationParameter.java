package javax0.jamal.tools.param;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.tools.Params;

import java.util.Optional;

public class EnumerationParameter extends AbstractTypedParameter{

    public EnumerationParameter(Params.Param<Boolean> param) {
        super(param);
    }

    private Class<?> enumClass = null;
    private Object enumDefault = null;

    public void setEnum(Class<?> klass) {
        this.enumClass = klass;
    }

    public <K> void setEnumDefault(K enumDefault) {
        if (enumDefault != null && !enumDefault.getClass().isAssignableFrom(enumClass)) {
            throw new IllegalArgumentException(String.format("The parameter '%s' is not '%s' type", param.name(), enumDefault.getClass().getName()));
        }
        this.enumDefault = enumDefault;
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

    public <K> Optional<K> enumeration(Class<K> klass) throws BadSyntax {
        BadSyntax.when(enumClass == null, "The parameter '%s' is not an enumeration", param.name());
        if (!enumClass.isAssignableFrom(klass)) {
            throw new IllegalArgumentException(String.format("The parameter '%s' is not '%s' type", param.name(), klass.getName()));
        }
        if (param.is()) {
            try {
                final var enumValue = Enum.valueOf((Class<Enum>) enumClass, param.name());
                return Optional.of((K) enumValue);
            } catch (IllegalArgumentException e) {
                throw new BadSyntax("The value '" + param.get() + "' is not a valid value for the enumeration " + enumClass.getName());
            }
        } else {
            if (enumDefault != null) {
                return Optional.of((K) enumDefault);
            }
            return Optional.empty();
        }
    }

    public static class WithDefault extends AbstractTypedParameter {
        private final EnumerationParameter supi;
        public WithDefault(EnumerationParameter supi) {
            super(supi.param);
            this.supi = supi;
        }

        public <K> K enumeration(Class<K> klass) throws BadSyntax {
            BadSyntax.when(supi.enumClass == null, "The parameter '%s' is not an enumeration", supi.param.name());
            if (supi.enumDefault == null) {
                throw new IllegalArgumentException("The parameter '" + supi.param.name() + "' has no default value.");
            }
            if (!supi.enumClass.isAssignableFrom(klass)) {
                throw new IllegalArgumentException(String.format("The parameter '%s' is not '%s' type", supi.param.name(), klass.getName()));
            }
            if (supi.param.is()) {
                try {
                    final var enumValue = Enum.valueOf((Class<Enum>) supi.enumClass, supi.param.name());
                    return (K) enumValue;
                } catch (IllegalArgumentException e) {
                    throw new BadSyntax("The value '" + supi.param.get() + "' is not a valid value for the enumeration " + supi.enumClass.getName());
                }
            } else {
                return (K) supi.enumDefault;
            }
        }
    }
}
