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
            final var pos = bs.getMessage().indexOf("at null/");
            final var posMessage = bs.getMessage().substring(pos);
            Assertions.assertEquals("at null/1:61 <<< null/1:58", posMessage);
        }
    }

    @Test
    void errorInPostMacroEvaluation() throws Exception {
        //                          11111111112222222222333333333344444444445555555555666666666677777777778888888888999999999900000
        //                 12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234
        TestThat.theInput("{@options failfast}{@define gulp(1,2,3)={zumi123}}{@define zumione=kakk}{gulp /one/{@ident {two}}/three}").throwsBadSyntax("User defined macro '\\{two ...' is not defined. Did you mean '@try'.*");
    }
}
