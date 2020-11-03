package javax0.jamal.cmd;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestJamalMain {

    private JamalMain sut;

    @Test
    @DisplayName("Command line works without arguments")
    public void testCompilingEmtpyFile() {
        JamalMain.main(new String[0]);
    }

}
