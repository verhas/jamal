package javax0.jamal.scriptbasic;

import javax0.jamal.testsupport.SentinelSmith;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestMacros {

    static SentinelSmith blade;

    @BeforeEach
    void setUp() throws Exception {
        blade = SentinelSmith.forge("basic");
    }

    @AfterAll
    static void afterAll() throws Exception {
        blade.close();
    }

    @DisplayName("Test all files that have an '.expected' pair")
    @Test
    void testExpectedFiles() throws Exception {
        TestThat.theInput(
                // snippet sample
                "{@import res:scriptbasic.jim}\n" +
                        "{expr 13+1}{@define start=1}{@define end=13}\n" +
                        "{#basic for i={start} to {end}\n" +
                        "if i%2 = 1 then\n" +
                        "  oddity = \"odd\"\n" +
                        "else\n" +
                        "  oddity = \"even\"\n" +
                        "endif\n" +
                        "print i,\". is an \",oddity,\" number\\n\"\n" +
                        "next\n" +
                        "}\n"
                // end snippet
        ).results(
                // snippet sample_output
                "\n" +
                        "14\n" +
                        "1. is an odd number\n" +
                        "2. is an even number\n" +
                        "3. is an odd number\n" +
                        "4. is an even number\n" +
                        "5. is an odd number\n" +
                        "6. is an even number\n" +
                        "7. is an odd number\n" +
                        "8. is an even number\n" +
                        "9. is an odd number\n" +
                        "10. is an even number\n" +
                        "11. is an odd number\n" +
                        "12. is an even number\n" +
                        "13. is an odd number\n" +
                        "\n");
        // end snippet
    }

}
