package javax0.jamal.tools;

import java.util.Optional;

public class Trie {
    private final Node root = new Node();
    private static final int OFFSET = '!';

    public interface ThrowingStringSupplier {
        String get();
    }

    private static class Node {
        String value;
        ThrowingStringSupplier supplier;
        boolean leaf = false;
        final private Node[] letters = new Node['~' - OFFSET + 1];
    }

    public static class Result {
        public final int start;
        public final int end;
        public final String value;

        private Result(int start, int end, Node node) {
            this.start = start;
            this.end = end;
            if (node.value == null && node.supplier != null) {
                node.value = node.supplier.get();
            }
            this.value = node.value;
        }
    }

    public Optional<Result> find(CharSequence key) throws Exception {
        return find(key, 0);
    }

    public Optional<Result> find(CharSequence key, int start) throws Exception {
        final var chars = key.toString().toCharArray();
        while (start < key.length()) {
            int end = start;
            Node node = root;
            while (node != null) {
                if (end >= chars.length || node.leaf) {
                    if (!node.leaf) {
                        return Optional.empty();
                    } else {
                        return Optional.of(new Result(start, end, node));
                    }
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

    public String get(String key) throws Exception {
        return find(key).map(f -> f.value).orElse(null);
    }

    public void put(String key, String value) {
        put(key, value, null);
    }

    public void put(String key, ThrowingStringSupplier value) {
        put(key, null, value);
    }

    private void put(String key, String value, ThrowingStringSupplier supplier) {
        final var chars = key.toCharArray();
        int i = 0;
        Node node = root;
        Node parent = null;
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
            parent.letters[index] = new Node();
            parent = parent.letters[index];
            i++;
        }
        parent.value = value;
        parent.supplier = supplier;
        parent.leaf = true;
    }
}
