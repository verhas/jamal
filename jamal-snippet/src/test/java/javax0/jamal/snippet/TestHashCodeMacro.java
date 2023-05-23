package javax0.jamal.snippet;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestHashCodeMacro {

    @Test
    void test() throws Exception {
        TestThat.theInput("{@hashCode apple fell down the tree}")
                //                 0000000001111111111222222222233333333334444444444555555555566666
                //                 1234567890123456789012345678901234567890123456789012345678901234
                .results("7ad9fd341363d3c26f40cb296a52e3af630ef8fcc1352ca3ceac451237cdf242");
    }

}
