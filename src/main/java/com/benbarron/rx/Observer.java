package com.benbarron.rx;

/**
 * Provides a mechanism for receiving push-based notifications.
 * @param <T> The object that provides notification information.
 */
public interface Observer<T> {

    /**
     * Notifies the observer that the provider has finished sending push-based notifications.
     */
    void onComplete();

    /**
     * Notifies the observer that the provider has experienced an error condition.
     * @param throwable An object that provides additional information about the error.
     */
    void onError(Throwable throwable);

    /**
     * Provides the observer with new data.
     * @param item The current notification information.
     */
    void onNext(T item);
}
