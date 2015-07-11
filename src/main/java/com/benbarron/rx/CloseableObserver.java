package com.benbarron.rx;

import com.benbarron.rx.lang.Closeable;

public interface CloseableObserver<T> extends Closeable, Observer<T> { }
