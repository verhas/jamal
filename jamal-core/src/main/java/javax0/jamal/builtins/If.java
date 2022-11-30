package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.BadSyntaxAt;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;

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
public class If implements Macro {

    private static class Options {
        // snippet if_options
        final Params.Param<Boolean> empty = Params.<Boolean>holder("empty").asBoolean();
        final Params.Param<Boolean> blank = Params.<Boolean>holder("blank").asBoolean();
        final Params.Param<Boolean> not = Params.<Boolean>holder("not").asBoolean();
        final Params.Param<Boolean> and = Params.<Boolean>holder("and").asBoolean();
        final Params.Param<Boolean> or = Params.<Boolean>holder("or").asBoolean();
        final Params.Param<List<Integer>> lessThan = Params.<Integer>holder("lessThan", "less","smaller", "smallerThan").asList(Integer.class);
        final Params.Param<List<Integer>> greaterThan = Params.<Integer>holder("greaterThan", "greater", "bigger", "biggerThan", "larger", "largerThan").asList(Integer.class);
        final Params.Param<List<Integer>> equals = Params.<Integer>holder("equals", "equal", "equalsTo", "equalTo").asList(Integer.class);
        // end snippet
        private final List<Params.Param<List<Integer>>> numericOptions = List.of(lessThan, greaterThan, equals);

        /**
         * Check that the options are used in a consistent manner and the user is not using options together which
         * should not be used together.
         *
         * @throws BadSyntax if the options are used in an inconsistent way
         */
        void assertConsistency() throws BadSyntax {
            BadSyntax.when(and.is() && or.is(), "You cannot have both 'and' and 'or' options in an 'if' macro.");
            BadSyntax.when((and.is() || or.is()) && countNumOptionsPresent() < 2,"You cannot have 'and' or 'or' options without multiple numeric options in an 'if' macro.");
            BadSyntax.when(blank.is() && empty.is(), "You cannot have both 'blank' and 'empty' options in an 'if' macro.");
            BadSyntax.when((empty.is() || blank.is()) && numericOptionsPresent().stream().anyMatch(Boolean.TRUE::equals), "You cannot have 'empty' or 'blank' options in an 'if' macro with numeric options.");
        }

        Params.Param<?>[] options() {
            return new Params.Param[]{empty, blank, not, and, or, lessThan, greaterThan, equals};
        }

        List<Boolean> numericOptionsPresent() throws BadSyntax {
            List<Boolean> list = new ArrayList<>();
            for (Params.Param<List<Integer>> numericOption : numericOptions) {
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
        final var opt = new Options();
        Params.using(processor).from(this).between("[]").keys(opt.options()).parse(input);
        opt.assertConsistency();
        final var parts = InputHandler.getParts(input, 3);
        BadSyntaxAt.when(parts.length < 1,"Macro 'if' needs 1, 2 or 3 arguments",pos);

        if (opt.not.is() != isTrue(parts[0], opt)) {
            return parts.length > 1 ? parts[1] : "";
        } else {
            return parts.length > 2 ? parts[2] : "";
        }
    }

    private boolean compare(List<Integer> number, boolean and, Predicate<Integer> p) {
        if (and) {
            return number.stream().allMatch(p);
        } else {
            return number.stream().anyMatch(p);
        }
    }

    private boolean isTrue(final String test,
                           final Options opt) throws BadSyntax {
        if (opt.countNumOptionsPresent() > 0) {
            final int testN;
            try {
                testN = Integer.parseInt(test);
            } catch (NumberFormatException nfe) {
                throw new BadSyntax("When macro 'if' uses a numeric option the test has to be an integer value.");
            }
            if (opt.and.is()) {
                return (!opt.lessThan.isPresent() || compare(opt.lessThan.get(), true, n -> n > testN))
                    && (!opt.greaterThan.isPresent() || compare(opt.greaterThan.get(), true, n -> n < testN))
                    && (!opt.equals.isPresent() || compare(opt.equals.get(), true, n -> n == testN))
                    ;
            } else {
                return (opt.lessThan.isPresent() && compare(opt.lessThan.get(), false, n -> n > testN))
                    || (opt.greaterThan.isPresent() && compare(opt.greaterThan.get(), false, n -> n < testN))
                    || (opt.equals.isPresent() && compare(opt.equals.get(), false, n -> n == testN))
                    ;
            }
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
