package javax0.jamal.builtins;

import javax0.jamal.api.*;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.StringParameter;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static javax0.jamal.tools.InputHandler.*;

public class Macro implements javax0.jamal.api.Macro, OptionsControlled.Core, Scanner.Core {

    public static final String USERDEFINED = "userdefined";

    private enum MacroType {
        userdefined, builtin
    }

    @Override
    public String evaluate(final Input input, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        final var type = scanner.str(null, "type").defaultValue(USERDEFINED);// kept here for backward compatibility
        final var alias = scanner.str(null, "alias").defaultValue("");
        final var global = scanner.bool(null, "global");
        final var btype = scanner.enumeration(MacroType.class).defaultValue(MacroType.userdefined);
        scanner.done();
        BadSyntax.when(btype.isPresent() && type.isPresent(), "You cannot specify both 'type' and 'builtin' or 'userdefined' in the same macro definition");

        if (type.isPresent()) {
            processor.logger().log(System.Logger.Level.WARNING, input.getPosition(), "The 'type' parameter of the macro is deprecated. Use 'builtin' or 'userdefined' instead.");
        }

        skipWhiteSpaces(input);
        final var macroType = btype.isPresent() ? btype.get(MacroType.class).name() : type.get().toLowerCase();
        switch (macroType) {
            case USERDEFINED:
            case "user defined":
            case "user-defined":
                return getUserDefined(input, processor, global.is(), alias);
            case "builtin":
            case "built in":
            case "built-in":
                return getBuitIn(input, processor, global.is(), alias);
            default:
                throw new BadSyntax("Unknown macro type: " + macroType);
        }
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    private static final Input EMPTY_INPUT = javax0.jamal.tools.Input.makeInput();

    private String getBuitIn(final Input input, final Processor processor, final boolean global, final StringParameter alias) throws BadSyntax {
        final var macro = getMacro(input, global, javax0.jamal.api.Macro.class, processor.getRegister()::getMacro);
        if (alias.isPresent()) {
            return aliasMacro(processor, alias, macro);
        }
        BadSyntax.when(macro == null, "Unknown built-in macro{@%s}", input);
        return macro.evaluate(EMPTY_INPUT, processor);
    }

    private String getUserDefined(final Input input, final Processor processor, final boolean global, final StringParameter alias) throws BadSyntax {
        final var macro = getMacro(input, global, UserDefinedMacro.class,
                id -> processor.getRegister().getUserDefined(id, Identified.DEFAULT_MACRO));
        if (alias.isPresent()) {
            return aliasMacro(processor, alias, (Identified) macro);
        } else {
            BadSyntax.when(macro == null, "Unknown user-defined macro {%s}", input);
            return macro.evaluate();
        }
    }

    private <T extends Identified> String aliasMacro(final Processor processor, final StringParameter alias, final T macro) throws BadSyntax {
        boolean export = isExportable(alias);
        final String name = calculateAlias(export, processor, alias);
        if (macro == null) {
            // just return the name, which is undefined, and the use will throw an exception or call the default macro
            return name;
        } else if (macro instanceof javax0.jamal.api.Macro) {
            if (isGlobalMacro(name)) {
                processor.getRegister().global((javax0.jamal.api.Macro) macro, convertGlobal(name));
                export = false;
            } else {
                processor.getRegister().define((javax0.jamal.api.Macro) macro, name);
            }
        } else {
            if (isGlobalMacro(name)) {
                processor.getRegister().global(macro, convertGlobal(name));
                export = false;
            } else {
                processor.getRegister().define(macro, name);
            }
        }
        if (export) {
            processor.getRegister().export(name);
        }
        return name;
    }

    private String getMacroName(final boolean global, final Input input) {
        final String macroName;
        if (global && !isGlobalMacro(input.toString())) {
            macroName = ":" + input;
        } else {
            macroName = input.toString();
        }
        return macroName;
    }

    private <T> T getMacro(final Input input, final boolean global, final Class<T> klass, final Function<String, Optional<T>> get) {
        final String macroName = getMacroName(global, input);
        return get.apply(macroName)
                .filter(klass::isInstance)
                .map(klass::cast)
                .orElse(null);
    }

    private boolean isExportable(final StringParameter alias) throws BadSyntax {
        return !alias.get().isEmpty() && !alias.get().equals("true");
    }

    private String calculateAlias(final boolean export, final Processor processor, final StringParameter alias) throws BadSyntax {
        final String name;
        if (!export) {
            String s;
            do {
                s = "_" + counter.incrementAndGet();
            } while (processor.getRegister().getUserDefined(s).isPresent());
            name = s;
        } else {
            name = alias.get();
        }
        return name;
    }
}
/*template jm_macro
{template |macro|macro [type=$T$ $O$] $C$|reference a macro with irregular name|
  {variable :T:enum("","userdefined","builtin")}
  {variable :O:"alias="" global"}
  {variable |C|"macro-name"}
}
 */