package javax0.jamal.mock;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.JamalTests;
import javax0.jamal.testsupport.JamalYamlTest;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;

public class TestMock {

    @TestFactory
    @DisplayName("Test the different mock functionalities")
    JamalTests<?> testMocks() {
        return JamalYamlTest.factory(
                "TestMocks");
    }

    @Test
    void test() throws BadSyntax, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        TestThat.theInput("{@mock (macro=w repeat=2 when=\".*bee.*\")bee}{@mock (macro=w repeat=2 when=\".*apple.*\")apple}{@w this is an apple} {@w this is a bee}").results(
                "apple bee");
    }

}
