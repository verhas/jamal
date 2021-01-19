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
    @DisplayName("Trims also vertical deleting empty lines from the start and from the end")
    void trimsVertical() throws Exception {
        TestThat.theInput("{#trimLines\n" +
            "{@options trimVertical}\n" +
            "\n" +
            "\n" +
            "\n" +
            "      for i in range(1,211):\n" +
            "        print \"hi\"\n" +
            "        print \"low\"\n" +
            "\n" +
            "\n" +
            "\n" +
            "}").results("for i in range(1,211):\n" +
            "  print \"hi\"\n" +
            "  print \"low\"\n"
        );
    }

    @Test
    @DisplayName("Empties the lines that contain only spaces")
    void trimsEmptySpaceLines() throws Exception {
        TestThat.theInput("{@trimLines                                    \n" +
            "                     \n" +
            "      for i in range(1,211):               \n" +
            "                               \n" +
            "        print \"hi\"\n" +
            "        print \"low\"" +
            "}").results(
            "\n" +
                "for i in range(1,211):\n" +
                "\n" +
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
    @DisplayName("When needed inserts space to have the margin")
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
