package com.benbarron.rx;

import com.benbarron.rx.lang.Closeable;
import com.benbarron.rx.lang.ConcurrentCollection;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a stream of push-based notifications.
 * @param <T> The object that provides notification information.
 */
public interface Observable<T> {

    default <R> Observable<R> operate(BiConsumer<Observer<R>, T> operation) {
        return operate(observer -> {
            AtomicBoolean isStopped = new AtomicBoolean(false);
            return new Observer<T>() {
                @Override
                public void onComplete() {
                    if (isStopped.compareAndSet(false, true)) {
                        observer.onComplete();
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    if (isStopped.compareAndSet(false, true)) {
                        observer.onError(throwable);
                    }
                }

                @Override
                public void onNext(T item) {
                    if (!isStopped.get()) {
                        operation.accept(observer, item);
                    }
                }
            };
        });
    }

    default <R> Observable<R> operate(Function<Observer<R>, Observer<T>> operation) {
        return observer -> this.subscribe(operation.apply(observer));
    }

    default ConnectableObservable<T> publish() {
        Observable<T> self = this;
        ConcurrentCollection<Observer<T>> observers = new ConcurrentCollection<>();
        AtomicBoolean isConnected = new AtomicBoolean(false);
        AtomicBoolean isStopped = new AtomicBoolean(false);
        //volatile tomicReference<Closeable> subscription = new AtomicReference<>(null);

        return new ConnectableObservable<T>() {
            @Override
            public Closeable connect() {
                if (isConnected.compareAndSet(false, true)) {
                    self.subscribe(new Observer<T>() {
                        @Override
                        public void onComplete() {
                            observers.forEach(Observer::onComplete);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            observers.forEach(observer -> observer.onError(throwable));
                        }

                        @Override
                        public void onNext(T item) {
                            observers.forEach(observer -> observer.onNext(item));
                        }
                    });
                }

                return null;
            }

            @Override
            public Closeable subscribe(Observer<T> observer) {
                observers.add(observer);
                return () -> observers.remove(observer);
            }
        };
    }

    Closeable subscribe(Observer<T> observer);

    default Closeable subscribe(Consumer<T> onNext) {
        return subscribe(onNext, () -> { });
    }

    default Closeable subscribe(Consumer<T> onNext,
                                Runnable onComplete) {

        return subscribe(onNext, throwable -> { }, onComplete);
    }

    default Closeable subscribe(Consumer<T> onNext,
                                Consumer<Throwable> onError,
                                Runnable onComplete) {

        AtomicBoolean isStopped = new AtomicBoolean(false);
        return subscribe(new Observer<T>() {
            @Override
            public void onComplete() {
                if (isStopped.compareAndSet(false, true)) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(Throwable throwable) {
                if (isStopped.compareAndSet(false, true)) {
                    onError.accept(throwable);
                }
            }

            @Override
            public void onNext(T item) {
                if (!isStopped.get()) {
                    onNext.accept(item);
                }
            }
        });
    }

    default Observable<T> subscriptionOperate(BiFunction<Observable<T>, Observer<T>, Closeable> subscribeOperation) {
        return observer -> subscribeOperation.apply(this, observer);
    }


    static <T> Observable<T> generate(Consumer<Observer<T>> generationFunction) {
        return generate(observer -> {
            generationFunction.accept(observer);
            return Closeable.empty();
        });
    }

    static <T> Observable<T> generate(Function<Observer<T>, Closeable> generationFunction) {
        return generationFunction::apply;
    }

    @SafeVarargs
    static <T> Observable<T> merge(Observable<T>... observables) {
        return observer ->
            Closeable.wrap(Stream.of(observables)
                .map(observable -> observable.subscribe(observer))
                .collect(Collectors.toList()));
    }
}
