package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.ObjectHolder;
import javax0.jamal.api.OptionsControlled;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;

import java.util.HashMap;

import static java.lang.String.format;
import static javax0.jamal.builtins.ForState.*;
import static javax0.jamal.tools.InputHandler.fetchId;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

/**
 * See the documentation of the "for" loop in the README.doc in the project root.
 */
@Macro.Stateful
public class For implements Macro, InnerScopeDependent, OptionsControlled.Core, Scanner.Core {

    enum KeyWord {
        IN, FROM
    }

    @Override
    public String evaluate(Input input, Processor processor) throws BadSyntax {
        Position pos = input.getPosition();
        final var scanner = newScanner(input, processor);
        final var it = new ForState(scanner,processor);
        scanner.done();

        skipWhiteSpaces(input);

        final String[] variables = getVariables(input);
        skipWhiteSpaces(input);
        final String[][] valueMatrix;
        switch (checkKeyword(input)) {
            case IN:
                valueMatrix = it.getValueMatrix(input, variables, pos);
                break;
            case FROM:
                final var source = fetchId(input);
                final var sourceObject = processor.getRegister().getUserDefined(source)
                        .filter(m -> m instanceof ObjectHolder<?>)
                        .map(m -> (ObjectHolder<?>) m)
                        .map(ObjectHolder::getObject)
                        .orElseThrow(() -> new BadSyntax(format("The user defined macro '%s' does not exist or cannot be used as data source for a 'for' loop.", source)));
                valueMatrix = it.getValueMatrix(sourceObject, variables.length);
                break;
            default:
                throw new IllegalArgumentException("Unknown keyword following the 'for'");
        }
        skipWhiteSpaces(input);
        checkEqualSign(input);
        final var content = input.toString();
        final var output = new StringBuilder();
        final Segment root = splitContentToSegments(variables, content);
        final var parameterMap = new HashMap<String, String>();
        var joinerString = "";
        for (final String[] values : valueMatrix) {
            if (values != null) {
                output.append(joinerString);
                if( it.join.isPresent()){
                    joinerString = it.join.get();
                }
                for (int i = 0; i < variables.length; i++) {
                    parameterMap.put(variables[i], i < values.length ? values[i] : "");
                }
                for (Segment segment = root; segment != null; segment = segment.next()) {
                    output.append(segment.content(parameterMap));
                }
            }

        }
        return output.toString();

    }


}
