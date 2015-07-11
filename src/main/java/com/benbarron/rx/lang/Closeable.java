package com.benbarron.rx.lang;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provides a mechanism for releasing resources.
 */
@FunctionalInterface
public interface Closeable extends AutoCloseable {

    /**
     * The empty closeable.
     */
    Closeable EMPTY = () -> { };

    /**
     * Performs application-defined tasks associated with freeing, releasing, or resetting resources.
     */
    @Override
    void close();

    /**
     * Closes when CloseableManagerBase.close() is called.
     * @param closeableManager CloseableManager on which to close with.
     */
    default Closeable closeWith(CloseableManager closeableManager) {
        closeableManager.addCloseable(this);
        return this;
    }


    /**
     * Returns the empty closeable.
     * @return The empty closeable (Closeable.EMPTY).
     */
    static Closeable empty() {
        return EMPTY;
    }

    /**
     * Closes all the supplied closeables. Any exceptions are caught and collated into one exception.
     * @param closeables Array of closeables.
     */
    static void closeAll(Closeable... closeables) {
        closeAll(Arrays.asList(closeables));
    }

    /**
     * Closes all the supplied closeables. Any exceptions are caught and collated into one exception.
     * @param closeables Iterable of closeables.
     */
    static void closeAll(Iterable<Closeable> closeables) {
        Collection<Throwable> exceptions = new LinkedList<>();

        closeables.forEach(closeable -> {
            try {
                closeable.close();
            } catch (Exception e) {
                exceptions.add(e);
            }
        });

        if (exceptions.isEmpty()) {
            return;
        }

        RuntimeException exception = new RuntimeException();
        exceptions.forEach(exception::addSuppressed);

        throw exception;
    }

    /**
     * Returns a closeable which closes all underlying closeables.
     * @param closeables Array of underlying closeables.
     * @return A closeable that closes the underlying closeables.
     */
    static Closeable wrap(Closeable... closeables) {
        return wrap(Arrays.asList(closeables));
    }

    /**
     * Returns a closeable which closes all underlying closeables.
     * @param closeables Iterable of underlying closeables.
     * @return A closeable that closes the underlying closeables.
     */
    static Closeable wrap(Iterable<Closeable> closeables) {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        return () -> {
            if (isClosed.compareAndSet(false, true)) {
                closeAll(closeables);
            }
        };
    }
}
