package javax0.jamal.asciidoc;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class ConvertInstallShell {

    @Test
    void installShellConversion() throws Exception {
        DocumentConverter.convert("install-asciidoc.sh.jam");
    }
}
