package javax0.jamal.markdown;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

public class TestMarkdown {

    @TestFactory
    JamalTests testMarkdown() {
        return JamalYamlTest.factory("Markdown");
    }


}
