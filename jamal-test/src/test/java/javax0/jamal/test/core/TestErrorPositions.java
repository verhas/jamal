package javax0.jamal.test.core;

import javax0.jamal.api.BadSyntax;
import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestErrorPositions {

    @Test
    void testSimpleErrors() throws Exception {
        TestThat.theInput("{@options failfast}{@define gulp={zumi}}{gulp {@define verbatim zumi=imuz}}").throwsBadSyntax("define 'verbatim' has no '=' to body.*");
    }

    @Test
    void errorInMacroParameter() throws Exception {
        try {
            //                          1111111111222222222233333333334444444444555555555566666666667777
            //                 1234567890123456789012345678901234567890123456789012345678901234567890123
            TestThat.theInput("{@options failfast}{@define gulp(1,2,3)={zumi123}}{gulp /one/tw{o}/three}").results("");
        } catch (final BadSyntax bs) {
            final var pos = bs.getMessage().indexOf("at null[gulp>o]/");
            final var posMessage = bs.getMessage().substring(pos);
            Assertions.assertEquals("at null[gulp>o]/1:61", posMessage);
        }
    }

    @Test
    void errorInPostMacroEvaluation() throws Exception {
        //                          11111111112222222222333333333344444444445555555555666666666677777777778888888888999999999900000
        //                 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
        TestThat.theInput("{@options failfast}{@define gulp(1,2,3)={zumi123}}{@define zumione=kakk}{gulp /one/{@ident {two}}/three}").throwsBadSyntax("User macro '\\{two ...' is not defined. Did you mean '@try'.*");
    }

    @Test
    void testErrorPosition() throws Exception {
        try{
        TestThat.theInput("{@define a={uppsi}}{@define b={a}}{b}").atPosition("baba.adoc.jam", 1, 1).results("");
        }catch (final BadSyntax bs) {
            // asserting that is throws with the support methods checks up to the " at" and we need here to check the position output
         Assertions.assertEquals("User macro '{uppsi ...' is not defined. Did you mean '@use', '@pos'? at baba.adoc.jam[b>a>uppsi]/1:40",bs.getMessage());
        }
    }

}
