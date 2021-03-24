package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestJavaMacro {

    @Test
    @DisplayName("Class is found and formatted")
    void testClass() throws Exception {
        TestThat.theInput("{@java:class " + TestJavaMacro.class.getCanonicalName() + "}").results(TestJavaMacro.class.getSimpleName());
        TestThat.theInput("{@java:class (format=\"$canonicalName\") " + TestJavaMacro.class.getCanonicalName() + "}").results(TestJavaMacro.class.getCanonicalName());
    }

    @Test
    @DisplayName("Class is not found and throws")
    void testClassNotFound() throws Exception {
        TestThat.theInput("{@java:class com.javax0.nonexistentClass}").throwsBadSyntax();
    }

    @Test
    @DisplayName("Method is found and formatted")
    void testMethod() throws Exception {
        TestThat.theInput("{@java:method javax0.jamal.snippet.TestJavaMacro#testMethod}").results("testMethod");
        TestThat.theInput("{@java:method javax0.jamal.snippet.TestJavaMacro::testMethod}").results("testMethod");
        TestThat.theInput("{@java:method (format=$modifiers) javax0.jamal.snippet.TestJavaMacro::testMethod}").results("");
        TestThat.theInput("{@java:method (format=$className) javax0.jamal.snippet.TestJavaMacro::testMethod}").results(TestJavaMacro.class.getName());
    }

    @Test
    @DisplayName("Method is not found and throws")
    void testBadMethod() throws Exception {
        TestThat.theInput("{@java:method javax0.jamal.snippet.TestiJavaMacro#testBadMethod}").throwsBadSyntax();
        TestThat.theInput("{@java:method javax0.jamal.snippet.TestJavaMacro#testMethodBid}").throwsBadSyntax();
    }

    private int testField;
    private static final boolean testBulean = true;

    @Test
    @DisplayName("Field is found and formatted")
    void testField() throws Exception {
        TestThat.theInput("{@java:field javax0.jamal.snippet.TestJavaMacro#testField}").results("testField");
        TestThat.theInput("{@java:field `\n`javax0.jamal.snippet.TestJavaMacro\ntestField}").results("testField");
        TestThat.theInput("{@java:field (format=$value)javax0.jamal.snippet.TestJavaMacro#testBulean}").results("true");
    }

    @Test
    @DisplayName("Method is not found throws")
    void testBadField() throws Exception {
        TestThat.theInput("{@java:field javax0.jamal.snippet.TestiJavaMacro#testField}").throwsBadSyntax();
        TestThat.theInput("{@java:field javax0.jamal.snippet.TestJavaMacro#testFeld}").throwsBadSyntax();
        TestThat.theInput("{@java:field javax0.jamal.snippet.TestJavaMacro}").throwsBadSyntax();
        TestThat.theInput("{@java:field /javax0.jamal.snippet.TestJavaMacro}").throwsBadSyntax();
        TestThat.theInput("{@java:field (format=$value)javax0.jamal.snippet.TestJavaMacro#testField}").throwsBadSyntax();
    }

}
