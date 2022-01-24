package javax0.jamal.tools;

import java.util.Optional;

public class GenericTrie<T> {
    private final Node<T> root = new Node<>();
    private static final int OFFSET = '!';

    private static class Node<T> {
        T value;
        boolean leaf = false;
        final private Node<T>[] letters = (Node<T>[]) new Node['~' - OFFSET + 1];
    }

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

    public Optional<Result<T>> find(CharSequence key) throws Exception {
        return find(key, 0);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        _toString("", sb, root);
        return sb.toString();
    }

    private void _toString(String prefix, StringBuilder sb, Node<T> node) {
        if( node.leaf){
            sb.append(prefix).append(" -> ").append(node.value).append("\n");
        }else{
            for (int i = 0; i < node.letters.length; i++) {
                if( node.letters[i] != null ){
                    _toString(prefix + (char)(i + OFFSET), sb, node.letters[i]);
                }
            }
        }
    }

    public Optional<Result<T>> find(CharSequence key, int start) throws Exception {
        final var chars = key.toString().toCharArray();
        while (start < key.length()) {
            int end = start;
            Node<T> node = root;
            while (node != null) {
                if (end >= chars.length || node.leaf) {
                    if (!node.leaf) {
                        return Optional.empty();
                    } else {
                        return Optional.of(new Result<>(start, end, node));
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

    public T get(String key) throws Exception {
        return find(key).map(f -> f.value).orElse(null);
    }

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
}
