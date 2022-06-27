package javax0.jamal.api;

/**
 * Objects that want to keep track of how many times they were used implement this interface.
 */
public interface Counted {
    /**
     * Count one use of the object.
     */
    void count();

    /**
     * Return how many times the object was used.
     * @return the use counter.
     */
    long counted();
}
