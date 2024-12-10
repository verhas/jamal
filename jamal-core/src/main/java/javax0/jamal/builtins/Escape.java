package javax0.jamal.builtins;

import javax0.jamal.api.Macro;
import javax0.jamal.api.*;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.OptionsStore;

import static javax0.jamal.tools.InputHandler.*;

/**
 * W A R N I N G!!!!
 * <p>
 * Before changing anything here, read the following and make sure you understand the consequences!
 * <p>
 * Jamal source text can use this macro to escape macro opening and closing strings. The syntax of the macro is
 *
 * <pre>{@code
 *         {@escape `x` escaped text presumably containing
 *                        macro opening and/or closing strings
 *                        possibly multiple times `x`}
 * }</pre>
 * <p>
 * Where {@code `x`} is an arbitrary string between backtick characters. It stands at the start of the macro after the
 * keyword and also signals the end of the escaped string.
 * <p>
 * The implementation of this macro heavily depends on the implementation of the class {@code
 * javax0.jamal.engine.util.MacroBodyFetcher}. When fetching the content of a new macro, the macro body fetcher looks
 * ahead and counting the opening and closing macro strings finds the matching closing string for the macro it fetches.
 * For example, it fetches a macro at a point where the input is
 *
 * <pre>{@code
 *           {@define a=2{b}}
 * }</pre>
 * <p>
 * it will fetch "{@code @define a=2{b}}" as it is counting the { and the } characters. If there is an {@code escape}
 * macro then the counting should be paused between the {@code `x`} parts. For example
 *
 * <pre>{@code
 *           {@escape `st` } `st`}
 * }</pre>
 * <p>
 * will fetch "{@code @escape `st` }} {@code `st`}" because it skips the counting in the part between the {@code `st`}.
 * <p>
 * [OBSOLETE
 * This way, this macro is "implemented" not only here but also in the macro body fetcher.]
 * <p>
 * The macro body fetcher was split into two parts. The first part is the {@code MacroBodyFetcher} that is responsible
 * for some high-level governance of body fetching. The main functionality was moved into the method {@code fetch}.
 * This method has a default implementation in the interface {@link Macro} and also here, overriding the default.
 */
