package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Context;
import javax0.jamal.api.Processor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.ArrayList;
import java.util.List;

public class XWPFContext implements Context {
    public interface DocxCallBack {
        default void setDocument(XWPFDocument document) {
        }

        void process();
    }

    public interface DocxTerminalCallBack extends DocxCallBack {
    }

    public interface DocxIntermediaryCallBack extends DocxCallBack {
        default void setParagraphStartIndex(int paragraphStartIndex) {
        }

        default void setRunStartIndex(int runStartIndex) {
        }
    }

    private List<DocxTerminalCallBack> terminals = new ArrayList<>();
    private List<DocxIntermediaryCallBack> intermediaries = new ArrayList<>();

    public void register(DocxCallBack callback) {
        if (callback instanceof DocxTerminalCallBack) {
            terminals.add((DocxTerminalCallBack) callback);
        }
        if (callback instanceof DocxIntermediaryCallBack) {
            intermediaries.add((DocxIntermediaryCallBack) callback);
        }
    }

    public List<DocxTerminalCallBack> getTerminals() {
        final var value = terminals;
        terminals = new ArrayList<>();
        return value;
    }

    public List<DocxIntermediaryCallBack> getIntermediaries() {
        final var value = intermediaries;
        intermediaries = new ArrayList<>();
        return value;
    }

    /**
     * The parent context can be used by any application that embeds the XWPFProcessor.
     * A macro can call {@code processor.getContext().getContext()} to get the parent context.
     * The embedding application can use the processor and call {@code xwpfProcessor.getProcessor().getContext().setContext(parentContext)}
     */
    Context parent;

    public Context getContex() {
        return parent;
    }

    public void setContext(final Context parent) {
        this.parent = parent;
    }

    public static XWPFContext getXWPFContext(Processor processor) throws BadSyntax {
        final var context = processor.getContext();
        if (context instanceof XWPFContext) {
            return (XWPFContext) context;
        } else {
            throw new BadSyntax("The macros 'docx:...' can only be used in DOCX files.");
        }
    }
}
