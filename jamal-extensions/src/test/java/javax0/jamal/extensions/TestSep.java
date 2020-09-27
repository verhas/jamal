package javax0.jamal.extensions;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.lang.reflect.InvocationTargetException;

public class TestSep {

    @ParameterizedTest(name = "The input \"{0}\" results \"{1}\"")
    @CsvSource(value = {
        "{@sep [/]}[@define a=aaa][a],aaa",
        "{@sep[]}[@define a=aaa][a],aaa",
        "{@sep []}[@define a=aaa][a],aaa",
        "{@sep [] }[@define a=aaa][a],aaa",
        "{@sep [ ]}[@define a=aaa][a],aaa",
        "{@sep [ ] }[@define a=aaa][a],aaa",
        "{@sep [   ] }[@define a=aaa][a],aaa",
        "{@sep    [   ]     }[@define a=aaa][a],aaa",
        "{@sep/[/]}[@define a=aaa][a],aaa",
        "{@sep/[ / ] }[@define a=aaa][a],aaa",
        "{@sep/ [ / ] }[@define a=aaa][a],aaa",
        "{@sep ((   )) }((@define a=aaa))((a)),aaa",
        "{@sep (((   ))) }(((@define a=aaa)))(((a))),aaa",
        "{@sep .*wuff!!   *.Wuff!! }.*wuff!!@define a=aaa*.Wuff!!.*wuff!!a*.Wuff!!,aaa",
        "{@sep !*wuff!!   !*Wuff!! }!*wuff!!@define a=aaa!*Wuff!!!*wuff!!a!*Wuff!!,aaa",
        "{@sep/((/))}((@define a=aaa))((a)),aaa",
        "{@sep    [[   ]]     }[[@define a=aaa]][[a]],aaa",
        "{@sep    [[  ]]     }[[@define a=aaa]][[a]],aaa",
        "{@sep [/]}[#sep]{@define a=aaa}{a},aaa",
        "{@sep [/]}[#sep    ]{@define a=aaa}{a},aaa",
        "{@sep /[ [/] ]}[ [@define a=aaa] ][ [a] ],aaa"
    })
    void testValidSepSyntaxes(final String source, final String result) throws Exception {
        TestThat.theInput(source).results(result);
    }


    @ParameterizedTest(name = "The input \"{0}\" throws BadSyntax, because {1}")
    @CsvSource(value = {
        "{@sep/[/ ] },the opening string is misleading",
        "{@sep /[/ ] },the opening string is misleading",
        "{@sep/[/ ]},the opening string is misleading",
        "{@sep/[ /]},the closing string is misleading",
        "{@sep [[},the opening and closing string is the same",
        "{@sep [},there is only one character",
        "{@sep [[]},three characters and the separator is the same as the opening string",
        "{@sep [[[},three characters and the separator is the same as the opening string and the closing string",
        "{@sep [/[},the openaing and closing strings are the same",
        "{@sep [ [},the opening and closing strings are the same",
        "{@sep [ [ },the opening and closing strings are the same",
        "{@sep [. [. },the opening and closing strings are the same",
        "{@sep /[/]/},the closing string contains the separator character",
    })
    void test1(final String source, final String reason) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        TestThat.theInput(source).throwsBadSyntax();
    }
}
