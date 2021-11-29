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
            TestThat.theInput("{@include res:inc1.jim}").results("");
        }catch (BadSyntax bs){
            Assertions.assertEquals("User defined macro '{this ...' is not defined. at res:inc4.jim/1:2 <<< res:inc3.jim/1:10 <<< res:inc2.jim/1:10 <<< res:inc1.jim/1:10 <<< null/1:10"
                    , bs.getMessage());
        }
    }
}
