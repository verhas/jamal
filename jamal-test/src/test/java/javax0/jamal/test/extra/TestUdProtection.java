package javax0.jamal.test.extra;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestUdProtection {

    @Test
    void worksWithSimpleReplace() throws Exception {
        TestThat.theInput("{@sep [ ]}[@define a(x)=[@define b={x}]][@sep]{a/z}{b}").results("{z}");
    }

    @Test
    void verbatimShowsTheEscapeMacrosInserted() throws Exception {
        TestThat.theInput("{@sep [ ]}[@define a(x)=[@define b={x}]][@sep]{a/z}{#verbatim b}").results("{@escape `a`{`a`}z{@escape `a`}`a`}");
    }

    @Test
    void worksWhenSeparatorContainsOther() throws Exception {
        TestThat.theInput("{@sep [ ]}[#define a(x)=[`@define b=[@sep { }][x]{@sep}]]" +
            "[@sep]{@sep [[ ]]}[[a/z]][[@define z=apple]][[!b]]").results("apple");
    }

    @Test
    void escapeDoesNotWorkWithParametersInside() throws Exception {
        TestThat.theInput("{@sep [ ]}[#define a(x)=[`@define b=[@escape`.`[x]`.`]]]" +
            "[@sep]{@sep [[ ]]}[[a/z]][[b]]").throwsBadSyntax();
    }

    @Test
    void escapeSaves() throws Exception {
        TestThat.theInput("{@sep [ ]}[#define a(x)=[`@define b=[@escape`.`[`.`]x[@escape`.`]`.`] ] ]" +
            "[@sep]{@sep [[ ]]}[[a/z]][[b]]").results(" [z] ");
    }

    @Test
    void worksWhenSeparatorContainsOtherVerbatim() throws Exception {
        TestThat.theInput("{@sep [ ]}[#define a(x)=[`@define b=[@sep { }][x]{@sep}]]" +
            "[@sep]{@sep [[ ]]}[[a/z]][[@define z=apple]][[#verbatim b]]").results("[[@sep { }]][[z]]{@sep}");
    }
}
