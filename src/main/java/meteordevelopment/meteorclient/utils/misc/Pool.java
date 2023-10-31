/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

public class Pool<T> {
    private final Queue<T> items = new ArrayDeque<>();
    private final Supplier<T> producer;

    public Pool(Supplier<T> producer) {
        this.producer = producer;
    }

    public synchronized T get() {
        return items.isEmpty()
            ? producer.get()
            : items.poll();
    }

    public synchronized void free(T obj) {
        items.offer(obj);
    }
}
