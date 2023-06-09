package javax0.jamal.snippet;

import javax0.jamal.api.*;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.javalex.JavaSourceDiff;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Input.makeInput;

public class JavaSourceInsert implements Macro {
    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var file = Params.<String>holder(null, "to", "file", "into");
        final var segment = Params.<String>holder("segment", "at", "id").orElseNull();
        final var update = Params.<Boolean>holder(null, "check", "checkUpdate", "update", "updateOnly").asBoolean();
        final var throwUp = Params.<Boolean>holder(null, "failOnUpdate", "failUpdate", "updateError").asBoolean();
        Scan.using(processor).from(this).firstLine().keys(file, segment, update, throwUp).parse(in);
        final JavaSourceInsertCloser closer;
        if (in.isEmpty()) {
            closer = new JavaSourceInsertCloser(file.get(), segment.get(), in.getPosition(), update.is() || throwUp.is());
            processor.deferredClose(closer);
        } else {
            try (final var _c = new JavaSourceInsertCloser(file.get(), segment.get(), in.getPosition(), update.is() || throwUp.is())) {
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

        private JavaSourceInsertCloser(final String file, final String segment, final Position pos, boolean update) {
            this.file = file;
            this.segment = segment;
            this.pos = pos;
            this.update = update;
        }

        private static final Pattern segmentStartPattern = Pattern.compile("^\\s*//\\s*<\\s*editor-fold(.*>)");
        private static final Pattern segmentEndPattern = Pattern.compile("^\\s*//\\s*</\\s*editor-fold\\s*>");

        @Override
        public void close() throws BadSyntax {
            final String fileName = FileTools.absolute(pos.file, file);
            final var originalContent = FileTools.getFileContent(fileName, processor);
            final var source = originalContent.split("\n", -1);
            final var outlines = new ArrayList<String>(source.length);
            boolean inSegment = false;
            boolean segmentAdded = false;
            for (final var line : source) {
                if (inSegment) {
                    final var matcher = segmentEndPattern.matcher(line);
                    if (matcher.matches()) {
                        // we squeeze the output into one line, no problem
                        outlines.add(output.toString());
                        outlines.add(line);
                        inSegment = false;
                        segmentAdded = true;
                    }
                } else {
                    outlines.add(line);
                    final var matcher = segmentStartPattern.matcher(line);
                    if (matcher.matches()) {
                        final var segmentParameters = matcher.group(1);
                        final var params = Params.using(null)
                                .from(() -> "for segment " + segment + "in file " + file + "[" + segmentParameters + "]")
                                .endWith('>')
                                .fetchParameters(makeInput(segmentParameters));
                        if (segment == null || (params.get("id") != null && segment.equals(params.get("id")))) {
                            inSegment = true;
                            BadSyntax.when(segmentAdded, segment == null ?
                                    "There are multiple segments in the file " + file + "and no segment id specified"
                                    :
                                    "There are multiple segments with the id " + segment + "in the file " + file + ".");
                        }
                    }
                }
            }
            BadSyntax.when(inSegment, "The segment " + segment + " was not closed in the file " + file + ".");
            BadSyntax.when(!segmentAdded, "The segment " + segment + " was not found in the file " + file + ".");
            final var newContent = String.join("\n", outlines.toArray(String[]::new));
            if (update) {
                if (!new JavaSourceDiff().test(originalContent, newContent)) {
                    return;
                }
            }
            FileTools.writeFileContent(fileName, newContent, processor);
            updated = true;
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
