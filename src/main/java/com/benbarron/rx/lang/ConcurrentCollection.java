package com.benbarron.rx.lang;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A thread-safe Collection.
 * @param <T> The type of elements in this collection
 */
public class ConcurrentCollection<T> extends AbstractCollection<T> {

    private final AtomicReference<T[]> arrayReference;

    @SafeVarargs
    public ConcurrentCollection(T... items) {
        this.arrayReference = new AtomicReference<>(items);
    }

    @Override
    public boolean add(T t) {
        arrayReference.updateAndGet(prev -> {
            T[] next = Arrays.copyOf(prev, prev.length + 1);
            next[prev.length] = t;
            return next;
        });

        return true;
    }

    @Override
    public void clear() {
        arrayReference.updateAndGet(prev -> Arrays.copyOf(prev, 0));
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private final T[] array = arrayReference.get();

            private int position = 0;

            @Override
            public boolean hasNext() {
                return position < array.length;
            }

            @Override
            public T next() {
                if (hasNext()) {
                    return array[position++];
                }

                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        T[] prev, next;

        do {
            prev = arrayReference.get();
            next = splice(arrayReference.get(), o);
        } while(!arrayReference.compareAndSet(prev, next));

        return next.length < prev.length;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        return arrayReference.get().length;
    }


    private static <T> T[] splice(T[] array, Object itemToRemove) {
        T[] next = array;

        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(itemToRemove)) {
                next = Arrays.copyOf(array, array.length - 1);
                System.arraycopy(array, i + 1, next, i, next.length - i);
                break;
            }
        }

        return next;
    }
}
