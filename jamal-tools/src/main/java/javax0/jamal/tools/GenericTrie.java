package javax0.jamal.tools;

import java.util.Optional;

/**
 * A simple generic Trie implementation.
 * For more information about tries visit <a href="https://en.wikipedia.org/wiki/Trie">Trie/Wikipedia</a>
 * This implementation is a pointer vector implementation.
 * The implementation is simplified, and it cannot handle the case, when a key is the prefix of another key.
 * In other words only leaf nodes can have values.
 * This makes the implementation simpler, and the intended use is to find and replace formatting placeholders in
 * strings.
 * Having a placeholder prefixing another one is not readable.
 * <p>
 * The alphabet over which the trie is navigable is the array of characters from '!' (33) to '~' (126).
 * That is the set of all printable ASCII characters except for space.
 * <p>
 * The trie stores a value for each leaf node. The type of the value if {@code T}, generic parameter.
 *
 * @param <T> the type of the value stored in the leaf nodes. YOu can store the actual values if they do not change.
 *            You can also store a reference to the value if the value changes.
 */
@SuppressWarnings("unchecked")
public class GenericTrie<T> {
    private final Node<T> root = new Node<>();
    private static final int OFFSET = '!';

    private static class Node<T> {
        T value;
        boolean leaf = false;
        final private Node<T>[] letters = (Node<T>[]) new Node['~' - OFFSET + 1];
    }

    /**
     * A result object contains the {@code start} (inclusive) and {@code end} (exclusive) indexes of the found string.
     * It also contains the value attached to the leaf node.
     *
     * @param <T> the type of the value stored in the leaf nodes.
     */
    public static class Result<T> {
        public final int start;
        public final int end;
        public final T value;

        private Result(int start, int end, Node<T> node) {
            this.start = start;
            this.end = end;
            this.value = node.value;
        }
    }

    /**
     * Find any of the key in the text.
     *
     * @param text in which we search for the keys
     * @return the optional Result object if the key is found or empty if not found.
     */
    public Optional<Result<T>> find(CharSequence text) {
        return find(text, 0);
    }

    /**
     * Find any of the key in the text starting at the given index.
     *
     * @param text  in which we search for the keys
     * @param start the start index
     * @return the optional Result object if the key is found or empty if not found.
     */
    public Optional<Result<T>> find(CharSequence text, int start) {
        final var chars = text.toString().toCharArray();
        while (start < text.length()) {
            int end = start;
            Node<T> node = root;
            while (node != null) {
                if (node.leaf) {
                    return Optional.of(new Result<>(start, end, node));
                }
                if (end >= chars.length) {
                    return Optional.empty();
                }
                final var index = chars[end] - OFFSET;
                if (index >= 0 && index < node.letters.length) {
                    node = node.letters[index];
                } else {
                    node = null;
                }
                end++;
            }
            start++;
        }
        return Optional.empty();
    }

    /**
     * Find a key in the text and return the value.
     *
     * @param text in which we search for the keys
     * @return the value associated with the key found in the text or null if a key is not found.
     */
    public T get(String text) {
        return find(text).map(f -> f.value).orElse(null);
    }

    /**
     * Put the key into the trie with the value.
     *
     * @param key   the key that will be searched in the text
     * @param value the vaue associated with the key
     */
    public void put(String key, T value) {
        final var chars = key.toCharArray();
        int i = 0;
        Node<T> node = root;
        Node<T> parent = null;
        while (node != null) {
            if (i >= chars.length) {
                throw new IllegalArgumentException("The key '" + key + "' is already in the trie.");
            }
            final var index = chars[i] - OFFSET;
            if (index < 0 || index >= node.letters.length) {
                throw new IllegalArgumentException("Characters in the string can only be between ! and ~ (33-126) in '" + key + "'");
            }
            parent = node;
            node = node.letters[index];
            if (node != null) {
                i++;
            }
        }
        if (parent.leaf) {
            throw new IllegalArgumentException("The key '" + key + "' has a prefix '" + key.substring(0, i) + "' in the trie.");
        }
        while (i < chars.length) {
            final var index = chars[i] - OFFSET;
            if (index < 0 || index >= parent.letters.length) {
                throw new IllegalArgumentException("Characters in the string can only be between ! and ~ (33-126) in '" + key + "'");
            }
            parent.letters[index] = new Node<>();
            parent = parent.letters[index];
            i++;
        }
        parent.value = value;
        parent.leaf = true;
    }

    /**
     * @return the string representation of the trie, mainly for debugging purposes.
     */
    @Override
    public String toString() {
        final var sb = new StringBuilder();
        _toString("", sb, root);
        return sb.toString();
    }

    private void _toString(String prefix, StringBuilder sb, Node<T> node) {
        if (node.leaf) {
            sb.append(prefix).append(" -> ").append(node.value).append("\n");
        } else {
            for (int i = 0; i < node.letters.length; i++) {
                if (node.letters[i] != null) {
                    _toString(prefix + (char) (i + OFFSET), sb, node.letters[i]);
                }
            }
        }
    }
}
