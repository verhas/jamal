package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Configurable;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;

import java.util.Arrays;

import static javax0.jamal.api.SpecialCharacters.DEFINE_OPTIONALLY;
import static javax0.jamal.api.SpecialCharacters.DEFINE_VERBATIM;
import static javax0.jamal.api.SpecialCharacters.ERROR_REDEFINE;
import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.getParameters;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Define implements Macro {
    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var verbatimParam = Params.<Boolean>holder(null, "verbatim").asBoolean();
        final var optionalParam = Params.<Boolean>holder(null, "optional", "ifNotDefined").asBoolean();
        final var noRedefineParam = Params.<Boolean>holder(null, "fail", "noRedefine", "noRedef", "failIfDefined").asBoolean();
        final var pureParam = Params.<Boolean>holder(null, "pure").asBoolean();
        final var globalParam = Params.<Boolean>holder(null, "global").asBoolean();
        final var exportParam = Params.<Boolean>holder(null, "export").asBoolean();
        // snipline RestrictedDefineParameters filter="(.*)"
        final var IdOnly = Params.<Boolean>holder("RestrictedDefineParameters").asBoolean();
        Scan.using(processor).from(this).between("[]").keys(verbatimParam, optionalParam, noRedefineParam, pureParam, globalParam, exportParam, IdOnly).parse(input);
        BadSyntax.when(noRedefineParam.is() && optionalParam.is(), "You cannot use %s and %s", optionalParam.name(), noRedefineParam.name());
        BadSyntax.when(globalParam.is() && exportParam.is(), "You cannot use %s and %s", optionalParam.name(), noRedefineParam.name());
        skipWhiteSpaces(input);
        boolean verbatim = verbatimParam.is();
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
        final var macro = processor.newUserDefinedMacro(convertGlobal(id), input.toString(), verbatim, params);
        if (globalParam.is() || isGlobalMacro(id)) {
            processor.defineGlobal(macro);
        } else {
            processor.define(macro);
            if(exportParam.is()){
                processor.getRegister().export(macro.getId());
            }
        }
        if ((pure || pureParam.is()) && macro instanceof Configurable) {
            ((Configurable) macro).configure("pure", true);
        }
        return "";
    }
}
