package javax0.jamal.api;

/**
 * Something that is usually defined by the user and as such has an identifier.
 * <p>
 * The typical user defined things are
 *
 * <ul>
 *     <li>user defined macros</li>
 *     <li>user defined scripts</li>
 *     <li>Built-in macros</li>
 * </ul>
 * <p>
 * Some macros also create objects implementing this interface when they want to store the object in the macro register.
 * Macro register is essentially the only place where a built-in macro can store and later retrieve an object, because
 * there is no guarantee that the macro instances are not shared by different processors or that a processor uses only
 * a single instance of a macro class.
 * <p>
 * This interface also defines the name of the "default" macro, and the "$macro" and "$_" names.
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
