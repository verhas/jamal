package javax0.jamal.api;

/**
 * Something that is usually defined by the user and as such has an identifier.
 * <p>
 * The typical user defined things are
 *
 * <ul>
 *     <li>user defined macros</li>
 *     <li>user defined scripts</li>
 * </ul>
 * <p>
 * Some macros also want to implement something similar things that may or may not be accessed from the processed
 * jamal text, but are available and managed by the code.
 */
@FunctionalInterface
public interface Identified {
    /**
     * Get the string identifier of the identifiable.
     *
     * @return the string representation of the identifier. Usually a human readable name.
     */
    String getId();

    String DEFAULT_MACRO = "default";
    String MACRO_NAME_ARG1 = "$macro";
    String MACRO_NAME_ARG2 = "$_";

    /**
     * A special placeholder for Identified enties that get undefined.
     * <p>
     * When an entity (typically a user defined macro) is not defined it is nowhere in the different layers. If it were
     * defined in any fo the layers then the current layer would inherit it and then it is defined.
     * <p>
     * When we explicitly want to undefine a macro we should not delete the definition from the structure, because the
     * effect of undefining a macro should have the same locality as defining it. For this reason we insert a dummy
     * definition, which is an instance of this class.
     * <p>
     * This "undefined" definition is local, can be exported and behaves exactly the same way as a "normal" defined
     * macro.
     */
    class Undefined implements Identified {
        private final String id;

        public Undefined(String id) {
            this.id = id;
        }

        @Override
        public String getId() {
            return id;
        }
    }
}
