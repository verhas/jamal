package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Scanner;
import javax0.jamal.tools.param.IntegerParameter;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.OptionalInt;

@Macro.Name("docx:picture")
public class MacroDocxPicture implements Macro, Scanner {

    private static class CallBack implements XWPFContext.DocxIntermediaryCallBack {
        final static List<Object> types = List.of(
                ".emf", XWPFDocument.PICTURE_TYPE_EMF,
                ".wmf", XWPFDocument.PICTURE_TYPE_WMF,
                ".pict", XWPFDocument.PICTURE_TYPE_PICT,
                ".jpeg", XWPFDocument.PICTURE_TYPE_JPEG,
                ".jpg", XWPFDocument.PICTURE_TYPE_JPEG,
                ".png", XWPFDocument.PICTURE_TYPE_PNG,
                ".dib", XWPFDocument.PICTURE_TYPE_DIB,
                ".gif", XWPFDocument.PICTURE_TYPE_GIF,
                ".tiff", XWPFDocument.PICTURE_TYPE_TIFF,
                ".eps", XWPFDocument.PICTURE_TYPE_EPS,
                ".bmp", XWPFDocument.PICTURE_TYPE_BMP,
                ".wpg", XWPFDocument.PICTURE_TYPE_WPG);
        private final File file;
        private List<XWPFParagraph> paragraphs;
        private int paragraphStartIndex;
        private int runStartIndex;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final OptionalInt width;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private final OptionalInt height;
        private final boolean distorted;

        CallBack(final File file, final OptionalInt width, final OptionalInt height, final boolean distorted) {
            this.file = file;
            this.width = width;
            this.height = height;
            this.distorted = distorted;
        }

        public void setRunStartIndex(final int runStartIndex) {
            this.runStartIndex = runStartIndex;
        }

        @Override
        public void setDocument(final XWPFDocument document) {
            XWPFContext.DocxIntermediaryCallBack.super.setDocument(document);
        }

        @Override
        public void setParagraphs(final List<XWPFParagraph> paragraphs) {
            this.paragraphs = paragraphs;
        }

        @Override
        public void setParagraphStartIndex(int paragraphStartIndex) {
            this.paragraphStartIndex = paragraphStartIndex;
        }

        @Override
        public void process() throws BadSyntax {
            try {
                final var run = paragraphs.get(paragraphStartIndex).getRuns().get(runStartIndex);
                final var fileName = file.getName();
                final var image = ImageIO.read(file);
                final int width, height;
                if (this.width.isPresent() && this.height.isPresent()) {
                    width = this.width.getAsInt();
                    height = this.height.getAsInt();
                } else if (this.width.isPresent()) {
                    width = this.width.getAsInt();
                    if (distorted) {
                        height = image.getHeight();
                    } else {
                        height = image.getHeight() * width / image.getWidth();
                    }
                } else if (this.height.isPresent()) {
                    height = this.height.getAsInt();
                    if (distorted) {
                        width = image.getWidth();
                    } else {
                        width = image.getWidth() * height / image.getHeight();
                    }
                } else {
                    width = image.getWidth();
                    height = image.getHeight();
                }
                try (final var fis = new FileInputStream(file)) {
                    run.addPicture(fis, getImageType(fileName), fileName,
                            Units.pixelToEMU(width),
                            Units.pixelToEMU(height));
                }
            } catch (Exception e) {
                throw new BadSyntax("Error inserting a picture int a docxument", e);
            }
        }

        private int getImageType(final String fileName) {
            int type = 0;
            for (int i = 0; i < types.size(); i += 2) {
                if (fileName.endsWith((String) types.get(i))) {
                    type = (int) types.get(i + 1);
                    break;
                }
            }
            return type;
        }
    }

    private static OptionalInt optional(IntegerParameter param) throws BadSyntax {
        if (param.isPresent()) {
            return OptionalInt.of(param.get());
        } else {
            return OptionalInt.empty();
        }
    }
    /*
     *
     * tag::picture[]
     * {%macro docx::picture%}
     *
     * Using this macro you can include a picture into a document.
     * The syntax of the macro is
     *
     * [source]
     * ----
     *   {@docx:picture [options] file_name}
     * ----
     * The file name can be absolute or relative to the processed file.
     * The macro will copy the content of the picture file and insert the picture into the document at the place where the macro is in the source document.
     *
     * Inserting a picture into a document using a macro may seem to be counterintuitive.
     * This is a functionality supported by the WYSIWYG functionality of Word.
     * There are two reasons why you may decide to use the macro instead.
     *
     * . When the external picture is defined by the macro it is read and inserted into the target document by the time the macro processing is executed.
     * If the picture is not final, and may change during the lifecycle of the documentation the macro will always include the latest version.
     *
     * . The processing may have different options for the picture and the actual picture may be selected from a set during the macro execution.
     * In this case it makes perfect sense to use the macro.
     *
     * If not for these cases then just insert the picture into the document using the Word built-in functionality.
     * The options that can modify the behavior of the picture handling are:
     * end::picture[]
     */
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        // tag::picture_options[]
        final var width = scanner.number(null, "width").optional();
        // can define the width of the picture.
        // The default value is the actual width of the picture, or a scaled width in case the height is defined and the picture is not distorted.
        final var height = scanner.number(null, "height").optional();
        // can define the height of the picture.
        // The default value is the actual height of the picture, or a scaled height in case the width is defined and the picture is not distorted.
        final var distorted = scanner.bool(null, "distort", "distorted");
        // can define if the picture is distorted or not.
        // If a picture is not distorted and only one of the `width` and `height` is defined, the non-defined parameter will be calculated.
        // In this case, the picture aspect ratio is preserved.
        // When this option is used and either `width` or `height` is defined, the other parameter will keep the value given by the picture itself.ß
        // end::picture_options[]
        scanner.done();
        var fileName = FileTools.absolute(in.getReference(), in.toString().trim());
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(new File(fileName), optional(width), optional(height), distorted.is()));
        return "";
    }
}
