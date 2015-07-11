package com.benbarron.rx;

import java.util.concurrent.atomic.AtomicBoolean;

abstract class DefaultObserver<T> implements CloseableObserver<T> {

    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    protected abstract void doOnComplete();

    protected abstract void doOnError(Throwable throwable);

    protected abstract void doOnNext(T item);

    @Override
    public void close() { }

    @Override
    public void onComplete() {
        if (isStopped.compareAndSet(false, true)) {
            doOnComplete();
            close();
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (isStopped.compareAndSet(false, true)) {
            doOnError(throwable);
            close();
        }
    }

    @Override
    public void onNext(T item) {
        if (!isStopped.get()) {
            doOnNext(item);
        }
    }
}
