package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;

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

    private class Response {
        final String text;
        final boolean inputCheck;
        final Pattern inputPattern;
        final boolean infinite;
        int repeat;

        private Response(final String text, final boolean inputCheck, final Pattern inputPattern, final boolean infinite, final int repeat) {
            this.text = text;
            this.inputCheck = inputCheck;
            this.inputPattern = inputPattern;
            this.infinite = infinite;
            this.repeat = repeat;
        }

        boolean matches(Input in) {
            if (!inputCheck) {
                return true;
            }
            return inputPattern.matcher(in.toString()).matches();
        }

    }

    Optional<String> get(Input in) {
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

    public MockImplementation(final String id) {
        this.id = id;
    }

    void response(final String text, final boolean inputCheck, final Pattern inputPattern, final boolean infinite, final int repeat) throws BadSyntax {
        if (!responses.isEmpty() && responses.get(responses.size() - 1).infinite && !responses.get(responses.size() - 1).inputCheck) {
            throw new BadSyntax("You cannot add a new mock response after an infinite one.");
        }
        responses.add(new Response(text, inputCheck, inputPattern, infinite, repeat));
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var result = get(in);
        if (result.isEmpty()) {
            throw new BadSyntax(String.format("Mock %s has exhausted after %d uses.", id, counter));
        }
        counter++;
        return result.get();
    }

    @Override
    public String getId() {
        return id;
    }
}
