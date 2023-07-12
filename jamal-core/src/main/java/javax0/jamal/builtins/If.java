package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.BooleanParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Define the {@code if} conditional macro. The syntax of the macro is
 *
 * <pre>
 *     {#if/test/then content/else content}
 * </pre>
 * <p>
 * The result of the evaluated macro will be the {@code then content} when the {@code test} is true and the {@code else}
 * content otherwise. The {@code test} is true, if it is the literal {@code "true"} (case insensitive), an integer
 * number and the value is not zero or any other string that contains at least one non-space character, except when the
 * {@code test} is the literal {@code "false"} (case insensitive) then the test is false.
 * <p>
 * The syntax depicted above using the {@code /} character as separator. It is only convention. Any non-space character
 * can be used as separator. The first non-space character following the {@code if} will be used as separator
 * character.
 */
public class If implements Macro, OptionsControlled.Core, Scanner.Core {

    private static class Options {
        final ScannerObject scanner;

        final BooleanParameter empty ;
        final BooleanParameter blank;
        final BooleanParameter not;
        final BooleanParameter and;
        final BooleanParameter or;
        final BooleanParameter isDefined;
        final BooleanParameter isGlobal;
        final BooleanParameter isLocal;
        final Params.Param<List<String>> lessThan;
        final Params.Param<List<String>> greaterThan;
        final Params.Param<List<String>> equals;
        private final List<Params.Param<List<String>>> numericOptions;

        private Options(ScannerObject scnr) {
            scanner = scnr;
            // snippet if_options
            empty = scanner.bool("empty");
            blank = scanner.bool("blank");
            not = scanner.bool("not");
            and = scanner.bool("and");
            or = scanner.bool("or");
            isDefined = scanner.bool("isDefined", "defined");
            isGlobal = scanner.bool("isGlobal", "global");
            isLocal = scanner.bool("isLocal", "local");
            lessThan = scanner.list("lessThan", "less", "smaller", "smallerThan");
            greaterThan = scanner.list("greaterThan", "greater", "bigger", "biggerThan", "larger", "largerThan");
            equals = scanner.list("equals", "equal", "equalsTo", "equalTo");
            // end snippet
            numericOptions = List.of(lessThan, greaterThan, equals);
        }

        /**
         * Check that the options are used in a consistent manner and the user is not using options together which
         * should not be used together.
         *
         * @throws BadSyntax if the options are used in an inconsistent way
         */
        void assertConsistency() throws BadSyntax {
            BadSyntax.when((isDefined.is() || isGlobal.is() || isLocal.is()) && (blank.is() || empty.is() || countNumOptionsPresent() > 0), "'blank' or 'empty' cannot be used together with 'isDefined', 'isLocal', or 'isGlobal' or with numeric checks");
            BadSyntax.when(and.is() && or.is(), "You cannot have both 'and' and 'or' options in an 'if' macro.");
            BadSyntax.when((and.is() || or.is()) && countNumOptionsPresent() < 2, "You cannot have 'and' or 'or' options without multiple numeric options in an 'if' macro.");
            BadSyntax.when(blank.is() && empty.is(), "You cannot have both 'blank' and 'empty' options in an 'if' macro.");
            BadSyntax.when((empty.is() || blank.is()) && numericOptionsPresent().stream().anyMatch(Boolean.TRUE::equals), "You cannot have 'empty' or 'blank' options in an 'if' macro with numeric options.");
        }

        List<Boolean> numericOptionsPresent() throws BadSyntax {
            List<Boolean> list = new ArrayList<>();
            for (Params.Param<List<String>> numericOption : numericOptions) {
                list.add(numericOption.isPresent());
            }
            return list;
        }

        long countNumOptionsPresent() throws BadSyntax {
            int counter = 0;
            for (final var param : numericOptions) {
                if (param.isPresent()) {
                    counter += param.get().size();
                }
            }
            return counter;
        }
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        final var pos = input.getPosition();
        final var scanner = newScanner(input, processor);
        final var opt = new Options(scanner);
        scanner.done();
        opt.assertConsistency();
        final var parts = InputHandler.getParts(input, 3);
        BadSyntaxAt.when(parts.length < 1, "Macro 'if' needs 1, 2 or 3 arguments", pos);

        if (opt.not.is() != isTrue(processor, parts[0], opt)) {
            return parts.length > 1 ? parts[1] : "";
        } else {
            return parts.length > 2 ? parts[2] : "";
        }
    }

    private static boolean compare(List<String> number, boolean and, Predicate<String> p) {
        if (and) {
            return number.stream().allMatch(p);
        } else {
            return number.stream().anyMatch(p);
        }
    }

    /**
     * b is less than a
     *
     * @param a the first number
     * @param b the second number
     * @return true if b is less than a
     */
    private static boolean lt(final String a, final String b) {
        try {
            return Integer.parseInt(a) > Integer.parseInt(b);
        } catch (NumberFormatException nfe) {
            return a.compareTo(b) > 0;
        }
    }

    private static boolean gt(final String a, final String b) {
        try {
            return Integer.parseInt(a) < Integer.parseInt(b);
        } catch (NumberFormatException nfe) {
            return a.compareTo(b) < 0;
        }
    }

    private static boolean eq(final String a, final String b) {
        try {
            return Integer.parseInt(a) == Integer.parseInt(b);
        } catch (NumberFormatException nfe) {
            return a.compareTo(b) == 0;
        }
    }

    private static boolean isTrue(final Processor processor,
                                  final String test,
                                  final Options opt) throws BadSyntax {
        if (opt.countNumOptionsPresent() > 0) {
            if (opt.and.is()) {
                return (!opt.lessThan.isPresent() || compare(opt.lessThan.get(), true, n -> lt(n, test)))
                        && (!opt.greaterThan.isPresent() || compare(opt.greaterThan.get(), true, n -> gt(n, test)))
                        && (!opt.equals.isPresent() || compare(opt.equals.get(), true, n -> eq(n, test)))
                        ;
            } else {
                return (opt.lessThan.isPresent() && compare(opt.lessThan.get(), false, n -> lt(n, test)))
                        || (opt.greaterThan.isPresent() && compare(opt.greaterThan.get(), false, n -> gt(n, test)))
                        || (opt.equals.isPresent() && compare(opt.equals.get(), false, n -> eq(n, test)))
                        ;
            }
        }
        if (opt.isLocal.is()) {
            return processor.getRegister().getUdMacroLocal(test).isPresent();
        } else if (opt.isGlobal.is()) {
            final String globalName;
            if (InputHandler.isGlobalMacro(test)) {
                globalName = test;
            } else {
                globalName = ":" + test;
            }
            return processor.getRegister().getUserDefined(globalName).isPresent();
        } else if (opt.isDefined.is()) {
            return processor.getRegister().getUserDefined(test).isPresent();
        }

        if (opt.blank.is()) {
            return test.trim().length() == 0;
        }
        if (opt.empty.is()) {
            return test.length() == 0;
        }
        if (test.trim().equalsIgnoreCase("true")) {
            return true;
        }
        if (test.trim().equalsIgnoreCase("false")) {
            return false;
        }
        if (test.trim().matches("[+-]?\\d+")) {
            return Integer.parseInt(test) != 0;
        }
        return test.trim().length() > 0;
    }

}
