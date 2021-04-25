package javax0.jamal.yaml;

import javax0.jamal.testsupport.TestThat;
import org.junit.jupiter.api.Test;

public class TestResolve {


    @Test
    void testClonedResolve() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "b: this is a.b}" +
            "{#yaml:define b=\n" +
            "b: this is b.b\n" +
            "refa: {@yaml:ref a}}" +
            "{#yaml:define c=\n" +
            "b: this is c.b\n" +
            "refb: {@yaml:ref b}}" +
            "This is c before resolving\n" +
            "{@verbatim c}\n" +
            "{@yaml:resolve (clone) c}\n" +
            "This is c after resolving\n" +
            "{@verbatim c}\n" +
            "This is b after resolving\n" +
            "{@verbatim b}\n" +
            ""
        ).results("This is c before resolving\n" +
            "b: this is c.b\n" +
            "refb: !!javax0.jamal.api.Ref {id: b}\n" +
            "\n" +
            "\n" +
            "This is c after resolving\n" +
            "b: this is c.b\n" +
            "refb:\n" +
            "  b: this is b.b\n" +
            "  refa: {b: this is a.b}\n" +
            "\n" +
            "This is b after resolving\n" +
            "b: this is b.b\n" +
            "refa: !!javax0.jamal.api.Ref {id: a}\n" +
            "\n");
    }

    @Test
    void testClonedResolveDeep() throws Exception {
        char last = 'a';
        final var sb = new StringBuilder();
        for (char current = 'b'; current <= 'e'; current++) {
            sb.append("{#yaml:define "
                + current
                + "=\nb: this is "
                + current
                + ".b\nrefa: {#yaml:ref "
                + last
                + "}}");
            last = current;
        }
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "b: this is a.b}" +
            sb +
            "This is e before resolving\n" +
            "{@verbatim e}\n" +
            "{@yaml:resolve (clone) e}\n" +
            "This is a after resolving\n" +
            "{@verbatim a}\n" +
            "This is b after resolving\n" +
            "{@verbatim b}\n" +
            "This is c after resolving\n" +
            "{@verbatim c}\n" +
            "This is d after resolving\n" +
            "{@verbatim d}\n" +
            "This is e after resolving\n" +
            "{@verbatim e}\n" +
            ""
        ).results("This is e before resolving\n" +
            "b: this is e.b\n" +
            "refa: !!javax0.jamal.api.Ref {id: d}\n" +
            "\n" +
            "\n" +
            "This is a after resolving\n" +
            "{b: this is a.b}\n" +
            "\n" +
            "This is b after resolving\n" +
            "b: this is b.b\n" +
            "refa: !!javax0.jamal.api.Ref {id: a}\n" +
            "\n" +
            "This is c after resolving\n" +
            "b: this is c.b\n" +
            "refa: !!javax0.jamal.api.Ref {id: b}\n" +
            "\n" +
            "This is d after resolving\n" +
            "b: this is d.b\n" +
            "refa: !!javax0.jamal.api.Ref {id: c}\n" +
            "\n" +
            "This is e after resolving\n" +
            "b: this is e.b\n" +
            "refa:\n" +
            "  b: this is d.b\n" +
            "  refa:\n" +
            "    b: this is c.b\n" +
            "    refa:\n" +
            "      b: this is b.b\n" +
            "      refa: {b: this is a.b}\n" +
            "\n");
    }

    @Test
    void testNormalResolve() throws Exception {
        TestThat.theInput("" +
            "{@yaml:define a=\n" +
            "a: this is a.a\n" +
            "b: this is a.b}" +
            "{@yaml:define b=\n" +
            "a: this is b.a\n" +
            "b: this is b.b\n" +
            "refa: !!javax0.jamal.api.Ref a}" +
            "{@yaml:define c=\n" +
            "a: this is c.a\n" +
            "b: this is c.b\n" +
            "refb: !!javax0.jamal.api.Ref b}" +
            "This is c before resolving\n" +
            "{@verbatim c}\n" +
            "{@yaml:resolve c}\n" +
            "This is c after resolving\n" +
            "{@verbatim c}\n" +
            "This is b after resolving\n" +
            "{@verbatim b}\n" +
            ""
        ).results("" +
            "This is c before resolving\n" +
            "a: this is c.a\n" +
            "b: this is c.b\n" +
            "refb: !!javax0.jamal.api.Ref {id: b}\n" +
            "\n" +
            "\n" +
            "This is c after resolving\n" +
            "a: this is c.a\n" +
            "b: this is c.b\n" +
            "refb:\n" +
            "  a: this is b.a\n" +
            "  b: this is b.b\n" +
            "  refa: {a: this is a.a, b: this is a.b}\n" +
            "\n" +
            "This is b after resolving\n" +
            "a: this is b.a\n" +
            "b: this is b.b\n" +
            "refa: {a: this is a.a, b: this is a.b}\n" +
            "\n"
        );
    }

    @Test
    void testCloneLoopResolve() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {@yaml:ref a}}\n" +
            "a before resolve\n" +
            "{@verbatim a}\n" +
            "{@yaml:resolve (clone) a}\n" +
            "a after resolve\n" +
            "{@verbatim a}" +
            ""
        ).results("" +
            "\n" +
            "a before resolve\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: !!javax0.jamal.api.Ref {id: a}\n" +
            "\n" +
            "\n" +
            "a after resolve\n" +
            "&id001\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: *id001\n"
        );
    }

    @Test
    void testNoCloneLoopResolve() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: {@yaml:ref a}}\n" +
            "a before resolve\n" +
            "{@verbatim a}\n" +
            "{@yaml:resolve a}\n" +
            "a after resolve\n" +
            "{@verbatim a}" +
            ""
        ).results("" +
            "\n" +
            "a before resolve\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: !!javax0.jamal.api.Ref {id: a}\n" +
            "\n" +
            "\n" +
            "a after resolve\n" +
            "&id001\n" +
            "a: this is a\n" +
            "b: this is b\n" +
            "c: *id001\n"
        );
    }

    @Test
    void testWrongResolve() throws Exception {
        TestThat.theInput("" +
            "{@define a=13}" +
            "{#yaml:define b=\n" +
            "a: 1\n" +
            "b: {@yaml:ref a}}" +
            "{@yaml:resolve b}"
        ).throwsBadSyntax("The user defined macro 'a' is not a YAML structure");
    }

    @Test
    void testRecursiveCopyResolver() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=[ a1, a2, a3, {@yaml:ref c}]}" +
            "{#yaml:define b=[ b1, b2, b3, {@yaml:ref a}]}" +
            "{#yaml:define c=[ c1, c2, c3, {@yaml:ref b}]}" +
            "{@yaml:output (copy) c}"
        ).results("&id001\n" +
            "- c1\n" +
            "- c2\n" +
            "- c3\n" +
            "- - b1\n" +
            "  - b2\n" +
            "  - b3\n" +
            "  - - a1\n" +
            "    - a2\n" +
            "    - a3\n" +
            "    - *id001\n");
    }

    @Test
    void testRecursiveCopyResolverCopy() throws Exception {
        TestThat.theInput("" +
            "{#yaml:define a=[ a1, {@yaml:ref c}, {@yaml:ref c} ]}" +
            "{#yaml:define b=[ b1, {@yaml:ref a}]}" +
            "{#yaml:define c=[ c1, {@yaml:ref b}]}" +
            "{@yaml:output (copy) c}"
        ).throwsBadSyntax("There is a recursive data structure while using the copying resolution.");
    }

}
