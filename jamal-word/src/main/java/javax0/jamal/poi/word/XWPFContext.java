package javax0.jamal.poi.word;

import javax0.jamal.api.Context;
import org.apache.poi.poifs.crypt.HashAlgorithm;

public class XWPFContext implements Context {
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

    boolean readOnly = false;
    String password = null;
    HashAlgorithm hashAlgorithm = HashAlgorithm.none;
}
