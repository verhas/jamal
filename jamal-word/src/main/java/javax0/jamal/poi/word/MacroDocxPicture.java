package javax0.jamal.poi.word;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;

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

        CallBack(final File file) {
            this.file = file;
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
                //final var run = paragraphs.get(paragraphStartIndex).getRuns().get(runStartIndex);
                final var run = paragraphs.get(paragraphStartIndex).createRun();
                final var fileName = file.getName();
                int type = 0;
                for (int i = 0; i < types.size(); i += 2) {
                    if (fileName.endsWith((String) types.get(i))) {
                        type = (int) types.get(i + 1);
                        break;
                    }
                }
                BufferedImage imaqe = ImageIO.read(file);
                int width = imaqe.getWidth();
                int height = imaqe.getHeight();
                try (final var fis = new FileInputStream(file)) {
                    run.addPicture(fis, type, file.getName(), Units.pixelToEMU(width), Units.pixelToEMU(height));
                }
            } catch (Exception e) {
                throw new BadSyntax("Error inserting a picture int a docxument", e);
            }
        }


    }

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var position = in.getPosition();
        var reference = position.file;
        var fileName = FileTools.absolute(reference, in.toString().trim());
        final var context = XWPFContext.getXWPFContext(processor);
        context.register(new CallBack(new File(fileName)));
        return "";
    }

    @Override
    public String getId() {
        return "docx:picture";
    }
}
