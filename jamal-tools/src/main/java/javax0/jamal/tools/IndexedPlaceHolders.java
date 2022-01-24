package javax0.jamal.tools;

import java.util.Arrays;

/**
 * Replace the placeholders to the actual values in strings. The placeholders are searched from left to right in the
 * string and are replaced one after the other. The string that gets into the place of the placeholder will not be
 * searched for additional placeholders.
 * <p>
 * Placeholders are specified as strings and the replacement can be specified either as  string or as a string supplier.
 * The supplier may throw an exception.
 */
public class IndexedPlaceHolders {

    private final GenericTrie<Integer> trie = new GenericTrie<>();

    /**
     * The simple implementation of the placeholder functionality that does not handle suppliers. This implementation
     * formally is the extension of the more complex one because and not the other way around. The whole hocus pocus
     * with the class extensions is to let two different {@link IndexedPlaceHolders#format(String, Value...)} replace()}
     * methods. One that throws exception and the other one that does not.
     *
     * @param keys the key of the placeholder
     * @return a placeholder implementation
     */
    public static IndexedPlaceHolders with(String... keys) {
        return new IndexedPlaceHolders(keys);
    }

    private IndexedPlaceHolders(String... keys) {
        for (int i = 0; i < keys.length; i++) {
            this.trie.put(keys[i], i);
        }
    }

    public interface ThrowingStringSupplier {
        String get() throws Exception;
    }

    public static class Value {
        private String value;
        private final ThrowingStringSupplier supplier;

        public Value(final String value, final ThrowingStringSupplier supplier) {
            this.value = value;
            this.supplier = supplier;
        }

        public String get() throws Exception {
            if (value == null) {
                value = supplier.get();
            }
            return value;
        }
    }

    public static Value value(String value) {
        return new Value(value, null);
    }

    public static Value value(ThrowingStringSupplier supplier) {
        return new Value(null, supplier);
    }

    public Value[] values(Value... vs) {
        return vs;
    }

    /**
     * Replace the placeholders in the string {@code format} with the values from the maps and return the result.
     * <p>
     * The placeholders and the values are stored in two maps. The keys in the maps are the placeholder strings, and the
     * values are used to get the string to replace the placeholders with.
     * <p>
     * One map contains strings as values. The other one contains string suppliers. These suppliers are NOT {@link
     * java.util.function.Supplier}s because they may throw an exception. Any of the suppliers are invoked only if there
     * is a need for them. This is an important feature, because it means that there will be no exception in case the
     * placeholder to be replaced by the result of the supplier is not used even if the supplier would throw an
     * exception.
     * <p>
     * Any of the suppliers are invoked at most once, when they are referenced the first time. After that the result is
     * stored in the string value map and used from there. Consecutive invocation of {@code format()} will use the
     * already calculated value.
     * <p>
     * Placeholder replacement starts from the left to the right and replacement texts are not scanned for placeholders.
     * For example:
     *
     * <pre>{@code
     *   PlaceHolders.of("$a", "$b", "$b", "$a").format("$a $b")
     * }</pre>
     * <p>
     * will become
     *
     * <pre>{@code
     * "$b $a"
     * }</pre>
     *
     * @param original the format string with the placeholders
     * @param values   the values to replace the placeholders with
     * @return the formatted string
     * @throws Exception if one of the evaluated supplier throws exception
     */
    public String format(final String original, Value... values) throws Exception {
        StringBuilder sb = new StringBuilder(original);
        int doneIndex = 0;
        while (true) {
            final var found = trie.find(sb, doneIndex);
            if (found.isPresent()) {
                final String actualValue = values[found.get().value].get();
                sb.replace(found.get().start, found.get().end, actualValue);
                doneIndex = found.get().start + actualValue.length();
            } else {
                break;
            }
        }
        return sb.toString();
    }

    public String format(final String original, final String... values) throws Exception {
        return format(original, Arrays.stream(values).map(s -> value(s)).toArray(Value[]::new));
    }
}
