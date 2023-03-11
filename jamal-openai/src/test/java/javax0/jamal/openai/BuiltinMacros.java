package javax0.jamal.openai;

import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * These tests call out to the openAI API services, therefore they are NOT unit tests.
 *
 * Also, there is no practical way to check the output.
 * These tests are here for occasional manual checks.
 *
 * This is the reason why the name of the class is not TestSomething, so it is not picked up by the command line.
 */
class BuiltinMacros {

    @TestFactory
    JamalTests<?> testAll() {
        return JamalYamlTest.factory(
                "TestOpenAI"
        );
    }
}
