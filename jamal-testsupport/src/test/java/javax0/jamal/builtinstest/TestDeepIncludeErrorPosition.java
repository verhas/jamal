package javax0.jamal.builtinstest;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TestDeepIncludeErrorPosition {
    @Test
    @DisplayName("The exception message position part is hierarchical.")
    void test() throws Exception {
        try {
            TestThat.theInput("{@include res:inc1.jim}").atPosition("baba",1,1).results("");
        }catch (BadSyntax bs){
            Assertions.assertEquals("User macro '{this ...' is not defined. Did you mean 'thus'? at res:inc4.jim/1:2"
                    , bs.getMessage());
        }
    }
}
