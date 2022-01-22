package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This macro composes several other macros and invokes them using the parameters given.
 * It does not deliver any new functionality that could not be done without it.
 * It simply gives an alternative way to invoke these other macros.
 * <p>
 * The macros it can invoke are:
 *
 * <ul>
 *     <li>{@link KillLines}</li>
 *     <li>{@link SkipLines}</li>
 *     <li>{@link ReplaceLines}</li>
 *     <li>{@link TrimLines}</li>
 *     <li>{@link Reflow}</li>
 *     <li>{@link NumberLines}</li>
 * </ul>
 * <p>
 * These macros all implement the interface {@link BlockConverter}. The method defined there is invoked directly
 * after the parameter handling was finished. That way the actual algorithm is not reimplemented, but it is used
 * directly from the implementation of the macro itself.
 * <p>
 * The macro can invoke the block formatting of the underlying macros only once. It is not possible, for example, to do
 * a trim, then a reflow and then a trim again. The underlying macros will be invoked in the order as they are listed in
 * the parameter {@code actions}. (You can also use the singular form {@code action} when it is more readable.)
 * <p>
 * When a certain macro is not listed in the parameter {@code actions} but any of its configuration parameters are then
 * it will automatically be added to the set of actions. In such a case, the listed macros are invoked first in the
 * order of the listing and after that all other macros that were configured in the order as they are listed above.
 */
public class SnipTransform implements Macro {
    private static final Set<String> knownActions = Set.of("kill", "skip", "replace", "trim", "reflow", "number");

    private class Parameters {
        // parameter that lists the actions in the order they should be performed. The list is comma delimited.
        final Params.Param<String> actions = Params.holder(null, "action", "actions").orElse("").asString();
        // parameters of the killLines macro
        final Params.Param<Pattern> pattern = Params.holder(null, "kill", "pattern").orElse("^\\s*$").asPattern();
        final Params.Param<Boolean> keep = Params.<Boolean>holder(null, "keep").asBoolean();
        // parameters for the number lines macro
        final Params.Param<String> format = Params.holder(null, "format").orElse("%d. ").asString();
        final Params.Param<Integer> start = Params.holder(null, "start").orElseInt(1);
        final Params.Param<Integer> step = Params.holder(null, "step").orElseInt(1);
        // parameters for reflow
        final Params.Param<Integer> width = Params.holder(null, "width").orElseInt(0);
        // parameters for replaceLines
        final Params.Param<List<String>> replace = Params.holder(null, "replace").asList(String.class);
        final Params.Param<Boolean> detectNoChange = Params.holder(null, "detectNoChange").asBoolean();
        // parameters for skipping lines
        final Params.Param<Pattern> skipStart = Params.<Pattern>holder(null, "skip").orElse("skip").asPattern();
        final Params.Param<Pattern> skipEnd = Params.<Pattern>holder(null, "endSkip").orElse("end\\s+skip").asPattern();
        // parameters for trim
        final Params.Param<Integer> margin = Params.<Integer>holder(null, "margin").orElseInt(0);
        final Params.Param<Boolean> trimVertical = Params.<Boolean>holder(null, "trimVertical").asBoolean();
        final Params.Param<Boolean> verticalTrimOnly = Params.<Boolean>holder(null, "verticalTrimOnly", "vtrimOnly").asBoolean();

        final Set<String> actionsSet;

        Parameters(final Input in, final Processor processor) throws BadSyntax {
            // there is no '(' and ')' around the parameters
            Params.using(processor)
                    .from(SnipTransform.this)
                    .keys(actions, pattern, keep, format, start, step, width, replace, detectNoChange, skipStart, skipEnd, margin, trimVertical, verticalTrimOnly)
                    .parse(in);

            actionsSet = getOrderedActionSet();

            addImplicitConfiguredActions();

            for (final String action : actionsSet) {
                if (!knownActions.contains(action)) {
                    throw new BadSyntax("Unknown action '" + action + "'");
                }
            }
        }

        private Set<String> getOrderedActionSet() throws BadSyntax {
            final var actionsList = Arrays.asList(actions.get().split(","));
            final var actionsSet = new LinkedHashSet<>(actionsList);
            if (actionsSet.size() != actionsList.size()) {
                throw new BadSyntax("Duplicate action(s) in " + actions.get());
            }
            return actionsSet;
        }

        private void addImplicitConfiguredActions() throws BadSyntax {
            if (pattern.isPresent() || keep.isPresent()) {
                actionsSet.add("kill");
            }
            if (skipStart.isPresent() || skipEnd.isPresent()) {
                actionsSet.add("skip");
            }
            if (replace.isPresent() || detectNoChange.isPresent()) {
                actionsSet.add("replace");
            }
            if (margin.isPresent() || trimVertical.isPresent() || verticalTrimOnly.isPresent()) {
                actionsSet.add("trim");
            }
            if (width.isPresent()) {
                actionsSet.add("reflow");
            }
            if (start.isPresent() || step.isPresent() || format.isPresent()) {
                actionsSet.add("number");
            }
        }
    }

    private static class UnderlyingMacros {

        final BlockConverter killLines;

        final BlockConverter numberLines;
        final BlockConverter reflow;
        final BlockConverter replaceLines;
        final BlockConverter skipLines;
        final BlockConverter trimLines;

        private static BlockConverter getConverter(final Processor processor, final String macroName) throws BadSyntax {
            return processor.getRegister().getMacro(macroName)
                    .filter(m -> m instanceof BlockConverter).map(m -> (BlockConverter) m)
                    .orElseThrow(() -> new BadSyntax("The macro '" + macroName + "' is not registered"));
        }

        UnderlyingMacros(final Processor processor) throws BadSyntax {
            killLines = getConverter(processor, "killLines");
            numberLines = getConverter(processor, "numberLines");
            reflow = getConverter(processor, "reflow");
            replaceLines = getConverter(processor, "replaceLines");
            skipLines = getConverter(processor, "skipLines");
            trimLines = getConverter(processor, "trimLines");
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var params = new Parameters(in, processor);
        final var macros = new UnderlyingMacros(processor);
        final var sb = in.getSB();
        final var pos = in.getPosition();
        for (final String action : params.actionsSet) {
            switch (action) {
                case "kill":
                    macros.killLines.convertTextBlock(sb, pos, params.pattern, params.keep);
                    break;
                case "skip":
                    macros.skipLines.convertTextBlock(sb, pos, params.skipStart, params.skipEnd);
                    break;
                case "replace":
                    macros.replaceLines.convertTextBlock(sb, pos, params.replace,params.detectNoChange);
                    break;
                case "trim":
                    macros.trimLines.convertTextBlock(sb, pos, params.margin, params.trimVertical, params.verticalTrimOnly);
                    break;
                case "reflow":
                    macros.reflow.convertTextBlock(sb, pos, params.width);
                    break;
                case "number":
                    macros.numberLines.convertTextBlock(sb, pos, params.format, params.start, params.step);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown action: " + action + "This is an internal error, as illegal actions was already checked");
            }
        }
        return sb.toString();
    }

    @Override
    public String getId() {
        return "snip:transform";
    }
}
