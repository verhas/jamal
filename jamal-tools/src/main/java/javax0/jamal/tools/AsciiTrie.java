package javax0.jamal.tools;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class AsciiTrie<T> implements Map<String, T> {
    private final Node<T> root = new Node<>();
    private final Set<String> keys = new HashSet<>();
    private static final int OFFSET = '!';

    private static class Node<T> {
        T value;
        final private Node<T>[] letters = new Node['~' - OFFSET+1];
    }

    private static class FindResult<T> {
        private Node<T> found;
        private Node<T> parent;
        private int lastIndex;
    }

    private Optional<FindResult<T>> find(String key) {
        final var result = new FindResult<T>();
        result.found = root;
        final var chars = key.toCharArray();
        int i = 0;
        while (result.found != null) {
            if (i >= chars.length) {
                return Optional.of(result);
            }
            result.parent = result.found;
            result.lastIndex = chars[i] - OFFSET;
            if (result.lastIndex < 0 || result.lastIndex >= result.found.letters.length) {
                throw new IllegalArgumentException("Characters in the string can nly be between ! and ~ (33-126)");
            }
            result.found = result.found.letters[result.lastIndex];
            i++;
        }
        return Optional.empty();
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return key instanceof String && find((String) key).isPresent();
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public T get(Object key) {
        if (key instanceof String) {
            return find((String) key).map(f -> f.found).map(f -> f.value).orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public T put(String key, T value) {
        keys.add(key);
        final var chars = key.toCharArray();
        int i = 0;
        Node<T> node = root;
        Node<T> parent = null;
        while (node != null) {
            if (i >= chars.length) {
                final T old = node.value;
                node.value = value;
                return old;
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
        while (i < chars.length) {
            final var index = chars[i] - OFFSET;
            if (index < 0 || index >= parent.letters.length) {
                throw new IllegalArgumentException("Characters in the string can only be between ! and ~ (33-126) in '" + key + "'");            }
            parent.letters[index] = new Node<>();
            parent = parent.letters[index];
            i++;
        }
        parent.value = value;
        return null;
    }

    @Override
    public T remove(Object key) {
        if (key instanceof String) {
            final var result = find((String) key);
            if (result.isPresent()) {
                final T old = result.get().parent.letters[result.get().lastIndex].value;
                result.get().parent.letters[result.get().lastIndex].value = null;
                keys.remove(key);
                return old;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void putAll(Map<? extends String, ? extends T> m) {
        for (final var e : m.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    @Override
    public void clear() {
        root.value = null;
        for (int i = 0; i < root.letters.length; i++) {
            root.letters[i] = null;
        }
    }

    @Override
    public Set<String> keySet() {
        return keys;
    }

    @Override
    public Collection<T> values() {
        Set<T> entries = new HashSet<>();
        for (final var s : keys) {
            entries.add(get(s));
        }
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public Set<Entry<String, T>> entrySet() {
        Set<Entry<String, T>> entries = new HashSet<>();
        for (final var s : keys) {
            entries.add(new AbstractMap.SimpleEntry<>(s, get(s)));
        }
        return Collections.unmodifiableSet(entries);
    }
}
