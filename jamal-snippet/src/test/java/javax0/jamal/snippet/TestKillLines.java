package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestKillLines {

    @Test
    @DisplayName("kill the lines by default that contain only space")
    void killEmptyLines() throws Exception {
        TestThat.theInput("{@killLines \n" +
            "\na\n\na\n\n\na\n\n" +
            "}").results("a\na\na\n");
    }

    @Test
    @DisplayName("kill the lines by default that contain pattern")
    void killMatchingLinesLines() throws Exception {
        TestThat.theInput("{#killLines {@define pattern=a+}" +
            "birce\n" +
            "a\n" +
            "hurca\n" +
            "abba\n" +
            "\n" +
            "nousea\n" +
            "aaaaaaaaa\n" +
            "\n" +
            "}").results(
            "birce\n" +
                "\n" +
                "\n"

        );
    }

    @Test
    @DisplayName("does not fail when there is nothing to kill")
    void noKill() throws Exception {
        TestThat.theInput("{#killLines {@define pattern=abrakadabra}" +
            "birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa\n\n}")
            .results("birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa\n\n");
    }

    @Test
    @DisplayName("keeps the last line when not killing any line")
    void noKillTerminatingNoNl() throws Exception {
        TestThat.theInput("{#killLines {@define pattern=abrakadabra}" +
            "birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa}")
            .results("birce\na\nhurca\nabba\n\nnousea\naaaaaaaaa");
    }
}
