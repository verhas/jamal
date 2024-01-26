package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Closer;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Position;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scanner;
import javax0.javalex.JavaSourceDiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Input.makeInput;

public class JavaSourceInsert implements Macro, Scanner.FirstLine {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var scanner = newScanner(in, processor);
        final var file = scanner.str(null, "to", "file", "into");
        final var segment = scanner.str("segment", "at", "id").optional();
        final var update = scanner.bool(null, "check", "checkUpdate", "update", "updateOnly");
        final var throwUp = scanner.bool(null, "failOnUpdate", "failUpdate", "updateError");
        final var wholeFile = scanner.bool(null, "wholeFile");
        scanner.done();
        BadSyntax.when(wholeFile.is() && segment.isPresent(), "When the whole file is updated then the segment should not be specified.");
        final JavaSourceInsertCloser closer;
        if (in.isEmpty()) {
            closer = new JavaSourceInsertCloser(file.get(), segment.get(), in.getPosition(), update.is() || throwUp.is(), wholeFile.is());
            processor.deferredClose(closer);
        } else {
            try (final var _c = new JavaSourceInsertCloser(file.get(), segment.get(), in.getPosition(), update.is() || throwUp.is(), wholeFile.is())) {
                closer = _c;
                closer.set(in);
                closer.set(processor);
            }
        }
        if (throwUp.is()) {
            processor.deferredClose(() -> BadSyntax.when(closer.isUpdated(), "The file " + file.get() + " was updated."));
        }
        return in.toString();
    }

    @Override
    public String getId() {
        return "java:insert";
    }

    private static class JavaSourceInsertCloser implements AutoCloseable, Closer.OutputAware, Closer.ProcessorAware {
        private final String file;
        private final String segment;

        private final Position pos;
        private Input output;
        private Processor processor;

        private final boolean update;

        private boolean updated = false;

        private final boolean wholeFile;

        private JavaSourceInsertCloser(final String file, final String segment, final Position pos, boolean update, final boolean wholeFile) {
            this.file = file;
            this.segment = segment;
            this.pos = pos;
            this.update = update;
            this.wholeFile = wholeFile;
        }

        private static final Pattern segmentStartPattern = Pattern.compile("^\\s*//\\s*<\\s*editor-fold(.*>)");
        private static final Pattern segmentEndPattern = Pattern.compile("^\\s*//\\s*</\\s*editor-fold\\s*>");

        @Override
        public void close() throws BadSyntax {
            final String fileName = FileTools.absolute(pos.file, file);
            final String newContent;
            final var originalContent= getFileContent(fileName);
            if (wholeFile) {
                newContent = output.toString();
            } else {
                final var linesSrc = originalContent.split("\n", -1);
                final var linesOut = new ArrayList<String>(linesSrc.length);
                final var linesGen = Arrays.asList(output.toString().split("\n", -1));
                boolean inSegment = false;
                boolean segmentAdded = false;
                for (final var line : linesSrc) {
                    if (inSegment) {
                        inSegment = !segmentEndPattern.matcher(line).matches();
                        segmentAdded = copyOrSkipLineInSegment(linesOut, linesGen, inSegment, line);
                    } else {
                        inSegment = copyLineOutOfSegment(linesOut, segmentAdded, line);
                    }
                }
                BadSyntax.when(inSegment, "The segment " + segment + " was not closed in the file " + file + ".");
                BadSyntax.when(!segmentAdded, "The segment " + segment + " was not found in the file " + file + ".");
                newContent = String.join("\n", linesOut.toArray(String[]::new));
            }
            if (update) {
                if (!new JavaSourceDiff().test(originalContent, newContent)) {
                    return;
                }
            }
            FileTools.writeFileContent(fileName, newContent, processor);
            updated = true;
        }

        /**
         * Get the content of the file. If the file does not exist or is not readable, then the content is empty.
         *
         * @param fileName the name of the file
         * @return the content of the file
         */
        private String getFileContent(String fileName) {
            try {
                return FileTools.getFileContent(fileName, processor);
            } catch (BadSyntax e) {
                return "";
            }
        }

        /**
         * Skip the line, or copy the line to the output when the segment ends.
         * When the segment ends, the generated code lines are also added and the segment closing line also.
         * When the segment does not end yet, then the line is simply skipped, since the new generated code will
         * replace the old one.
         *
         * @param linesOut  the output lines to append the current line to
         * @param linesGen  the generated lines to add to the output when the segment ends
         * @param inSegment signals if we are still inside the segment, the line is not a segment end
         * @param line      the line to copy to the output if it is the segment end
         * @return {@code true} if the line is the segment end and the segment was added to the output
         */
        private static boolean copyOrSkipLineInSegment(ArrayList<String> linesOut, List<String> linesGen, boolean inSegment, String line) {
            boolean segmentAdded = false;
            if (!inSegment) {
                linesOut.addAll(linesGen);
                linesOut.add(line);
                segmentAdded = true;
            }
            return segmentAdded;
        }

        /**
         * Copy the line to the output and check if the line starts the target segment.
         * The target segment starts with a line that matches the {@code segmentStartPattern}
         * and the specified segment id is the same as the segment id in the line.
         * <p>
         * If the desired segment id is {@code null}, then any segment start is accepted.
         *
         * @param linesOut     the output lines to append the current line to
         * @param segmentAdded signals if the segment was already added, in which case finding another segment start is an error
         * @param line         the line to copy to the output
         * @return {@code true} if the line starts the segment
         * @throws BadSyntax if the segment is already added and the line starts a new segment
         */
        private boolean copyLineOutOfSegment(ArrayList<String> linesOut, boolean segmentAdded, String line) throws BadSyntax {
            linesOut.add(line);
            boolean inSegment = false;
            final var matcher = segmentStartPattern.matcher(line);
            if (matcher.matches()) {
                final var segmentParameters = matcher.group(1);
                // get the parameters from the segment without any key constraints using the params handling utility parser
                final var params = Params.using(null)
                        .from(() -> "for segment " + segment + "in file " + file + "[" + segmentParameters + "]")
                        .endWith('>')
                        .fetchParameters(makeInput(segmentParameters));
                if (segment == null || (params.get("id") != null && segment.equals(params.get("id")))) {
                    inSegment = true;
                    BadSyntax.when(segmentAdded, segment == null ?
                                    "There are multiple segments in the file %s and no segment id specified"
                                    :
                                    String.format("There are multiple segments with the id %s in the file %%s.", segment),
                            file);
                }
            }
            return inSegment;
        }

        @Override
        public void set(final Input output) {
            this.output = output;
        }

        @Override
        public void set(final Processor processor) {
            this.processor = processor;
        }

        public boolean isUpdated() {
            return updated;
        }

    }

}
