package javax0.jamal.builtins;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Marker;
import javax0.jamal.api.Processor;

import java.util.Objects;

import static javax0.jamal.api.SpecialCharacters.QUERY;
import static javax0.jamal.api.SpecialCharacters.REPORT_ERRMES;
import static javax0.jamal.tools.InputHandler.firstCharIs;
import static javax0.jamal.tools.InputHandler.skip;
import static javax0.jamal.tools.InputHandler.skipWhiteSpaces;

public class Try implements Macro {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final boolean query = firstCharIs(in, QUERY);
        final boolean report = isReport(in, query);
        skipWhiteSpaces(in);
        final var markerStart = processor.getRegister().test();
        final int err = processor.errors().size();
        try {
            return process(in, processor, query, err);
        } catch (BadSyntax bs) {
            return process(processor, query, report, markerStart, err, bs);
        }
    }

    private static String process(Processor processor, boolean query, boolean report, Marker markerStart, int err, BadSyntax bs) throws BadSyntax {
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

    private static String process(Input in, Processor processor, boolean query, int err) throws BadSyntax {
        final var result = processor.process(in);
        BadSyntax bs = getFirstError(processor, err,null);
        if (bs != null) {
            throw bs;
        }
        if (query) {
            return "true";
        } else {
            return result;
        }
    }

    private static BadSyntax getFirstError(Processor processor, int err, BadSyntax bs) {
        while (err < processor.errors().size()) {
            bs = processor.errors().pop();
        }
        return bs;
    }

    private static boolean isReport(Input in, boolean query) {
        final boolean report;
        if (query) {
            skip(in, 1);
            return false;
        } else {
            report = firstCharIs(in, REPORT_ERRMES);
            if (report) {
                skip(in, 1);
            }
        }
        return report;
    }

    private static void cleanUpTheMarkerStack(Processor processor, Marker markerStart) throws BadSyntax {
        Marker markerEnd;
        while ((markerEnd = processor.getRegister().test()) != null && !Objects.equals(markerStart, markerEnd)) {
            processor.getRegister().pop(markerEnd);
        }
    }
}
