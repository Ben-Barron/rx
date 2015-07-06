package com.benbarron.rx.lang;

public abstract class CloseableBase implements CloseableManager {

    private final ConcurrentCollection<Closeable> closeables = new ConcurrentCollection<>();

    public void addCloseable(Closeable closeable) {
        closeables.add(closeable);
    }

    @Override
    public void close() {
        Closeable.closeAll(closeables);
    }
}
