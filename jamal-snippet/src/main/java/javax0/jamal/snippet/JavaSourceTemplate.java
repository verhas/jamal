package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.Scan;
import javax0.javalex.JavaSourceDiff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static javax0.jamal.tools.Input.makeInput;

public class JavaSourceTemplate implements Macro {
    private static final Pattern segmentStartPattern = Pattern.compile("^\\s*//\\s*<\\s*editor-fold(.*>)");
    private static final Pattern segmentEndPattern = Pattern.compile("^\\s*//\\s*</\\s*editor-fold\\s*>");
    private static final Pattern segmentHeaderPattern = Pattern.compile("^\\s*//(.*)$");

    private static final String[] NOARGS = new String[0];

    @Override
    public String evaluate(final Input in, final Processor processor) throws BadSyntax {
        final var path = Params.<String>holder(null, "path").orElseNull();
        final var template = Params.<String>holder(null, "template", "name", "id");
        final var update = Params.<Boolean>holder(null, "check", "checkUpdate", "update", "updateOnly").asBoolean();
        final var debug = Params.<Boolean>holder(null, "debug").asBoolean();
        final var apply = Params.<Boolean>holder(null, "apply").asBoolean();
        final var throwUp = Params.<Boolean>holder(null, "failOnUpdate", "failUpdate", "updateError").asBoolean();
        Scan.using(processor).from(this).firstLine().keys(path, template, update, throwUp, debug, apply).parse(in);

        final String templateContent;
        if (apply.is()) {
            final var templateMacro = template.get();
            templateContent =
                    processor.getRegister().getMacro(templateMacro)
                            .filter(m -> m instanceof javax0.jamal.engine.UserDefinedMacro)
                            .map(m -> (javax0.jamal.engine.UserDefinedMacro) m)
                            .map(javax0.jamal.engine.UserDefinedMacro::getContent)
                            .orElseThrow(() -> new BadSyntax(String.format("The template '%s' is not defined", templateMacro)));
            BadSyntax.when(in.toString().trim().length() > 0, "The input should be empty when applying a template.");
        } else {
            templateContent = in.toString();
            processor.getRegister().define(processor.newUserDefinedMacro(template.get(), templateContent, NOARGS));
        }
        final String resultAggregation;
        if( path.isPresent()) {
            resultAggregation = processFiles(FileTools.absolute(in.getReference(), path.get()), template.get(), update.is(), throwUp.is(), processor, templateContent);
        }else{
            resultAggregation = "";
        }
        return debug.is() ? resultAggregation : "";
    }

    @Override
    public String getId() {
        return "java:template";
    }

    private String processFiles(final String path,
                                final String template,
                                final boolean shouldOnlyUpdate,
                                final boolean isThrowUp,
                                final Processor processor,
                                final String input) throws BadSyntax {
        final var output = new StringBuilder();
        try {
            Files.find(Paths.get(path),
                            Integer.MAX_VALUE,
                            (filePath, fileAttr) -> fileAttr.isRegularFile() && filePath.toString().endsWith(".java"))
                    .forEach(file -> {
                        try {
                            processFile(file, template, shouldOnlyUpdate, isThrowUp, processor, input, output);
                        } catch (BadSyntax e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new BadSyntax("Cannot collect the Java files to update under " + path, e);
        }
        return output.toString();
    }

    private void processFile(final Path file,
                             final String template,
                             final boolean shouldOnlyUpdate,
                             final boolean isThrowUp,
                             final Processor processor,
                             final String input,
                             final StringBuilder output) throws BadSyntax {
        output.append("//").append(file).append("\n");
        final var originalContent = FileTools.getFileContent(file.toString(), processor);
        final var linesSrc = originalContent.split("\n", -1);
        final var linesOut = new ArrayList<String>(linesSrc.length);
        boolean inSegment = false;
        String lastSegment = "";
        boolean inSegmentHeader = false;
        StringBuilder in = null;
        for (final var line : linesSrc) {
            if (inSegment) {
                final var matcher = segmentEndPattern.matcher(line);
                if (matcher.matches()) {
                    final var generated = processor.process(makeInput(in.toString()));
                    output.append(generated);
                    final var linesGen = generated.split("\n", -1);
                    linesOut.addAll(List.of(linesGen));
                    linesOut.add(line);
                    inSegment = false;
                } else {
                    if (inSegmentHeader) {
                        final var headerMather = segmentHeaderPattern.matcher(line);
                        if (headerMather.matches()) {
                            linesOut.add(line);
                            in.append(headerMather.group(1)).append("\n");
                        } else {
                            inSegmentHeader = false;
                        }
                    }
                }
            } else {
                linesOut.add(line);
                final var matcher = segmentStartPattern.matcher(line);
                if (matcher.matches()) {
                    final var segmentParameters = matcher.group(1);
                    // get the parameters from the segment without any key constraints using the params handling utility parser
                    final var params = Params.using(null)
                            .from(() -> "for a Jamal template in file " + file + "[" + segmentParameters + "]")
                            .endWith('>')
                            .fetchParameters(makeInput(segmentParameters));
                    if (template.equals(params.get("template"))) {
                        inSegment = true;
                        inSegmentHeader = true;
                        in = new StringBuilder(input);
                        lastSegment = params.get("id");
                    }
                }
            }
        }
        BadSyntax.when(inSegment, "The segment " + lastSegment + " was not closed in the file " + file + ".");
        final var newContent = String.join("\n", linesOut.toArray(String[]::new));
        boolean differ = new JavaSourceDiff().test(originalContent, newContent);
        if (shouldOnlyUpdate) {
            if (!differ) {
                return;
            }
        }
        BadSyntax.when(isThrowUp && differ, "The file '%s' is not up to date.", file);
        FileTools.writeFileContent(file.toString(), newContent, processor);
    }
}
