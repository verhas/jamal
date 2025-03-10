package javax0.jamal.builtins;

import javax0.jamal.api.*;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.*;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

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
        final var javaDefined = scanner.str(null, "class").optional();
        final var defaults = scanner.bool(null, "named");
        // snipline RestrictedDefineParameters filter="(.*)"
        final var IdOnly = scanner.bool("RestrictedDefineParameters");
        scanner.done();
        final var validator = Parop.validator(processor);

        ScannerTools.badSyntax(this).whenBooleans(noRedefineParam, optionalParam).multipleAreTrue();
        ScannerTools.badSyntax(this).whenBooleans(globalParam, exportParam).multipleAreTrue();
        skipWhiteSpaces(input);
        boolean verbatim = verbatimParam.is();
        boolean tailParams = tailParamsParam.is();
        if (!verbatim && firstCharIs(input, DEFINE_VERBATIM)) {
            verbatim = true;
            skip(input, 1);
            skipWhiteSpaces(input);
        }
        var optional = firstCharIs(input, DEFINE_OPTIONALLY);
        var noRedefine = firstCharIs(input, ERROR_REDEFINE);
        if (validator
                .when(optionalParam.is() && optional).then("You cannot use %s and '?' in the define at the same time.", optionalParam.name())
                .when(noRedefineParam.is() && noRedefine).then("You cannot use %s and '!' in the define at the same time.", noRedefineParam.name())
                .anyFailed()) {
            return "";
        }

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
        if (existing
                .filter(m -> !(m instanceof Identified.Undefined))
                .isPresent()) {
            if (optional) {
                return "";
            }
            if (validator.when(!softDefined(existing.get()) && noRedefine).then("The macro '%s' was already defined.", id)
                    .hasFailed()) {
                return "";
            }
        }
        skipWhiteSpaces(input);
        if (validator.when(id.endsWith(":") && !firstCharIs(input, '(')).then("The () in define is not optional when the macro name ends with ':'.").hasFailed()) {
            return "";
        }
        final String[] params;
        final Map<String, String> paramDefaults;
        if (defaults.isPresent()) {
            paramDefaults = getParametersWithDefaults(processor, input, id);
            params = paramDefaults.keySet().toArray(String[]::new);
        } else {
            paramDefaults = new LinkedHashMap<>();
            params = getParameters(input, id);
        }

        if (IdOnly.is() && validator.when(!Arrays.stream(params).allMatch(InputHandler::isIdentifier)).then("The parameters of the define must be identifiers.").hasFailed()) {
            return "";
        }

        final var pure = firstCharIs(input, ':');
        if (pure) {
            skip(input, 1);
        }
        if (validator.when(!firstCharIs(input, '=')).then("define '%s' has no '=' to body", id).hasFailed()) {
            return "";
        }
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
                configurable.configure(Configurable.Keys.PURE, true);
            }
            if (defaults.is()) {
                try {
                    configurable.configure(Configurable.Keys.XTENDED, true);
                    configurable.configure(Configurable.Keys.DEFAULTS, paramDefaults);
                } catch (Exception e) {
                    processor.deferredThrow(new BadSyntax("The defaults parameter is invalid.", e));
                }
            }
        }
        return "";
    }

    /**
     * Return true if the user-defined macro is soft-defined (it is loaded from a reference file, in which case you can
     * still '{'@define ! xxx '}' later.
     *
     * @param userDefinedMacro the user-defined macro we want to examine
     * @return {@code true} if the macro was soft-defined
     */
    private boolean softDefined(Identified userDefinedMacro) {
        return Optional.of(userDefinedMacro)
                .filter(m -> m instanceof Configurable).map(Configurable.class::cast)
                .map(c -> c.get(Configurable.Keys.SOFT))
                .filter(Optional::isPresent).map(Optional::get)
                .filter(t -> t instanceof Boolean).map(Boolean.class::cast)
                .filter(Boolean::booleanValue)
                .isPresent();
    }

    private Identified createFromClass(Processor processor, String className, String id, Input in, boolean verbatim, boolean tailParams, String... params) throws BadSyntax {
        return Throwing.of(() -> Class.forName(className.trim()), "Class '%s' not found.", className)
                .hurl("Class '%s' does not implement Identified and Evaluable.", className)
                .when(klass -> !(Identified.class.isAssignableFrom(klass)) || !(Evaluable.class.isAssignableFrom(klass)))
                .map(Class::getConstructor, "Class '%s' has no default constructor.", className)
                .map(Constructor::newInstance, "Class '%s' cannot be instantiated.", className)
                .when(Configurable.class, c -> {
                    c.configure(Configurable.Keys.ID, id);
                    c.configure(Configurable.Keys.VERBATIM, verbatim);
                    c.configure(Configurable.Keys.TAIL, tailParams);
                    c.configure(Configurable.Keys.PARAMS, params);
                    c.configure(Configurable.Keys.PROCESSOR, processor);
                    c.configure(Configurable.Keys.INPUT, in);
                })
                .hurl("Class '%s' cannot be cast to Identified.", className)
                .cast(Identified.class)
                .get();
    }
}
/*template jm_define
{template |define|define [$O$]$M$($P$)=$V$|Define a Jamal user defined macro|
  {variable |O|"verbatim tail optional fail pure global export class"}
  {variable |M|"MACRO_NAME"}
  {variable |P|"$p1,$p2,..."}
  {variable |V|"..."}
}
 */