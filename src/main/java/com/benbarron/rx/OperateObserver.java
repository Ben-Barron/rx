package com.benbarron.rx;

import com.benbarron.rx.lang.Closeable;

public interface OperateObserver<T> extends Closeable, Observer<T> { }
