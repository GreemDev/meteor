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
    private final Supplier<T> supplier;

    public Pool(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public synchronized T get() {
        if (items.size() > 0) return items.poll();
        return supplier.get();
    }

    public synchronized void free(T obj) {
        items.offer(obj);
    }
}
