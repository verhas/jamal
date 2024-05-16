package javax0.jamal.xls;


import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.IdentifiedObjectHolder;
import javax0.jamal.tools.Scanner;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Open implements Macro, Scanner.WholeInput {

    public static final String XLS_WORKBOOK = "xls$:worksheet";

    private enum Mode {
        READ, WRITE
    }

    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = scanner.str(null, "file", "in", "input", "from").required();
        final var output = scanner.str(null, "out", "output", "to").optional();
        final var id = scanner.str(null, "workbook", "wb").defaultValue(XLS_WORKBOOK);
        final var mode = scanner.enumeration(Mode.class).defaultValue(Mode.READ);
        scanner.done();

        final var reference = in.getReference();
        final var fileName = FileTools.absolute(reference, file.get());
        final var f = new File(fileName);
        final String outFileName;
        final boolean readOnly;
        if (output.isPresent()) {
            outFileName = FileTools.absolute(reference, output.get());
            readOnly = false;
        } else {
            outFileName = fileName;
            readOnly = mode.get(Mode.class) == Mode.READ;
        }
        if (f.exists()) {
            try (final var fis = new FileInputStream(f)) {
                // ignore the warning, the workbook is closed in the holder
                final var wb = WorkbookFactory.create(fis);
                final var wh = new WorkbookHolder(id.get(), readOnly, outFileName, wb, processor);
                processor.define(wh);
            } catch (IOException e) {
                throw new BadSyntax("Cannot read the XLS workbook '" + fileName + "'", e);
            }
        } else {
            BadSyntax.when(readOnly || output.isPresent(), "The file '" + fileName + "' does not exist");
            try {
                final var wb = WorkbookFactory.create(outFileName.endsWith(".xlsx"));
                final var wh = new WorkbookHolder(id.get(), false, outFileName, wb, processor);
                processor.define(wh);
            } catch (IOException e) {
                throw new BadSyntax("Cannot create the XLS workbook '" + fileName + "'", e);
            }
        }
        return "";
    }

    @Override
    public String getId() {
        return "xls:open";
    }

    public static class OpenWorkbook {
        final boolean readOnly;
        public Workbook workbook;
        final String fileName;


        public OpenWorkbook(boolean readOnly, Workbook workbook, String fileName) {
            this.readOnly = readOnly;
            this.workbook = workbook;
            this.fileName = fileName;
        }
    }


    public static class WorkbookHolder extends IdentifiedObjectHolder<OpenWorkbook> implements AutoCloseable {

        public WorkbookHolder(final String id, final boolean readOnly, final String fileName, final Workbook workbook, final Processor processor) {
            super(new OpenWorkbook(readOnly, workbook, fileName), id);
            processor.deferredClose(this);
        }

        @Override
        public void close() throws Exception {
            if (!getObject().readOnly) {
                try (final var fos = new FileOutputStream(getObject().fileName)) {
                    getObject().workbook.write(fos);
                }
            }
            getObject().workbook.close();
        }
    }

}
