package javax0.jamal.cmd;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.engine.Processor;
import javax0.jamal.tools.Input;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestJamalMain {

    private JamalMain sut;

    @Test
    @DisplayName("Command line works with some simple example")
    public void testCompilingEmtpyFile() throws BadSyntax {
        JamalMain.main(new String[]{"-v", "--file", "pom.xml.jam", "pom.xmla" });
        final var proc = new Processor("{","}");
        final var result = proc.process(Input.makeInput("{@include res:test}"));
        Assertions.assertEquals("1",result);
    }

}
