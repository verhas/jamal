package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.Throwing;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static javax0.jamal.api.SpecialCharacters.*;
import static javax0.jamal.tools.InputHandler.*;

public class Define implements Macro, OptionsControlled.Core, Scanner.Core {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        final var verbatimParam = scanner.bool(null, "verbatim");
        final var tailParamsParam = scanner.bool(null, "tail");
        final var optionalParam = scanner.bool(null, "optional", "ifNotDefined");
        final var noRedefineParam = scanner.bool("noRedefine", "fail", "noRedef", "failIfDefined");
        final var pureParam = scanner.bool(null, "pure");
        final var globalParam = scanner.bool(null, "global");
        final var exportParam = scanner.bool(null, "export");
        final var javaDefined = scanner.str(null, "class");
        final var defaults = scanner.bool(null, "named");
        // snipline RestrictedDefineParameters filter="(.*)"
        final var IdOnly = scanner.bool("RestrictedDefineParameters");
        scanner.done();

        BadSyntax.when(noRedefineParam.is() && optionalParam.is(), "You cannot use %s and %s", optionalParam.name(), noRedefineParam.name());
        BadSyntax.when(globalParam.is() && exportParam.is(), "You cannot use %s and %s", optionalParam.name(), noRedefineParam.name());
        skipWhiteSpaces(input);
        boolean verbatim = verbatimParam.is();
        boolean tailParams = tailParamsParam.is();
        if (!verbatim && firstCharIs(input, DEFINE_VERBATIM)) {
            verbatim = true;
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var optional = firstCharIs(input, DEFINE_OPTIONALLY);
        BadSyntax.when(optionalParam.is() && optional, "You cannot use %s and '?' in the define at the same time.", optionalParam.name());

        var noRedefine = firstCharIs(input, ERROR_REDEFINE);
        BadSyntax.when(noRedefineParam.is() && noRedefine, "You cannot use %s and '!' in the define at the same time.", noRedefineParam.name());

        if (optional || noRedefine) {
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        optional = optional || optionalParam.is();
        noRedefine = noRedefine || noRedefineParam.is();
        if (!verbatim && firstCharIs(input, DEFINE_VERBATIM)) {
            verbatim = true;
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var id = fetchId(input);
        final var existing = processor.getRegister().getUserDefined(id);
        if (existing.isPresent() && !(existing.get() instanceof Identified.Undefined)) {
            if (optional) {
                return "";
            }
            BadSyntax.when(noRedefine, "The macro '%s' was already defined.", id);
        }
        skipWhiteSpaces(input);
        BadSyntax.when(id.endsWith(":") && !firstCharIs(input, '('), "The () in define is not optional when the macro name ends with ':'.");
        final String[] params;
        final Map<String, String> paramDefaults;
        if (defaults.isPresent()) {
            paramDefaults = getParametersWithDefaults(processor, input, id);
            params = paramDefaults.keySet().toArray(String[]::new);
        } else {
            paramDefaults = new LinkedHashMap<>();
            params = getParameters(input, id);
        }

        if (IdOnly.is()) {
            BadSyntax.when(!Arrays.stream(params).allMatch(InputHandler::isIdentifier), "The parameters of the define must be identifiers.");
        }

        final var pure = firstCharIs(input, ':');
        if (pure) {
            skip(input, 1);
        }
        BadSyntax.when(!firstCharIs(input, '='), "define '%s' has no '=' to body", id);
        skip(input, 1);
        final Identified macro;
        if (javaDefined.isPresent()) {
            macro = createFromClass(processor, javaDefined.get(), convertGlobal(id), input, verbatim, tailParams, params);
        } else {
            macro = processor.newUserDefinedMacro(convertGlobal(id), input.toString(), verbatim, tailParams, params);
        }
        if (globalParam.is() || isGlobalMacro(id)) {
            processor.defineGlobal(macro);
        } else {
            processor.define(macro);
            if (exportParam.is()) {
                processor.getRegister().export(macro.getId());
            }
        }
        if (macro instanceof Configurable) {
            final var configurable = (Configurable) macro;
            if (pure || pureParam.is()) {
                configurable.configure("pure", true);
            }
            if (defaults.is()) {
                try {
                    configurable.configure("xtended", true);
                    configurable.configure("defaults", paramDefaults);
                } catch (Exception e) {
                    throw new BadSyntax("The defaults parameter is invalid.", e);
                }
            }
        }
        return "";
    }

    private Identified createFromClass(Processor processor, String className, String id, Input in, boolean verbatim, boolean tailParams, String... params) throws BadSyntax {
        return Throwing.of(() -> Class.forName(className.trim()), "Class '%s' not found.", className)
                .hurl("Class '%s' does not implement Identified and Evaluable.", className)
                .when(klass -> !(Identified.class.isAssignableFrom(klass)) || !(Evaluable.class.isAssignableFrom(klass)))
                .map(Class::getConstructor, "Class '%s' has no default constructor.", className)
                .map(Constructor::newInstance, "Class '%s' cannot be instantiated.", className)
                .when(Configurable.class, c -> {
                    c.configure("id", id);
                    c.configure("verbatim", verbatim);
                    c.configure("tail", tailParams);
                    c.configure("params", params);
                    c.configure("processor", processor);
                    c.configure("input", in);
                })
                .hurl("Class '%s' cannot be cast to Identified.", className)
                .cast(Identified.class)
                .get();
    }
}
