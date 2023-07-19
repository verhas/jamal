package javax0.jamal.snippet;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

public class TestHierarchicalCounter {

    @Test
    void testSimpleTest() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@counter:define hierarchical id=c}" +
                "{c} {c}{c open} {c}{c close} {c}{c close}"
        ).results("1 2 2.1 3");
    }

    private void openClose(int level, StringBuilder sb) {
        if (level > 0) {
            sb.append("{c open}");
            sb.append("{c}\n");
            sb.append("{c}\n");
            if (level > 1) {
                openClose(level - 1, sb);
            }
            sb.append("{c}\n");
            sb.append("{c}\n");
            sb.append("{c close}");
        }
    }

    @Test
    void testExhaustive() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        final var sb = new StringBuilder("{@counter:define hierarchical id=c}");
        sb.append("{c}\n");
        openClose(3, sb);
        sb.append("{c}\n");
        openClose(3, sb);
        TestThat.theInput(sb.toString()).results("1\n" +
                "1.1\n" +
                "1.2\n" +
                "1.2.1\n" +
                "1.2.2\n" +
                "1.2.2.1\n" +
                "1.2.2.2\n" +
                "1.2.2.3\n" +
                "1.2.2.4\n" +
                "1.2.3\n" +
                "1.2.4\n" +
                "1.3\n" +
                "1.4\n" +
                "2\n" +
                "2.1\n" +
                "2.2\n" +
                "2.2.1\n" +
                "2.2.2\n" +
                "2.2.2.1\n" +
                "2.2.2.2\n" +
                "2.2.2.3\n" +
                "2.2.2.4\n" +
                "2.2.3\n" +
                "2.2.4\n" +
                "2.3\n" +
                "2.4\n");
    }

    @Test
    void testLast() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String sb = "{@counter:define hierarchical id=c}" + "{c}\n" +
                "B {c}\n" +
                "{c open}" +
                "C {c last}\n" +
                "C {c}\n" +
                "{c close}" +
                "B {c last}\n" +
                "A {c}\n" +
                "A {c last}\n";
        TestThat.theInput(sb).results("1\n" +
                "B 2\n" +
                "C 2.1\n" +
                "C 2.1\n" +
                "B 2\n" +
                "A 3\n" +
                "A 3\n");
    }

    @Test
    void testFormatting() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String sb = "{@counter:define hierarchical id=c}" +
                "{@define [verbatim] F=\"{1:$roman.}{2:$ROMAN.}{3:%03d}\"}" +
                "{c}\n" +
                "{c format={F}}\n" +
                "{c open}" +
                "{c last format={F}}\n" +
                "{c format={F}}\n" +
                "{c close}" +
                "{c last format={F}}\n" +
                "{c}" +
                "{c open}" +
                "{c open}" +
                "\n" +
                "{c last format={F}}\n";
        TestThat.theInput(sb).results(
                "1\n" +
                        "ii.\n" +
                        "ii.I.\n" +
                        "ii.I.\n" +
                        "ii.\n" +
                        "3\n" +
                        "iii.I.001\n");
    }

    @Test
    void testReset() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String sb = "{@counter:define hierarchical id=c}" +
                "{c}\n" +
                "{c}\n" +
                "{c open}" +
                "{c last}\n" +
                "{c}\n" +
                "{c close}" +
                "{c open}" +
                "{c open}" +
                "{c open}" +
                "{c open}" +
                "{c}{c reset}\n" +
                "RESET {c last}\n" +
                "{c}" +
                "{c open}" +
                "{c open}" +
                "\n" +
                "{c last}\n";
        TestThat.theInput(sb).results(
                "1\n" +
                        "2\n" +
                        "2.1\n" +
                        "2.1\n" +
                        "2.1.1.1.1\n" +
                        "RESET 1\n" +
                        "1\n" +
                        "1.1.1\n");
    }

    @Test
    void testSave() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String sb = "{@counter:define hierarchical id=c}" +
                "Q {c save=q}\n" +
                "{c}\n" +
                "{c open}" +
                "{c last}\n" +
                "{c}\n" +
                "Q {q}\n" +
                "{c close}" +
                "{c open}" +
                "{c open}" +
                "{c open}" +
                "{c open}" +
                "{c}\n";
        TestThat.theInput(sb).results(
                "Q 1\n" +
                        "2\n" +
                        "2.1\n" +
                        "2.1\n" +
                        "Q 1\n" +
                        "2.1.1.1.1\n");
    }

    @Test
    void testTitle() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        String sb = "{@counter:define hierarchical id=c}" +
                "Q {c saveAs=q title=\" First Chapter\"}\n" +
                "{c title=\" Second Chapter\"}\n" +
                "Q {q}\n" +
                "title={q format=$title}\n";
        TestThat.theInput(sb).results(
                "Q 1 First Chapter\n" +
                        "2 Second Chapter\n" +
                        "Q 1 First Chapter\n" +
                        "title= First Chapter\n");
    }
}
