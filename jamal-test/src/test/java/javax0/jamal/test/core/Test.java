package javax0.jamal.test.core;

import javax0.jamal.testsupport.JamalYamlTest;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import java.util.Collection;

public class Test {

    @TestFactory
    Collection<? extends DynamicNode> testAll() {
        return JamalYamlTest.factory(
            "TestFor",
            "TestBeginEnd",
            "TestBlock",
            "TestComment",
            "TestDefine"
        );
    }
}
