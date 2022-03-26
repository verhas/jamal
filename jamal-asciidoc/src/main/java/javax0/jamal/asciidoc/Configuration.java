package javax0.jamal.asciidoc;

import javax0.jamal.api.EnvironmentVariables;

public class Configuration {
    public static final Configuration INSTANCE = new Configuration();
    public final String macroOpen;
    public final String macroClose;
    public final boolean nosave;
    public final boolean log;

    private Configuration() {
        macroOpen = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_OPEN").orElse("{%");
        macroClose = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_CLOSE").orElse("%}");
        nosave = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_NOSAVE").map(Boolean::parseBoolean).orElse(false);
        log = EnvironmentVariables.getenv("ASCIIDOC_EXTENSION_LOG").map(Boolean::parseBoolean).orElse(false);
    }
}
