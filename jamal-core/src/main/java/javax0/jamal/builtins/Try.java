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
        final boolean report;
        if (query) {
            skip(in, 1);
            report = false;
        } else {
            report = firstCharIs(in, REPORT_ERRMES);
            if (report) {
                skip(in, 1);
            }
        }
        skipWhiteSpaces(in);
        final var markerStart = processor.getRegister().test();
        final int err = processor.errors().size();
        try {
            final var result = processor.process(in);
            BadSyntax bse = null;
            while (err < processor.errors().size()) {
                bse = processor.errors().pop();
            }
            if (bse != null) {
                throw bse;
            }
            if (query) {
                return "true";
            } else {
                return result;
            }
        } catch (BadSyntax bs) {
            while (err < processor.errors().size()) {
                bs = processor.errors().pop();
            }
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
    }

    private void cleanUpTheMarkerStack(Processor processor, Marker markerStart) throws BadSyntax {
        Marker markerEnd;
        while ((markerEnd = processor.getRegister().test()) != null && !Objects.equals(markerStart, markerEnd)) {
            processor.getRegister().pop(markerEnd);
        }
    }
}
