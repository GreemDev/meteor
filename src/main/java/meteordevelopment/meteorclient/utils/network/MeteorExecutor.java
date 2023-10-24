/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import kotlinx.coroutines.Job;
import kotlinx.coroutines.future.FutureKt;
import net.greemdev.meteor.util.Coroutines;
import net.greemdev.meteor.utils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MeteorExecutor {

    private MeteorExecutor() {}

    public static Job execute(Runnable task) {
        return Coroutines.runInCoroutine(task, Coroutines.scope());
    }

    public static Job execute(Runnable task, Runnable success) {
        return Coroutines.launchJob(builder ->
            builder.whenDone(utils.getKotlin(success))
                .executing(Coroutines.runInCoroutine(task, builder.scope()))
        );
    }

    public static Job execute(Runnable task, Runnable success, Consumer<Throwable> errored) {
        return Coroutines.launchJob(builder ->
            builder.whenDone(utils.getKotlin(success))
                .whenError(utils.getKotlin(errored))
                .executing(Coroutines.runInCoroutine(task, builder.scope()))
        );
    }

    public static Job execute(Runnable task, Runnable success, Consumer<Throwable> errored, Consumer<CancellationException> cancelled) {
        return Coroutines.launchJob(builder ->
            builder.whenDone(utils.getKotlin(success))
                .whenError(utils.getKotlin(errored))
                .whenCancelled(utils.getKotlin(cancelled))
                .executing(Coroutines.runInCoroutine(task, builder.scope()))
        );
    }

    public static <T> CompletableFuture<T> get(Supplier<T> supplier) {
        return FutureKt.asCompletableFuture(Coroutines.getAsync(supplier, Coroutines.scope()));
    }
}
