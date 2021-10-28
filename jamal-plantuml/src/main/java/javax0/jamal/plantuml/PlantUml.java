package javax0.jamal.plantuml;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.api.InnerScopeDependent;
import javax0.jamal.api.Input;
import javax0.jamal.api.Macro;
import javax0.jamal.api.Processor;
import javax0.jamal.tools.Cache;
import javax0.jamal.tools.FileTools;
import javax0.jamal.tools.InputHandler;
import javax0.jamal.tools.Params;
import javax0.jamal.tools.PlaceHolders;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

public class PlantUml implements Macro, InnerScopeDependent {
    @Override
    public String evaluate(Input in, Processor processor) throws BadSyntax {
        final var root = Params.<String>holder("pu$folder", "folder").orElse("./");
        final var format = Params.<String>holder("pu$format", "format").orElse("SVG");
        final var template = Params.<String>holder("pu$template", "template").orElse("$file");
        Params.using(processor).keys(root, format, template).between("()").parse(in);
        final var fileName = InputHandler.fetch2EOL(in).trim();
        final var imageDir = root.get().endsWith("/") ? root.get() : root.get() + "/";
        final var absoluteFileName = FileTools.absolute(in.getReference(), imageDir + fileName);
        InputHandler.skipWhiteSpaces(in);
        final var umlText = getUmlText(in);
        final var output = new File(absoluteFileName).getAbsoluteFile();
        final var entry = getCacheEntry(output);
        try {
            final boolean erred;
            if (needPlantUmlRun(umlText, output, entry)) {
                erred = convertToFile(umlText, output, format.get());
                if (entry != null) {
                    //noinspection unchecked
                    entry.save(umlText, Map.of("error", "" + erred));
                }
            } else {
                erred = "true".equals(entry.getProperty("error"));
            }
            if (erred) {
                throw new BadSyntax("There was an error processing diagram for '" + fileName + "' in PlantUml.");
            }
            return PlaceHolders.with("$file", fileName).format(template.get());
        } catch (Exception e) {
            throw new BadSyntax("PlantUml diagram '" + fileName + "'cannot be created.", e);
        }
    }

    /**
     * Convert the text to diagram and save the diagram to the file.
     *
     * @param text   the string that contains the textual description of the diagram
     * @param file   the file where the output has to be written
     * @param format the string representation of the format. like {@code SVG}, {@code PNG}, ...
     * @return {@code true} if the conversion was erroneous, {@code false} otherwise
     * @throws BadSyntax   if the format is not supported
     * @throws IOException if the file cannot be written
     */
    private boolean convertToFile(String text, File file, String format) throws BadSyntax, IOException {
        final boolean erred;
        final var reader = new SourceStringReader(text);
        FileFormat fileFormat = convertFileFormat(format);
        final var os = new ByteArrayOutputStream();
        try (os) {
            erred = "(Error)".equals(reader.outputImage(os, new FileFormatOption(fileFormat)).getDescription());
        }
        //noinspection ResultOfMethodCallIgnored
        file.getParentFile().mkdirs();
        try (final var fos = new FileOutputStream(file)) {
            fos.write(os.toByteArray());
        }
        return erred;
    }

    private boolean needPlantUmlRun(String umlText, File output, Cache.Entry entry) {
        return entry == null || entry.isMiss() || !entry.getContent().toString().equals(umlText) || !output.exists();
    }

    private Cache.Entry getCacheEntry(File output) {
        try {
            return Cache.getEntry(output.toURI().toURL());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Convert the string to the enum value. In case the string does not represent any of the enum values then throw
     * BadSyntax.
     *
     * @param format the string that describes the output format of plantUML, like SVG, PNG...
     * @return the enum value
     * @throws BadSyntax if the string does not represent any of the enumerated values
     */
    private FileFormat convertFileFormat(String format) throws BadSyntax {
        for (final var fileFormat : FileFormat.values()) {
            if (format.equals(fileFormat.toString())) {
                return fileFormat;
            }
        }
        throw new BadSyntax("The output format '" + format + "' is not supported by PlantUml.");
    }

    /**
     * Get the diagram describing string from the input. This method does not consume the input. If the input contains
     * the
     * <pre>{@code
     * @startuml
     * }</pre>
     * <p>
     * and
     *
     * <pre>{@code
     * @enduml
     * }</pre>
     * <p>
     * lines, then leave it there. If they are not there, then the method prepends and appends the lines.
     *
     * @param in the input
     * @return the diagram describing string
     */
    private String getUmlText(Input in) {
        var umlText = in.toString();
        if (umlText.length() > 0 && umlText.charAt(0) != '@') {
            umlText = "@startuml\n" +
                umlText +
                "@enduml\n";
        }
        return umlText;
    }
}