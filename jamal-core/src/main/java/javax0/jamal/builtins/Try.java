package javax0.jamal.builtins;

import javax0.jamal.api.*;
import javax0.jamal.api.Macro;
import javax0.jamal.tools.Option;

import java.util.Objects;

import static javax0.jamal.api.SpecialCharacters.QUERY;
import static javax0.jamal.api.SpecialCharacters.REPORT_ERRMES;
import static javax0.jamal.tools.InputHandler.*;

public class Try implements Macro {

    // snipline tryoption filter="(.*)"
    public static final String CAUGHT_ERROR_OPTION = "try$caught$error";

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final boolean query = firstCharIs(in, QUERY);
        final boolean report = firstCharIs(in, REPORT_ERRMES);
        if (query || report) {
            skip(in, 1);
        }
        skipWhiteSpaces(in);
        final var markerStart = processor.getRegister().test();
        final int err = processor.errors().size();
        String retval;
        final var caught = new Option(CAUGHT_ERROR_OPTION);
        processor.define(caught);
        final boolean oldDeferring = processor.setDeferring(false);
        try {
            retval = processInput(in, processor, query, err);
            caught.set(false);
        } catch (BadSyntax bs) {
            retval = handleBadSyntax(processor, query, report, markerStart, err, bs);
            caught.set(true);
        }finally {
            processor.setDeferring(oldDeferring);
        }
        return retval;
    }

    /**
     * Handle the situation when a {@link BadSyntax} exception is thrown.
     * When this exception is thrown there may be suppressed exceptions in the error stack.
     * There are three different possibilities regarding the suppressed exceptions:
     *
     * <ul>
     *     <li>During the processing some exceptions were suppressed and at the end an exception was thrown.
     *     In this case the thrown exception is ignored as well as all the exceptions, which were created following the
     *     first one. The first one is fetched and used to create the return value.
     *     </li>
     *     <li>During the processing some exceptions were suppressed and at the end no exception was thrown.
     *     In this case the method {@link #processInput(Input, Processor, boolean, int) processInput()} pops the
     *     suppressed exceptions and returns the value for {@code try}. In this case this method is not invoked.
     *     </li>
     *     <li>During the processing no exceptions were suppressed and at the end an exception was thrown.
     *     In this case this exception is used and the call to clean the suppressed exceptions does not do anything.
     *     </li>
     * </ul>
     * <p>
     * The return value is {@code "false"}, the short message of the exception or empty string based on the modifier
     * character used following the {@code try} macro name.
     *
     * @param processor   the processor used to process the input
     * @param query       used to calculate the output. If {@code true} the output is {@code "false"}, otherwise the output
     *                    is controlled by the argument {@code report}.
     * @param report      used to calculate the output. If {@code true} the output is the error message, otherwise an
     *                    empty string is returned.
     * @param markerStart the start marker of the {@code try} macro
     * @param err         the number of errors before the {@code try} macro was invoked
     * @param bs          the exception thrown during the processing
     * @return the output, which is either {@code "false"}, the error message or an empty string
     * @throws BadSyntax if some other exception is thrown during processing
     */
    private static String handleBadSyntax(Processor processor, boolean query, boolean report, Marker markerStart, int err, BadSyntax bs) throws
        javax0.jamal.api.BadSyntax {
        bs = getFirstError(processor, err, bs);
        cleanUpTheMarkerStack(processor, markerStart);
        if (query) {
            return "false";
        }
        if (report) {
            return bs.getShortMessage();
        } else {
            return "";
        }
    }

    /**
     * Process the input and return the result or convert it to the string "{@code true}".
     * <p>
     * If an error occurs the exception {@link BadSyntax} is thrown. There are two different cases.
     *
     * <ul>
     *     <li>The evaluation throws an exception. In this case the exception is not caught.</li>
     *     <li>The evaluation suppresses exceptions but does not throw. In that case the exceptions are in a stack
     *     in the processor. In this case the code cleans the exception stack up to, and including the first exception
     *     that occurred during the evaluation of the macro {@code try} and throws the first exception.</li>
     * </ul>
     *
     * @param in        the input
     * @param processor the processor to use
     * @param query     if true then return "{@code true}"
     * @param err       the number of suppressed errors in the error stack at the start
     * @return either the result of the macro evaluation or the string "{@code true}" if the {@code query} is true
     * @throws BadSyntax when the processing throws {@link BadSyntax}
     */
    private static String processInput(Input in, Processor processor, boolean query, int err) throws BadSyntax {
        final var result = processor.process(in);
        BadSyntax bs = getFirstError(processor, err, null);
        if (bs != null) {
            throw bs;
        }
        if (query) {
            return "true";
        } else {
            return result;
        }
    }

    /**
     * During the evaluation of the macros in the {@code try} block, errors can happen.
     * Some errors are recoverable in the sense that Jamal can continue working in order to discover more possible
     * errors. These errors are suppressed and are collected in the processor.
     * <p>
     * The stack storing the suppressed errors may have had errors already when the evaluation of the macro {@code try}
     * starts. The number of errors in the stack at the start is the {@code err} parameter. This value was saved before
     * starting the evaluation.
     * <p>
     * This method removes the errors that happened during the evaluation of the macro {@code try} and returns the first
     * one that has happened.
     *
     * @param processor the processor to use
     * @param err       the number of errors in the error stack at the start
     * @param bs        the exception, which was caught by the caller of this method. If there are no sooner exceptions
     *                  in the stack, then this is the first one, and this is returned.
     * @return the exception, which occurred first.
     */
    private static BadSyntax getFirstError(Processor processor, int err, BadSyntax bs) {
        while (err < processor.errors().size()) {
            bs = processor.errors().pop();
        }
        return bs;
    }

    /**
     * The Jamal input contains nested blocks of several levels. When a block closes, all non-global user defined and
     * non-global built-in macros are released. They are not available anymore.
     * <p>
     * When an error occurs in a try block, it is not guaranteed that all opened blocks are also closed. The error may
     * happen deep inside blocks nested. The blocks that were opened before the error are still open and they have to be
     * closed.
     * <p>
     * Each block has a marker object. The current block marker object is saved by the try macro at the start and if
     * there is an error the marker is used to release the blocks following it.
     * <p>
     * This method releases (pops) all blocks following the marker.
     *
     * @param processor   the processor to use
     * @param markerStart the marker object at the start of the try block. This block is not released.
     * @throws BadSyntax if we run out of blocks and the marker was ntot found
     */
    private static void cleanUpTheMarkerStack(Processor processor, Marker markerStart) throws BadSyntax {
        Marker markerEnd;
        while ((markerEnd = processor.getRegister().test()) != null && !Objects.equals(markerStart, markerEnd)) {
            processor.getRegister().pop(markerEnd);
        }
    }
}

/*template jm_try
{template |try|try$M$$C$|define opening and closing string|
  {variable |M|enum("","!","?")}
  {variable |C|"..."}
}
 */