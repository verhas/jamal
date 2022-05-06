package javax0.jamal.doclet.sample;

/**
 * This is an empty sample class that is used to test that the jamal doclet works.
 *
 * @jamal
 * {@define a=13}{a} <-- in the generated JavaDoc you must see here 13 and nothing else in front of the arrow
 *
 */
public class Main {

    /**
     * @jamal
     * {@snip:collect from="./Main.java" onceAs="Main.java"}
     * This Javadoc contains the method content itself
     *
     * The following is the actual code of the method collected by jamal as a s{@comment}nippet and inserted into the JavaDoc by
     * the Jamal doclet.
     * <pre>
     * {@snip testSnippet}
     * </pre>
     */
    // snippet testSnippet
    public void testSnippet(){

    }
    // end snippet
}
