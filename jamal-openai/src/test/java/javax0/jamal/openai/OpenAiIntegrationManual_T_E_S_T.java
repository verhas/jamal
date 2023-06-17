package javax0.jamal.openai;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.TestFactory;

/**
 * These tests call out to the openAI API services, therefore they are NOT unit tests.
 * <p>
 * Also, there is no practical way to check the output.
 * These tests are here for occasional manual checks.
 * <p>
 * This is the reason why the name of the class is not TestSomething, so it is not picked up by the command line.
 */
class OpenAiIntegrationManual_T_E_S_T {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
                "TestOpenAI"
        );
    }
}
