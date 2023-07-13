package javax0.jamal.asciidoc;

import javax0.jamal.api.EnvironmentVariables;

public class Configuration {
    public static final Configuration INSTANCE = new Configuration();
    public final String macroOpen;
    public final String macroClose;
    public final boolean nosave;
    public final boolean log;
    public final boolean external;
    public final boolean fromFile;
    public final boolean noDependencies;
    public final String externalCommand;
    public boolean keepFrontMatter;

    private Configuration() {
        // snipline ASCIIDOC_EXTENSION_OPEN filter="(\w+)"
        macroOpen = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_OPEN").orElse("{%");
        // snipline ASCIIDOC_EXTENSION_CLOSE filter="(\w+)"
        macroClose = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_CLOSE").orElse("%}");
        // snipline ASCIIDOC_EXTENSION_NOSAVE filter="(\w+)"
        nosave = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_NOSAVE").map(Boolean::parseBoolean).orElse(false);
        // snipline ASCIIDOC_EXTENSION_LOG filter="(\w+)"
        log = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_LOG").map(Boolean::parseBoolean).orElse(false);
        // snipline ASCIIDOC_EXTENSION_EXTERNAL_COMMAND filter="(\w+)"
        externalCommand = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_EXTERNAL_COMMAND").orElse("");
        // snipline ASCIIDOC_EXTENSION_EXTERNAL filter="(\w+)"
        external = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_EXTERNAL").map(Boolean::parseBoolean).orElse(false);
        // snipline ASCIIDOC_EXTENSION_FROM_FILE filter="(\w+)"
        fromFile = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_FROM_FILE").map(Boolean::parseBoolean).orElse(false);
        // snipline ASCIIDOC_EXTENSION_NO_DEPENDENCIES filter="(\w+)"
        noDependencies = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_NO_DEPENDENCIES").map(Boolean::parseBoolean).orElse(false);
        // snipline ASCIIDOC_EXTENSION_KEEP_FRONT_MATTER filter="(\w+)"
        keepFrontMatter = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_KEEP_FRONT_MATTER").map(Boolean::parseBoolean).orElse(false);
    }
}
