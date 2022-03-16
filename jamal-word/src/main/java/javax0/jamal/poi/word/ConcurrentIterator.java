package javax0.jamal.poi.word;

import java.util.ConcurrentModificationException;
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
    private T lastReturned;

    public ConcurrentIterator(List<T> list) {
        index = 0;
        lastReturned = null;
        this.list = list;
    }

    private void checkConcurrentModification() {
        if (lastReturned != null && list.get(index - 1) != lastReturned) {
            for (index = 0; index < list.size(); index++) {
                if (list.get(index) == lastReturned) {
                    index++;
                    return;
                }
            }
            throw new ConcurrentModificationException(String.format("%s is not found in the list", lastReturned));
        }
    }

    @Override
    public boolean hasNext() {
        checkConcurrentModification();
        return index < list.size();
    }

    @Override
    public T next() {
        checkConcurrentModification();
        return lastReturned = list.get(index++);
    }
}
