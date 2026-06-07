package org.caesium.base;

import java.util.function.Consumer;

/*
* The base interface to implement a Clock unit
* */
public interface Clock {
    void start(int tickRate, Consumer<Long> callback);
    void stop();
}
