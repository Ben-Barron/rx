package com.benbarron.rx.lang;

public interface CloseableManager extends Closeable {

    void addCloseable(AutoCloseable closeable);
}
