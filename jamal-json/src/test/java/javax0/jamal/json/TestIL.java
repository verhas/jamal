package javax0.jamal.json;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestIL {

    @Test
    @DisplayName("Test sample that causes infinte loop")
    void test() throws Exception {
     TestThat.theInput("{@json:define docker={\n" +
             "version: \"3.6\",\n" +
             "services: [\"http\",\"https\",\"jamal-debug\"],\n" +
             "zilch: { \"nada\": \"nothing\" }\n" +
             "}}\\\n" +
             "\n" +
             "{@json:add to=docker./services flat\n" +
             "[{\n" +
             "\"com.javax0.jamal.title\": \"Non-relational DB Instance\",\n" +
             "\"com.javax0.jamal.sizing\": 1000,\n" +
             "\"com.javax0.jamal.nodeType\": \"primary\"\n" +
             "}]\n" +
             "}\\\n" +
             "{docker}\n").results("\n" +
             "{\"zilch\":{\"nada\":\"nothing\"},\"services\":[\"http\",\"https\",\"jamal-debug\",{\"com.javax0.jamal.sizing\":1000,\"com.javax0.jamal.title\":\"Non-relational DB Instance\",\"com.javax0.jamal.nodeType\":\"primary\"}],\"version\":\"3.6\"}\n");
    }
}
