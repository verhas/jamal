package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Params;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final Set<String> knownActions = Set.of("kill", "skip", "replace", "trim", "reflow", "number", "untab", "range");
    private static final Map<String, String> actionAliases = Map.of(
            "ranges", "range"
    );

    /**
     * Parse the input for parameters and store the parameters. The input is parsed in the constructor and then the
     * parameter set is checked for consistency as well as implicit actions are added.
     */
    private class Parameters {
        // parameter that lists the actions in the order they should be performed. The list is comma delimited.
        final Params.Param<String> actions = Params.holder(null, "action", "actions").defaultValue("").asString();
        // parameters of the killLines macro
        final Params.Param<Pattern> pattern = Params.holder(null, "kill", "pattern").defaultValue("^\\s*$").asPattern();
        final Params.Param<Boolean> keep = Params.<Boolean>holder(null, "keep").asBoolean();
        // parameters for the number lines macro
        final Params.Param<String> format = Params.holder(null, "format").defaultValue("%d. ").asString();
        final Params.Param<Integer> start = Params.holder(null, "start").defaultValue(1);
        final Params.Param<Integer> step = Params.holder(null, "step").defaultValue(1);
        // parameters for reflow
        final Params.Param<Integer> width = Params.holder(null, "width").defaultValue(0);
        // parameters for replaceLines
        final Params.Param<List<String>> replace = Params.holder(null, "replace").asList(String.class);
        final Params.Param<Boolean> detectNoChange = Params.holder(null, "detectNoChange").asBoolean();
        // parameters for skipping lines
        final Params.Param<Pattern> skipStart = Params.<Pattern>holder(null, "skip").defaultValue("skip").asPattern();
        final Params.Param<Pattern> skipEnd = Params.<Pattern>holder(null, "endSkip").defaultValue("end\\s+skip").asPattern();
        // parameters for trim
        final Params.Param<Integer> margin = Params.<Integer>holder(null, "margin", "trim").defaultValue(0);
        final Params.Param<Boolean> trimVertical = Params.<Boolean>holder(null, "trimVertical").asBoolean();
        final Params.Param<Boolean> verticalTrimOnly = Params.<Boolean>holder(null, "verticalTrimOnly", "vtrimOnly").asBoolean();
        final Params.Param<Integer> tabSize = Params.<Integer>holder("tabSize", "tab").asInt();
        // parameter for range
        final Params.Param<String> ranges = Params.holder(null, "range", "ranges", "lines").asString();
        final Set<String> actionsSet;

        private void parse(final Params.ExtraParams extraParams, final Input in, final Processor processor) throws BadSyntax {
            if (extraParams == null) {
                // there are no '(' and ')' around the parameters
                Params.using(processor)
                        .from(SnipTransform.this)
                        .keys(actions, pattern, keep, format, start, step, width, replace, detectNoChange,
                                skipStart, skipEnd, margin, trimVertical, verticalTrimOnly, tabSize, ranges)
                        .parse(in);
            } else {
                Params.using(processor)
                        .from(SnipTransform.this)
                        .keys(actions, pattern, keep, format, start, step, width, replace, detectNoChange,
                                skipStart, skipEnd, margin, trimVertical, verticalTrimOnly, tabSize, ranges)
                        .parse(extraParams);
            }

        }

        Parameters(final Params.ExtraParams extraParams, final Input in, final Processor processor) throws BadSyntax {
            parse(extraParams, in, processor);

            actionsSet = getOrderedActionSet();

            addImplicitConfiguredActions();
            checkMissingActions();

            for (final String action : actionsSet) {
                BadSyntax.when(!knownActions.contains(action), "Unknown action '%s'", action);
            }
        }

        private Set<String> getOrderedActionSet() throws BadSyntax {
            final List<String> actionsList;
            if (actions.isPresent()) {
                actionsList = Arrays.stream(actions.get().split(",", -1)).map(String::trim).collect(Collectors.toCollection(ArrayList::new));
            } else {
                actionsList = new ArrayList<>();
            }
            unaliasActions(actionsList);
            final var actionsSet = new LinkedHashSet<>(actionsList);
            BadSyntax.when(actionsSet.size() != actionsList.size(), "Duplicate action(s) in %s", actions.get());
            return actionsSet;
        }

        /**
         * Replace the elements in the action list if they are aliases with the real action.
         *
         * @param actionsList the list of actions optionally containing aliases
         */
        private void unaliasActions(final List<String> actionsList) {
            for (final var alias : actionAliases.keySet()) {
                final var aliasIndex = actionsList.indexOf(alias);
                if (aliasIndex != -1) {
                    actionsList.set(aliasIndex, actionAliases.get(alias));
                }
            }
        }

        /**
         * The elements of the table that contains the list of the actions added automatically to the list of explicit
         * actions when a parameter for the missing action is used.
         */
        private class TableImplicits {
            /**
             * The name of the parameter that belongs to an action. It is usually the same as the name of the action,
             * with one exception (as for 1.11.0 release, may change later).
             */
            final String name;
            /**
             * The name of the action.
             */
            final String action;
            /**
             * The parameter that configures the action.
             */
            final Params.Param<?> param;

            TableImplicits(final String name, final String action, final Params.Param<?> param) {
                this.name = name;
                this.action = action;
                this.param = param;
            }
        }

        /**
         * Create an action with the parameter and the name of the action is the same as the name of the parameter.
         *
         * @param name  the name of the parameter and the action
         * @param param the parameter used to configure the action
         * @return the table row
         */
        TableImplicits action(final String name, final Params.Param<?> param) {
            return new TableImplicits(name, name, param);
        }

        /**
         * Create a table row with the name of the action and the parameter that configures the action.
         *
         * @param name   the name of the parameter
         * @param param  the parameter
         * @param action the name of the action
         * @return the table row
         */
        TableImplicits action(final String name, final Params.Param<?> param, final String action) {
            return new TableImplicits(name, action, param);
        }

        TableImplicits[] implicitActions(final TableImplicits... ts) {
            return ts;
        }

        /**
         * Add the actions that may not need to be listed in the {@code actions} parameter. When the name of a parameter
         * is the same as the name of an action, the action is added to the set of actions. The action is also
         * automatically added when the name of the parameter reasonably suggests what to do. For example, when the
         * parameter is {@code keep} the action {@code kill} is added.
         * <p>
         * The code also checks in some cases (in one case to be precise, at the moment) that the name of the parameter
         * is the one from the many alternatives that makes sense. The example is the parameter {@code kill} which is an
         * alias for {@code pattern}. You can use {@code kill} as a parameter and then the action {@code kill} will be
         * added, but if you use the alias {@code pattern} the action {@code kill} will not be added automatically. This
         * is for readability reason.
         *
         * @throws BadSyntax when there is some problem with the parameter handling
         */
        private void addImplicitConfiguredActions() throws BadSyntax {
            for (final var a : implicitActions(
                    // THIS IS THE DEFAULT ORDER OF THE ACTIONS
                    action("kill", pattern),
                    action("keep", keep, "kill"),
                    action("skip", skipStart),
                    action("range", ranges),
                    action("ranges", ranges, "range"),
                    action("lines", ranges, "range"),
                    action("replace", replace),
                    action("tab", tabSize, "untab"),
                    action("tabSize", tabSize, "untab"),
                    action("trim", margin, "trim")
            )) {
                if (a.param.isPresent() && a.param.name().equals(a.name)) {
                    actionsSet.add(a.action);
                }
            }
            if (actionsSet.contains("ranges")) {
                actionsSet.add("range");
            }
        }

        /**
         * Check that there is no parameter for a missing, non-listed action. This method is invoked after the actions
         * implicit configuration is done.
         *
         * @throws BadSyntax when there is a parameter for a missing action
         */
        private void checkMissingActions() throws BadSyntax {
            final var needs = Map.of(
                    "kill", List.of(pattern, keep),
                    "skip", List.of(skipEnd),
                    "replace", List.of(detectNoChange),
                    "trim", List.of(margin, trimVertical, verticalTrimOnly, tabSize),
                    "reflow", List.of(width),
                    "untab", List.of(tabSize),
                    "number", List.of(format, start, step),
                    "range", List.of(ranges));
            for (final var e : needs.entrySet()) {
                final var action = e.getKey();
                if (!actionsSet.contains(action)) {
                    for (final var param : e.getValue()) {
                        BadSyntax.when(param.isPresent(), () -> String.format("'%s' can be used only when '%s' specified as action or parameter.",
                                param.name(), action));
                    }
                }
            }
        }
    }

    /**
     * Collect and store the macros that implement the different actions.
     * <p>
     * These macros are located by their identifier. The usual situation is that they are the macros that are defined
     * in the snippet library (this one, actually) and the macros are global. Jamal, however, can redefine globally and
     * locally the macros. If these macro names lead to a different macro, then they should conform to the original
     * macro of the same name. They have to implement the {@link BlockConverter} interface, and they should work with
     * the same parameters.
     * <p>
     * If some macro does not implement the interface, then the {@link #getConverter(Processor, String) getConverter()}
     * method will throw an exception. If the parameters are different, then the functionality may not be correct. There
     * is no technical way to check that with the current implementation.
     */
    private static class UnderlyingMacros {

        final BlockConverter killLines;
        final BlockConverter skipLines;
        final BlockConverter replaceLines;
        final BlockConverter trimLines;
        final BlockConverter reflow;
        final BlockConverter numberLines;
        final BlockConverter untab;
        final BlockConverter range;

        private static BlockConverter getConverter(final Processor processor, final String macroName) throws BadSyntax {
            return processor.getRegister().getMacro(macroName)
                    .filter(m -> m instanceof BlockConverter).map(m -> (BlockConverter) m)
                    .orElseThrow(() -> new BadSyntax("The macro '" + macroName + "' is not registered"));
        }

        UnderlyingMacros(final Processor processor) throws BadSyntax {
            killLines = getConverter(processor, "killLines");
            skipLines = getConverter(processor, "skipLines");
            replaceLines = getConverter(processor, "replaceLines");
            trimLines = getConverter(processor, "trimLines");
            reflow = getConverter(processor, "reflow");
            numberLines = getConverter(processor, "numberLines");
            untab = getConverter(processor, "untab");
            range = getConverter(processor, "range");
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        return evaluate(null, in, processor);
    }

    public String evaluate(final Params.ExtraParams extraParams, final Input in, final Processor processor) throws BadSyntax {
        final var params = new Parameters(extraParams, in, processor);
        final var macros = new UnderlyingMacros(processor);
        final var sb = in.getSB();
        final var pos = in.getPosition();
        for (final String action : params.actionsSet) {
            switch (action) {
                case "kill":
                    macros.killLines.convertTextBlock(processor, sb, pos, params.pattern, params.keep);
                    break;
                case "skip":
                    macros.skipLines.convertTextBlock(processor, sb, pos, params.skipStart, params.skipEnd);
                    break;
                case "replace":
                    macros.replaceLines.convertTextBlock(processor, sb, pos, params.replace, params.detectNoChange);
                    break;
                case "trim":
                    macros.trimLines.convertTextBlock(processor, sb, pos, params.margin, params.trimVertical, params.verticalTrimOnly);
                    break;
                case "reflow":
                    macros.reflow.convertTextBlock(processor, sb, pos, params.width);
                    break;
                case "number":
                    macros.numberLines.convertTextBlock(processor, sb, pos, params.format, params.start, params.step);
                    break;
                case "untab":
                    macros.untab.convertTextBlock(processor, sb, pos, params.tabSize);
                    break;
                case "range":
                    macros.range.convertTextBlock(processor, sb, pos, params.ranges);
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
