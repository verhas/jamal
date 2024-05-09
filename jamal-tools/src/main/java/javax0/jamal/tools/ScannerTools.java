package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.tools.param.AbstractTypedParameter;
import javax0.jamal.tools.param.BooleanParameter;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ScannerTools {

    private final String format;
    private final Object[] formatParameters;
    private AbstractTypedParameter<?>[] parameters;
    private final Identified identified;

    private ScannerTools(Identified identified, String format, Object... parameters) {
        this.format = format;
        this.formatParameters = parameters;
        this.identified = identified;
    }

    public static ScannerTools badSyntax(Identified macro) throws BadSyntax {
        return new ScannerTools(macro, "");
    }

    public static ScannerTools badSyntax(String format, Object... parameters) throws BadSyntax {
        return new ScannerTools(null, format, parameters);
    }

    public ScannerTools.Are whenParameters(AbstractTypedParameter<?>... parameters) {
        this.parameters = parameters;
        return new Are();
    }

    public ScannerTools.BoolAre whenBooleans(BooleanParameter... parameters) {
        this.parameters = parameters;
        return new BoolAre();
    }

    public class BoolAre {
        public void multipleAreTrue() throws BadSyntax {
            var found = 0;
            for (var p : parameters) {
                if (((BooleanParameter)p).is()) {
                    found++;
                }
            }
            if (found > 1) {
                if (format.isEmpty()) {
                    throw new BadSyntax(String.format("In the macro '%s' only one of %s can be true.",
                            identified.getId(),
                            parameterNameList(p -> {
                                try {
                                    return ((BooleanParameter)p).is();
                                } catch (BadSyntax e) {
                                    return true;
                                }
                            })));

                } else {
                    BadSyntax.format(format, formatParameters);
                }
            }
        }
    }

    public class Are {
        public void multipleArePresent() throws BadSyntax {
            var found = 0;
            for (var p : parameters) {
                if (p.isPresent()) {
                    found++;
                }
            }
            if (found > 1) {
                if (format.isEmpty()) {
                    throw new BadSyntax(String.format("In the macro '%s' you cannot use %s together.",
                            identified.getId(),
                            parameterNameList(AbstractTypedParameter::isPresent)));

                } else {
                    BadSyntax.format(format, formatParameters);
                }
            }
        }
    }

    private String parameterNameList(Predicate<AbstractTypedParameter<?>> predicate) {
        return Arrays.stream(parameters)
                .filter(predicate)
                .map(AbstractTypedParameter::name)
                .map(s -> "'" + s + "'")
                .collect(Collectors.joining(", "));
    }
}
