package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Identified;
import javax0.jamal.api.Input;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.api.UserDefinedMacro;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static javax0.jamal.tools.InputHandler.convertGlobal;
import static javax0.jamal.tools.InputHandler.isGlobalMacro;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Macro implements javax0.jamal.api.Macro, OptionsControlled.Core, Scanner.Core {

    public static final String USERDEFINED = "userdefined";

    @Override
    public String evaluate(final Input input, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(input, processor);
        final var type = scanner.str(null, "type").defaultValue(USERDEFINED);
        final var alias = scanner.str(null, "alias").defaultValue("");
        final var global = scanner.bool(null, "global");
        scanner.done();
        skipWhiteSpaces(input);

        final var macroType = type.get().toLowerCase();
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

    private String getBuitIn(final Input input, final Processor processor, final boolean global, final Params.Param<String> alias) throws BadSyntax {
        final var macro = getMacro(input, global, javax0.jamal.api.Macro.class, processor.getRegister()::getMacro);
        if (alias.isPresent()) {
            return aliasMacro(processor, alias, macro);
        }
        BadSyntax.when(macro == null, "Unknown built-in macro{@%s}", input);
        return macro.evaluate(EMPTY_INPUT, processor);
    }

    private String getUserDefined(final Input input, final Processor processor, final boolean global, final Params.Param<String> alias) throws BadSyntax {
        final var macro = getMacro(input, global, UserDefinedMacro.class,
                id -> processor.getRegister().getUserDefined(id, Identified.DEFAULT_MACRO));
        if (alias.isPresent()) {
            return aliasMacro(processor, alias, (Identified) macro);
        } else {
            BadSyntax.when(macro == null, "Unknown user-defined macro {%s}", input);
            return macro.evaluate();
        }
    }

    private <T extends Identified> String aliasMacro(final Processor processor, final Params.Param<String> alias, final T macro) throws BadSyntax {
        boolean export = isExportable(alias);
        final String name = calculateAlias(export, processor, alias);
        if (macro == null) {
            // just return the name, which is undefined and the use will throw an exception or call the defaulr macro
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

    private boolean isExportable(final Params.Param<String> alias) throws BadSyntax {
        return alias.get().length() > 0 && !alias.get().equals("true");
    }

    private String calculateAlias(final boolean export, final Processor processor, final Params.Param<String> alias) throws BadSyntax {
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
