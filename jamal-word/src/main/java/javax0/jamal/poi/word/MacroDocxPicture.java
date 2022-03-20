package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.OptionalInt;

public class MacroDocxPicture implements Macro {

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

    private static OptionalInt optional(Params.Param<Integer> param) throws BadSyntax {
        if (param.isPresent()) {
            return OptionalInt.of(param.get());
        } else {
            return OptionalInt.empty();
        }
    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var width = Params.<Integer>holder(null, "width").asInt();
        final var height = Params.<Integer>holder(null, "height").asInt();
        final var distorted = Params.<Boolean>holder(null, "distort", "distorted").asBoolean();
        Scan.using(processor).from(this).between("()").keys(width, height, distorted).parse(in);
        var fileName = FileTools.absolute(in.getReference(), in.toString().trim());
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(new File(fileName), optional(width), optional(height), distorted.is()));
        return "";
    }

    @Override
    public String getId() {
        return "docx:picture";
    }
}
