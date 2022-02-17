package javax0.jamal.poi.word;

import java.util.Iterator;
import java.util.List;

/**
 * A simple iterator that will not throw concurrent modification exception iterating through the body elements when the
 * processing deletes and replaces some elements. It can be done safely, because in this very special case we know that
 * the modification happens before the current position.
 *
 * @param <T> the type of the elements to be iterated over.
 */
public class ConcurrentIterator<T> implements Iterator<T> {
    private int index;
    private List<T> list;

    public ConcurrentIterator(List<T> list) {
        index = 0;
        this.list = list;
    }

    @Override
    public boolean hasNext() {
        return index < list.size();
    }

    @Override
    public T next() {
        return list.get(index++);
    }
}