public class Escape implements Macro, Macro.Escape {
    private static final String UNESCAPE_OPTION = "4a616d616c206973206b696e67";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final boolean fullPreserve = isFullPreserve(in, processor);
        InputHandler.skipWhiteSpaces(in);
        BadSyntaxAt.when(in.charAt(0) != '`', "The macro escape needs an escape string enclosed between ` characters.", in.getPosition());
        InputHandler.skip(in, 1);
        final var endOfEscape = in.indexOf("`", -1);
        BadSyntaxAt.when(endOfEscape == -1, "The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", in.getPosition());
        final var escapeSequence = "`" + in.subSequence(0, endOfEscape) + "`";
        InputHandler.skip(in, escapeSequence.length() - 1);
        final var endOfString = in.indexOf(escapeSequence, -1);
        BadSyntaxAt.when(endOfString == -1, "I cannot find the escape string at the end of the macro: " + escapeSequence, in.getPosition());
        final var escapedString = in.substring(0, endOfString);
        InputHandler.skip(in, escapedString);
        InputHandler.skip(in, escapeSequence);
        InputHandler.skipWhiteSpaces(in);
        BadSyntaxAt.when(in.length() > 0, "There are extra characters in the use of {@escape } after the closing escape sequence: " + escapeSequence, in.getPosition());
        if (fullPreserve) {
            processor.deferredClose(new UnescapeCloser());
            return processor.getRegister().open() + "@escape*" + escapeSequence + escapedString + escapeSequence + processor.getRegister().close();
        }
        return escapedString;
    }

    /**
     * Decide if the result of the macro has to be the full macro or only the content.
     * <p>
     * The full macro is deserved when the macro is used as {@code {@escape* `x` ...}}. In this case, the macro will
     * return the full macro, but the macro closer will be invoked with an option that will make the macro closer
     * return only the content.
     * <p>
     * The macro will return only the content when the macro is used as {@code {@escape `x` ...}}. In this case, the
     * macro closer will not be invoked.
     * <p>
     * The macro closer also calls the macro processor to process the content. To ignore the {@code *} option in this
     * special case, the macro processor is invoked with the option {@code UNESCAPE_OPTION}.
     * <p>
     * The option {@code UNESCAPE_OPTION} is just a random name, not likely to be set by the user. It is used only to
     * signal the macro processor that the macro closer is invoked from the {@code escape} macro.
     * <p>
     * In this case this method returns {@code false}.
     *
     * @param in        the input to check for the {@code *} at the first character position. If there is a {@code *} then it
     *                  is also skipped.
     * @param processor the processor used to get the option store.
     * @return {@code true} if the macro has to return the full macro, {@code false} if only the content.
     */
    private static boolean isFullPreserve(Input in, Processor processor) {
        final boolean fullPreserve;
        if (in.charAt(0) == '*') {
            InputHandler.skip(in, 1);
            fullPreserve = !OptionsStore.getInstance(processor).is(UNESCAPE_OPTION);
        } else {
            fullPreserve = false;
        }
        return fullPreserve;
    }


    /**
     * Move the 'escape' macro body to the output.
     * <p>
     * The method is invoked only when {@code getEscapeMacro(Processor, Input, int)} returned  {@code true}. It assumes
     * that the start of the input contains an escape macro.
     * <p>
     * Note that this method will not care if there are extra non-whitespace character between the second {@code `xxx`}
     * string and the macro closing string, like in
     *
     * <pre>{@code
     *     {@escape `|||`escaped content `|||` this is erroneous}
     * }</pre>
     * <p>
     * the characters "{@code this is erroneous}". This will however be an error when the macro as implemented in the
     * module {@code jamal.core} is executed.
     *
     * @param processor the Jamal processor
     * @param input     has to point to the start of the macro. After the invocation the input will be stepped onto the
     *                  closing macro string and not after.
     * @return the body of the macro
     * @throws BadSyntaxAt if the syntax of the escape macro is violated.
     */
    @Override
    public String fetch(Processor processor, Input input) throws BadSyntaxAt {
        final var output = javax0.jamal.tools.Input.makeInput();
        final var start = input.getPosition();
        moveWhiteSpaces(input, output);
        move(input, 1, output); // the # or @ character
        moveWhiteSpaces(input, output);
        output.append(fetchId(input));
        if (input.charAt(0) == '*') {
            output.append('*');
            skip(input, 1);
        }
        moveWhiteSpaces(input, output);
        BadSyntaxAt.when(input.charAt(0) != '`', "The macro escape needs an escape string enclosed between ` characters.", input.getPosition());
        move(input, 1, output);
        final var endOfEscape = input.indexOf("`", -1);
        BadSyntaxAt.when(endOfEscape == -1, "The macro escape needs an escape string enclosed between ` characters. Closing ` is not found.", input.getPosition());
        final var escapeSequence = "`" + input.subSequence(0, endOfEscape) + "`";
        move(input, escapeSequence.length() - 1, output);
        final var endOfString = input.indexOf(escapeSequence, -1);
        BadSyntaxAt.when(endOfString == -1, "I cannot find the escape string at the end of the macro: " + escapeSequence, input.getPosition());
        move(input, endOfString, output);
        move(input, escapeSequence.length(), output);
        final var closeStr = processor.getRegister().close();
        final var endOfEscapeMacro = input.indexOf(closeStr, -1);
        BadSyntaxAt.when(endOfEscapeMacro == -1, "Escape macro is not closed", start);
        move(input, endOfEscapeMacro, output);
        skip(input, closeStr);
        Macro.findFirstSignificantCharacter(input);
        return output.toString();
    }

    /**
     * This closer will recursively invoke the {@link Processor#process(Input)} method after setting an option driving
     * the {@code escape*} macros to return only the content and not the whole macro.
     * <p>
     * To have the correct result such a closer should only run once, even of there are many {@code escape*} macros in
     * the Jamal source. For this reason the {@link Object#equals(Object)} and {@link Object#hashCode()} are implemented
     * so that there will only be one instance of this closer registered in a processor.
     */
    private static class UnescapeCloser implements Closer.OutputAware, Closer.ProcessorAware, AutoCloseable {
        private Processor processor;
        private Input output;

        @Override
        public boolean equals(Object o) {
            return UnescapeCloser.class == o.getClass();
        }

        @Override
        public int hashCode() {
            return UnescapeCloser.class.hashCode();
        }

        @Override
        public void set(Processor processor) {
            this.processor = processor;
        }

        @Override
        public void set(Input output) {
            this.output = output;
        }


        @Override
        public void close() throws Exception {
            OptionsStore.getInstance(processor).addOptions(UNESCAPE_OPTION);
            final String result = processor.process(output);
            OptionsStore.getInstance(processor).addOptions("~" + UNESCAPE_OPTION);
            if (!processor.errors().isEmpty()) {
                processor.throwUp();
            }
            output.replace(result);
        }
    }
}
/*template jm_escape
{template |escape|escape$S$ `$P$`$C$`$P$`|escape the evaluation of the content|
  {variable |S|enum("*","")}
  {variable |P|"..."}
  {variable |C|"..."}
}
 */