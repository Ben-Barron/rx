package com.benbarron.rx.subject;

import com.benbarron.rx.Observable;
import com.benbarron.rx.Observer;
import com.benbarron.rx.lang.Closeable;
import com.benbarron.rx.lang.ConcurrentCollection;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

public class Subject<T> implements Observable<T>, Observer<T> {

    private final Collection<Observer<T>> observers = new ConcurrentCollection<>();
    private final AtomicBoolean isStopped = new AtomicBoolean(false);

    @Override
    public void onComplete() {
        if (isStopped.compareAndSet(false, true)) {
            observers.forEach(Observer::onComplete);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (isStopped.compareAndSet(false, true)) {
            observers.forEach(observer -> observer.onError(throwable));
        }
    }

    @Override
    public void onNext(T item) {
        if (!isStopped.get()) {
            observers.forEach(observer -> observer.onNext(item));
        }
    }

    @Override
    public Closeable subscribe(Observer<T> observer) {
        observers.add(observer);
        return () -> observers.remove(observer);
    }

}
