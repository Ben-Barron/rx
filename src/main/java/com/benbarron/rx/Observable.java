package com.benbarron.rx;

import com.benbarron.rx.lang.Closeable;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a stream of push-based notifications.
 * @param <T> The object that provides notification information.
 */
public interface Observable<T> {

    default <R> Observable<R> operate(BiConsumer<Observer<R>, T> operation) {
        return operate(o ->
            new DefaultObserver<T>() {
                @Override
                protected void doOnComplete() {
                    o.onComplete();
                }

                @Override
                protected void doOnError(Throwable throwable) {
                    o.onError(throwable);
                }

                @Override
                protected void doOnNext(T item) {
                    operation.accept(o, item);
                }
            });
    }

    default <R> Observable<R> operate(Function<Observer<R>, Observer<T>> operation) {
        return observer -> this.subscribe(operation.apply(observer));
    }

    Closeable subscribe(Observer<T> observer);

    default Closeable subscribe() {
        return subscribe(i -> { });
    }

    default Closeable subscribe(Consumer<T> onNext) {
        return subscribe(onNext, () -> { });
    }

    default Closeable subscribe(Consumer<T> onNext,
                                Runnable onComplete) {

        return subscribe(onNext, t -> { }, onComplete);
    }

    default Closeable subscribe(Consumer<T> onNext,
                                Consumer<Throwable> onError,
                                Runnable onComplete) {

        return subscribe(new DefaultObserver<T>() {
            @Override
            protected void doOnComplete() {
                onComplete.run();
            }

            @Override
            protected void doOnError(Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            protected void doOnNext(T item) {
                onNext.accept(item);
            }
        });
    }

    default Observable<T> subscriptionOperate(BiFunction<Observable<T>, Observer<T>, Closeable> subscribeOperation) {
        return o -> subscribeOperation.apply(this, o);
    }


    static <T> Observable<T> generate(Consumer<Observer<T>> generationFunction) {
        return generate(o -> {
            generationFunction.accept(o);
            return Closeable.empty();
        });
    }

    static <T> Observable<T> generate(Function<Observer<T>, Closeable> generationFunction) {
        return generationFunction::apply;
    }
}
