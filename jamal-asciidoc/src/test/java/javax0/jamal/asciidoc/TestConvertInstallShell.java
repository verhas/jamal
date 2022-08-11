package javax0.jamal.asciidoc;

import javax0.jamal.DocumentConverter;
import org.junit.jupiter.api.Test;

public class TestConvertInstallShell {

    @Test
    void installShellConversion() throws Exception {
        DocumentConverter.convert("install-asciidoc.sh.jam");
    }
}
