package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.jamal.tools.Throwing;

import java.lang.reflect.Constructor;
import java.util.Arrays;

import static javax0.jamal.api.SpecialCharacters.*;
import static javax0.jamal.tools.InputHandler.*;

public class Define implements Macro, OptionsControlled.Core {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var verbatimParam = Params.<Boolean>holder(null, "verbatim").asBoolean();
        final var tailParamsParam = Params.<Boolean>holder(null, "tail").asBoolean();
        final var optionalParam = Params.<Boolean>holder(null, "optional", "ifNotDefined").asBoolean();
        final var noRedefineParam = Params.<Boolean>holder(null, "fail", "noRedefine", "noRedef", "failIfDefined").asBoolean();
        final var pureParam = Params.<Boolean>holder(null, "pure").asBoolean();
        final var globalParam = Params.<Boolean>holder(null, "global").asBoolean();
        final var exportParam = Params.<Boolean>holder(null, "export").asBoolean();
        final var javaDefined = Params.<Boolean>holder(null, "class").asBoolean();
        // snipline RestrictedDefineParameters filter="(.*)"
        final var IdOnly = Params.<Boolean>holder("RestrictedDefineParameters").asBoolean();
        Scan.using(processor).from(this).between("[]").keys(verbatimParam, tailParamsParam, optionalParam, noRedefineParam, pureParam, globalParam, exportParam, IdOnly, javaDefined).parse(input);
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

        final String[] params = getParameters(input, id);

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
        if (javaDefined.is()) {
            macro = createFromClass(convertGlobal(id), input.toString(), verbatim, tailParams, params);
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
        if ((pure || pureParam.is()) && macro instanceof Configurable) {
            ((Configurable) macro).configure("pure", true);
        }
        return "";
    }

    private Identified createFromClass(String id, String className, boolean verbatim, boolean tailParams, String... params) throws BadSyntax {
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
                })
                .cast(Identified.class)
                .get();
    }
}
