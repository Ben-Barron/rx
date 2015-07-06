package com.benbarron.rx.lang;

import com.sun.istack.internal.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A thread-safe Collection.
 * @param <E> The type of elements in this collection
 */
public class ConcurrentCollection<E> extends AbstractCollection<E> {

    @SuppressWarnings("unchecked")
    private final AtomicReference<E[]> arrayReference = new AtomicReference<>((E[]) new Object[0]);

    @Override
    public boolean add(E e) {
        arrayReference.updateAndGet(prev -> {
            E[] next = Arrays.copyOf(prev, prev.length + 1);
            next[prev.length] = e;
            return next;
        });

        return true;
    }

    @Override
    public void clear() {
        arrayReference.updateAndGet(prev -> Arrays.copyOf(prev, 0));
    }

    @Override
    public Iterator<E> iterator() {
        return new Iterator<E>() {

            private final E[] array = arrayReference.get();

            private int position = 0;

            @Override
            public boolean hasNext() {
                return position < array.length;
            }

            @Override
            public E next() {
                if (hasNext()) {
                    return array[position++];
                }

                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public boolean remove(Object o) {
        E[] prev, next;

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
