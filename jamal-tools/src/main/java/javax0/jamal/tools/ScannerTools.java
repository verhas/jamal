package javax0.jamal.tools;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.tools.param.AbstractTypedParameter;
import javax0.jamal.tools.param.BooleanParameter;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class ScannerTools {

    private AbstractTypedParameter<?>[] parameters;
    private final String id;

    private ScannerTools(String id) {
        this.id = id;
    }

    public static ScannerTools badSyntax(Identified macro) throws BadSyntax {
        return new ScannerTools(macro.getId());
    }

    public static ScannerTools badSyntax(String id) throws BadSyntax {
        return new ScannerTools(id);
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
        public void anyAreTrue(String msg) throws BadSyntax {
            var found = 0;
            for (var p : parameters) {
                if (((BooleanParameter) p).is()) {
                    found++;
                }
            }
            if (found >= 1) {
                throw new BadSyntax(msg);
            }
        }

        public void multipleAreTrue() throws BadSyntax {
            var found = 0;
            for (var p : parameters) {
                if (((BooleanParameter) p).is()) {
                    found++;
                }
            }
            if (found > 1) {
                throw new BadSyntax(String.format("In the macro '%s' only one of %s can be true.",
                        id,
                        parameterNameList(p -> {
                            try {
                                return ((BooleanParameter) p).is();
                            } catch (BadSyntax e) {
                                return true;
                            }
                        })));
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
                throw new BadSyntax(String.format("In the macro '%s' you cannot use %s together.",
                        id,
                        parameterNameList(AbstractTypedParameter::isPresent)));
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
