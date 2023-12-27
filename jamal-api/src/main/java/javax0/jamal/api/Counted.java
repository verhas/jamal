package javax0.jamal.api;

/**
 * Objects that want to keep track of how many times they were used implement this interface. The
 * {@link UserDefinedMacro} class implements this interface. It is used to display a warning when a user defined
 * macro is declared but is never used in a scope, which is not the global scope. Giving warning on the global scope
 * would result too many warning for the imported macros that come in a package, and the actual file may only use some
 * of them.
 * <p>
 * On the other hand, a macro defined in a local scope and not used is usually an error and the warning is justified.
 */
public interface Counted {
    /**
     * Count one use of the object.
     */
    void count();

    /**
     * Return how many times the object was used.
     *
     * @return the use counter.
     */
    long counted();
}
