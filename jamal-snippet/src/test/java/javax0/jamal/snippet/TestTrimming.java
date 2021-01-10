package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestTrimming {

    @Test
    @DisplayName("When there is no newLine after 'trimLines' nothing is trimmed")
    void trimsToLeftNothing() throws Exception {
        TestThat.theInput("{@trimLines wupbaba\n" +
            "        siiias das\n" +
            "sdsd sdsd ddd" +
            "}").results("wupbaba\n" +
            "        siiias das\n" +
            "sdsd sdsd ddd");
    }

    @Test
    @DisplayName("Trims the lines to the left")
    void trimsToLeftNeatly() throws Exception {
        TestThat.theInput("{@trimLines\n" +
            "      for i in range(1,211):\n" +
            "        print \"hi\"\n" +
            "        print \"low\"" +
            "}").results("for i in range(1,211):\n" +
            "  print \"hi\"\n" +
            "  print \"low\"");
    }

    @Test
    @DisplayName("Trims the lines with margin")
    void trimsToLeftWithMargin() throws Exception {
        TestThat.theInput("{#trimLines {@define margin=1}\n" +
            "      for i in range(1,211):\n" +
            "        print \"hi\"\n" +
            "        print \"low\"" +
            "}").results(" for i in range(1,211):\n" +
            "   print \"hi\"\n" +
            "   print \"low\"");
    }

    @Test
    @DisplayName("When needed inserts sace to have the margin")
    void addsMargin() throws Exception {
        TestThat.theInput("{#trimLines {@define margin=1}\n" +
            "for i in range(1,211):\n" +
            "  print \"hi\"\n" +
            "  print \"low\"" +
            "}").results(" for i in range(1,211):\n" +
            "   print \"hi\"\n" +
            "   print \"low\"");
    }
}
