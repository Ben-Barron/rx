package com.benbarron.rx;

import com.benbarron.rx.lang.Closeable;

/**
 * Represents an observable that can be connected and disconnected.
 * @param <T> The object that provides notification information.
 */
public interface ConnectableObservable<T> extends Observable<T> {

    /**
     * Connects the observable.
     * @return Closeable object used to disconnect the observable.
     */
    Closeable connect();
}
