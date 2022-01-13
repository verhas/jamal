package javax0.jamal.test.core;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestErrorPositions {

    @Test
    void testSimpleErrors()throws Exception{
        try {
            TestThat.theInput("{@options failfast}{@define gulp={zumi}}{gulp {@define [verbatem] zumi=imuz}}").results("");
        }catch (final BadSyntax bs){
            Assertions.assertEquals("define '[verbatem]' has no '=' to body at null/1:55",bs.getMessage());
        }
    }

    @Test
    void errorInMacroParameter()throws Exception{
        try {
            //                          1111111111222222222233333333334444444444555555555566666666667777
            //                 1234567890123456789012345678901234567890123456789012345678901234567890123
            TestThat.theInput("{@options failfast}{@define gulp(1,2,3)={zumi123}}{gulp /one/tw{o}/three}").results("");
        }catch (final BadSyntax bs){
            Assertions.assertEquals("User defined macro '{o ...' is not defined. Did you mean '@for', '@if'? at null/1:61 <<< null/1:58",bs.getMessage());
        }
    }
    @Test
    void errorInPostMacroEvaluation()throws Exception{
        try {
            //                          11111111112222222222333333333344444444445555555555666666666677777777778888888888999999999900000
            //                 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
            TestThat.theInput("{@options failfast}{@define gulp(1,2,3)={zumi123}}{@define zumione=kakk}{gulp /one/{@ident {two}}/three}").results("");
        }catch (final BadSyntax bs){
            Assertions.assertEquals("User defined macro '{two ...' is not defined. Did you mean '@try'? at null/1:89 <<< null/1:80",bs.getMessage());
        }
    }
}
