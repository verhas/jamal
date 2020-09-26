package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

public class TestSep {

    @ParameterizedTest(name = "The input \"{0}\" results \"{1}\"")
    @CsvSource(value = {
        "{@sep/[/ ] }/[/@define a=aaa]/[/a],aaa",
        "{@sep/[/ ]}/[/@define a=aaa]/[/a],aaa",
        "{@sep [/]}[@define a=aaa][a],aaa",
        "{@sep [ ]}[@define a=aaa][a],aaa",
        "{@sep [ ] }[@define a=aaa][a],aaa",
        "{@sep [   ] }[@define a=aaa][a],aaa",
        "{@sep    [   ]     }[@define a=aaa][a],aaa",
        "{@sep/[/]}[@define a=aaa][a],aaa",
        "{@sep/[ / ] }[@define a=aaa][a],aaa",
        "{@sep/ [ / ] }[@define a=aaa][a],aaa",
        "{@sep    [[   ]]     }[[@define a=aaa]][[a]],aaa",
        "{@sep    [[  ]]     }[[@define a=aaa]][[a]],aaa",
        "{@sep [/]}[#sep]{@define a=aaa}{a},aaa",
        "{@sep [/]}[#sep    ]{@define a=aaa}{a},aaa",
        "{@sep /[ [/] ]}[ [@define a=aaa] ][ [a] ],aaa"
    })
    void testValidSepSyntaxes(final String source, final String result) throws Exception {
        TestThat.theInput(source).results(result);
    }


    @ParameterizedTest(name = "The input \"{0}\" throws BadSyntax")
    @ValueSource(strings = {
        "{@sep [[}",
        "{@sep [}",
        "{@sep [[]}",
        "{@sep [[[}",
        "{@sep [/[}",
        "{@sep [ [}",
        "{@sep [ [ }",
        "{@sep [. [. }",
        "{@sep /[/]/}",
    })
    void test1(final String source) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestThat.theInput(source).throwsBadSyntax();
    }
}
