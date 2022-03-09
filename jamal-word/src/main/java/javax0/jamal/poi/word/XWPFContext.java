package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Context;
import javax0.jamal.api.Processor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.util.ArrayList;
import java.util.List;

/**
 * The implemented Context interface is only a signal interface without any methods or constants.
 * The classes, like this, which are used by embedding applications can implement this interface.
 * The embedding application can put information into the context object and the macros can retrieve it from there.
 * When the processing finishes the information flow can be reversed.
 * The embedding application can read the information macros created.
 * <p>
 * This implementation of the Context interface is used by the {@link XWPFProcessor} class.
 * <p>
 * The DOCX macros cannot perform actions on the Word document directly.
 * Macros have access only to the input and the processor.
 * Because of that they cannot directly modify the document structure.
 * The recommended way to modify the document is to register a callback function.
 * This call-back is invoked by the processor later when no macro execution is performed.
 * That way the document is not concurrently modified by the macros and the processor at the same time.
 * <p>
 * There are two ways to register a callback function.
 * One is to register a terminal call back.
 * Terminal call backs are invoked when the processing of the document is finished.
 *
 * The other way is to register an intermediary callback.
 * This call-back is invoked by the processor between macro executions.
 */
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

    public void register(DocxIntermediaryCallBack callback) {
        intermediaries.add(callback);
    }

    public void register(DocxTerminalCallBack callback) {
        terminals.add(callback);
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

    public Context getContext() {
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
