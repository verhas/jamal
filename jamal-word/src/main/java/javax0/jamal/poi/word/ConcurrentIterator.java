package javax0.jamal.poi.word;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A simple iterator that will not throw concurrent modification exception iterating through the body elements when the
 * processing deletes and replaces some elements. It can be done safely, so long as long the element returned the last
 * time remains inside the list.
 * <p>
 * If the modification happens on elements that were already passed, then the iteration will not go through them.
 * If the modification happens on elements that were not yet passed, then the iteration will go through them.
 *
 * @param <T> the type of the elements to be iterated over.
 */
public class ConcurrentIterator<T> implements Iterator<T>, Consumer<T>, Supplier<T> {
    private int index;
    private final List<T> list;
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
        if(index < list.size()){
            return true;
        }
        lastReturned = null;
        return false;
    }

    @Override
    public T next() {
        if (hasNext()) {
            return lastReturned = list.get(index++);
        }
        return lastReturned = null;
    }

    @Override
    public T get() {
        return lastReturned;
    }

    @Override
    public void accept(final T lastReturned) {
        this.lastReturned = lastReturned;
    }

}
