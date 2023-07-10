package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Scanner;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

/*
 * tag::trackChanges[]
 * {%macro docx::trackChanges%}
 *
 * This macro can switch on or off the track changes in the output document.
 * The syntax of the macro is
 *
 * [source]
 * ----
 *   {@docx::trackChanges}
 * ----
 *
 * or
 *
 * [source]
 * ----
 *   {@docx::trackChanges off}
 * ----
 *
 * Note that you can also set the protection of the output document so that the change tracking cannot be switched off.
 *
 * end::trackChanges[]
 */
public class MacroDocxTrackRevisions implements Macro, Scanner.WholeInput {
    private static class CallBack implements XWPFContext.DocxTerminalCallBack {
        private XWPFDocument document;
        private final boolean off;

        CallBack(boolean off) {
            this.off = off;
        }

        @Override
        public void setDocument(final XWPFDocument document) {
            this.document = document;
        }

        @Override
        public void process() {
            document.setTrackRevisions(!off);
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var off = scanner.bool(null, "off");
        scanner.done();
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(off.is()));
        return "";
    }

    @Override
    public String getId() {
        return "docx:trackChanges";
    }
}
