package javax0.jamal.asciidoc;

import javax0.jamal.api.SpecialCharacters;

import java.util.List;
import java.util.regex.Pattern;

class InFileOptions {
    // save the converted text from `xxx.jam` --> `xxx` by default
    boolean save = !Configuration.INSTANCE.nosave;
    // by default, we do not write log file
    boolean log = Configuration.INSTANCE.log;
    boolean external = Configuration.INSTANCE.external;
    boolean withoutDeps = Configuration.INSTANCE.noDependencies;
    boolean off = false;
    boolean useDefaultSeparators;
    boolean fromFile = Configuration.INSTANCE.fromFile;
    boolean dual = false;
    boolean keepFrontMatter = Configuration.INSTANCE.keepFrontMatter;

    boolean writableOutput = Configuration.INSTANCE.writableOutput;

    boolean prefixLog;

    InFileOptions(final String firstLine) {
        final var matcher = Pattern.compile("@comment\\s+([\\w\\s]*)").matcher(firstLine);
        final var options = matcher.find() ? List.of(matcher.group(1).split("\\s+")) : List.<String>of();

        // snippet OPTIONS
        if (options.contains("writableOutput")) {
            writableOutput = true;
        }
        if (options.contains("dual")) {
            dual = true;
            save = false;
        }
        if (options.contains("fromFile")) {
            fromFile = true;
        }
        if (options.contains("off")) {
            off = true;
        }
        if (options.contains("nosave") || options.contains("noSave")) {
            save = false;
        }
        if (options.contains("log")) {
            log = true;
        }
        if (options.contains("prefixLog")) {
            prefixLog = true;
        }
        if (options.contains("external")) {
            external = true;
        }
        if (options.contains("noDependencies")) {
            withoutDeps = true;
        }
        if (options.contains("keepFrontMatter")) {
            keepFrontMatter = true;
        }
        // end snippet
        useDefaultSeparators = firstLine.length() > 1 && firstLine.charAt(0) == SpecialCharacters.IMPORT_SHEBANG1 && firstLine.charAt(1) == SpecialCharacters.IMPORT_SHEBANG2;
    }
}
