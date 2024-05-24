package javax0.jamal.xls;


import javax0.jamal.api.*;
import javax0.jamal.api.Macro.Name;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Name("xls:open")
public class Open implements Macro, Scanner.WholeInput {

    // snipline DEFAULT_WORKBOOK filter="(.*?)"
    public static final String XLS_WORKBOOK = "xls$:worksheet";

    private enum Mode {
        READ, WRITE
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // snippet open_parops
        final var file = scanner.str(null, "file", "in", "input", "from").required();
        // * `file` (aliases are `in`, `input`, `from`) is the name of the file that contains the XLS workbook.
        //  Technically `file` is also an alias, thus a macro havign the name of `file` will not be considered.
        // This parop must be defined.
        final var output = scanner.str(null, "out", "output", "to").optional();
        // * `out` (aliases are `output`, `to`) is the name of the file where the XLS workbook is written.
        // This parop is optional.
        // If this parop is defined, then the mode is automatically considered to be `WRITE`.
        // In other cases, `READ` is the default, and it is an error to specify `READ` when there is a defined output.
        final var id = scanner.str(null, "workbook", "wb").defaultValue(XLS_WORKBOOK);
        // * `workbook` (aliases are `wb`) is the name of the workbook that is used to refer to the workbook in the rest of the document.
        // This parop is optional and the default value is `+{%@snip DEFAULT_WORKBOOK%}+`.
        final var mode = scanner.enumeration(Mode.class).defaultValue(Mode.READ);
        // * `READ` or `WRITE` is the mode of the operation. If the `out` parop is defined then the mode is `WRITE`.
        // This parop is optional and the default value is `READ`.
        // end snippet
        scanner.done();

        final var reference = in.getReference();
        final var fileName = FileTools.absolute(reference, file.get());
        final String outFileName;
        final boolean readOnly;
        if (output.isPresent()) {
            outFileName = FileTools.absolute(reference, output.get());
            readOnly = false;
        } else {
            outFileName = fileName;
            readOnly = mode.get(Mode.class) == Mode.READ;
        }
        class WorkbookProblemException extends BadSyntax {
            public WorkbookProblemException(String message, Exception e) {
                super(message, e);
            }
        }
        try {
            final var xlsContent = FileTools.getFileBinaryContent(fileName, true, processor);
            try (final var fis = new ByteArrayInputStream(xlsContent)) {
                // ignore the warning, the workbook is closed in the holder
                final var wb = WorkbookFactory.create(fis);
                final var wh = new WorkbookHolder(id.get(), readOnly, outFileName, wb, processor, in.getPosition().top());
                processor.define(wh);
            } catch (Exception e) {
                throw new WorkbookProblemException("Cannot read the XLS workbook '" + file.get() + "'", e);
            }
        } catch (WorkbookProblemException problem) {
            throw problem;
        } catch (BadSyntax e) {
            BadSyntax.when(FileTools.isRemote(outFileName), "Cannot create the XLS workbook '" + file.get() + "' from remote file and it seems not to exist", e);
            BadSyntax.when(readOnly, "Cannot create the XLS workbook '" + file.get() + "' in read only mode, it does not seem to exist", e);
            try {
                final var wb = WorkbookFactory.create(outFileName.endsWith(".xlsx"));
                final var wh = new WorkbookHolder(id.get(), false, outFileName, wb, processor, in.getPosition().top());
                processor.define(wh);
            } catch (IOException ex) {
                throw new BadSyntax("Cannot create the XLS workbook '" + file.get() + "'", ex);
            }
        }
        return "";
    }

    public static class OpenWorkbook {
        final public boolean readOnly;
        public Workbook workbook;
        final String fileName;
        final Processor processor;
        final Position pos;
        boolean closed = false;

        public boolean isClosed() {
            return closed;
        }

        public OpenWorkbook(boolean readOnly, Workbook workbook, String fileName, Processor processor, Position pos) {
            this.readOnly = readOnly;
            this.workbook = workbook;
            this.fileName = fileName;
            this.processor = processor;
            this.pos = pos;
        }
    }

    public static class WorkbookHolder extends IdentifiedObjectHolder<OpenWorkbook> implements AutoCloseable {

        public WorkbookHolder(final String id, final boolean readOnly, final String fileName, final Workbook workbook, final Processor processor, final Position pos) {
            super(new OpenWorkbook(readOnly, workbook, fileName, processor, pos), id);
            processor.deferredClose(this);
        }

        @Override
        public void close() throws Exception {
            if (!getObject().readOnly) {
                String fn = getObject().fileName;
                BadSyntax.when(FileTools.isRemote(fn),
                        "Cannot write the XLS workbook '" + fn + "' to remote file");
                FileTools.assertSafe(fn, getObject().pos.file);
                try (final var baos = new ByteArrayOutputStream()) {
                    getObject().workbook.write(baos);
                    FileTools.writeFileContent(fn, baos.toByteArray(), getObject().processor);
                } catch (IOException e) {
                    throw new BadSyntax("Cannot write the XLS workbook '" + fn + "'", e);
                } finally {
                    getObject().workbook.close();
                    getObject().closed = true;
                }
            } else {
                getObject().workbook.close();
                getObject().closed = true;
            }
        }
    }

}
