package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Format;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is the implementation of the mock dynamically allocated every time the {@code mock} macro is called.
 * This macro is NOT listed in the META-INF/services directory and not in the module-info.
 */
@Macro.Stateful
public class MockImplementation implements Macro {
    private final String id;
    private final List<Response> responses = new ArrayList<>();
    private int counter = 0;
    private final Macro shadowedMacro;

    /**
     * One possible response, that contains the response itself, but also the data used to calculate the condition
     * when the response si to be used.
     */
    private static class Response {
        final String text;
        final boolean inputCheck;
        final Pattern inputPattern;
        final boolean infinite;
        int repeat;

        /**
         * Create a new response.
         *
         * @param text         the text of the response. This is not calulated, transformed. The text is a constant
         *                     returned by the macro when mocked and this response is used.
         * @param inputCheck   the input is checked against the pattern only if this parameter is {@code true}
         * @param inputPattern a regular expression pattern or {@code null}. Must not be {@code null} when {@code
         *                     inputCheck} is {@code true}.
         * @param infinite     when {@code true} the response can be used any number of times.
         * @param repeat       the number of times the response can be used. This parameter is ignored when {@code
         *                     infinite} is {@code true}.
         */
        private Response(final String text, final boolean inputCheck, final Pattern inputPattern, final boolean infinite, final int repeat) {
            this.text = text;
            this.inputCheck = inputCheck;
            this.inputPattern = inputPattern;
            this.infinite = infinite;
            this.repeat = repeat;
        }

        /**
         *
         * @param in the input of the mocked macro
         * @return {@code true} if the response can be used for this input.
         */
        boolean matches(Input in) {
            if (!inputCheck) {
                return true;
            }
            return inputPattern.matcher(in.toString()).matches();
        }

    }

    /**
     * Get the first response, which is not expired and can be used for the given input.
     *
     * @param in the input of the mocked macro.
     * @return either the response in an optional or empty if there is none.
     */
    Optional<String> getResult(Input in) {
        for (final var response : responses) {
            if (response.matches(in)) {
                if (response.infinite) {
                    return Optional.of(response.text);
                }
                if (response.repeat > 0) {
                    response.repeat--;
                    return Optional.of(response.text);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Create a new mock implementation for the given id. The original macro object is used to be invoked in case the
     * responses are expired.
     * @param id the macro identifier. It can be different from the main identifier of the shadowed macro, as macros
     *           can have aliases.
     * @param shadowedMacro the macro to be mocked. If there is no macro then it has to be {@code null}.
     */
    public MockImplementation(final String id, final Macro shadowedMacro) {
        this.id = id;
        this.shadowedMacro = shadowedMacro;
    }

    void response(final String text, final boolean inputCheck, final Pattern inputPattern, final boolean infinite, final int repeat) throws BadSyntax {
        BadSyntax.when(!responses.isEmpty() &&
                        responses.get(responses.size() - 1).infinite &&
                        !responses.get(responses.size() - 1).inputCheck,
                "You cannot add a new mock response after an infinite one.");
        responses.add(new Response(text, inputCheck, inputPattern, infinite, repeat));
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var result = getResult(in);
        if (result.isEmpty()) {
            BadSyntax.when(shadowedMacro == null, Format.msg("Mock %s has exhausted after %d uses.", id, counter));
            return shadowedMacro.evaluate(in, processor);
        }
        counter++;
        return result.get();
    }

    @Override
    public String getId() {
        return id;
    }
}
