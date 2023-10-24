/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Coroutines")
package net.greemdev.meteor.util


import kotlinx.coroutines.*
import net.greemdev.meteor.*
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.function.Supplier
import kotlin.concurrent.thread

inline fun Runtime.addSuspendShutdownHook(crossinline func: SuspendingInitializer<CoroutineScope>) =
    addShutdownHook(thread(start = false) { runBlocking { func() } })

context(CoroutineScope)
fun <T> Collection<T>.forEachLaunch(func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        launch { func(it) }
    }
}

fun <T> Collection<T>.forEachBlocking(func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        runBlocking { func(it) }
    }
}

@JvmName("waitForCompletion")
fun<T> `deferred-result-from-java`(deferred: Deferred<T>): T {
    return runBlocking { deferred.await() }
}

suspend infix fun <T> Deferred<T>.thenTake(block: SuspendingValueAction<T>) = thenMap(block)
suspend infix fun <T> Deferred<T>.then(block: SuspendingVisitor<T>): T = block(await())

infix fun <T, R> Deferred<T>.thenAsync(block: suspend T.() -> R): Deferred<R> =
    scope.async { block(await()) }
suspend infix fun <T, R> Deferred<T>.thenMap(block: SuspendingMapper<T, R>): R = block(await())

inline fun CoroutineScope.jobBuilder(crossinline block: Initializer<AsyncJobBuilder>) = object : AsyncJobBuilder(this) {}.apply(block)

inline fun<J : Job> CoroutineScope.wrapJob(job: J, crossinline block: Initializer<AsyncJobBuilder>) = jobBuilder(block) executing job

abstract class AsyncJobBuilder(
    @get:JvmName("scope")
    val scope: CoroutineScope
) {
    private var onSuccess: Action = {}
    private var onFailure: ValueAction<Throwable> = {}
    private var onCancel: ValueAction<CancellationException> = {}

    fun whenDone(block: Action): AsyncJobBuilder {
        onSuccess = block
        return this
    }

    fun whenError(block: ValueAction<Throwable>): AsyncJobBuilder {
        onFailure = block
        return this
    }

    fun whenCancelled(block: ValueAction<CancellationException>): AsyncJobBuilder {
        onCancel = block
        return this
    }

    infix fun executing(block: SuspendingInitializer<CoroutineScope>) = executing(scope.launch(block = block))

    infix fun<J : Job> executing(job: J) = job.apply {
        invokeOnCompletion {
            when (it) {
                null -> onSuccess()
                is CancellationException -> onCancel(it)
                else -> onFailure(it)
            }
        }
    }
}

private val coroutineLog by log4j { "Coroutines" }

private var threadId = 0

private val pool = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism().coerceAtLeast(2)) {
    thread(
        name = "Greteor-Work-Thread-${threadId++}",
        start = false,
        isDaemon = true,
        block = it.kotlin
    )
}

@get:JvmName("dispatcher")
val dispatcher by lazy(pool::asCoroutineDispatcher)

@get:JvmName("supervisor")
val supervisor by lazy(::SupervisorJob)

@get:JvmName("exceptionHandler")
val coroutineExceptionHandler by lazy {
    CoroutineExceptionHandler { ctx, t ->
        if (t is Error) {
            supervisor.cancel()
            throw t
        }
        if (t !is CancellationException)
            coroutineLog.error("Unhandled exception in coroutine ${ctx.job}", t)
    }
}

@get:JvmName("scope")
val scope by lazy {
    CoroutineScope(dispatcher + supervisor + coroutineExceptionHandler)
}

inline fun<T> coroutines(block: CoroutineScope.() -> T): T = scope.block()

/**
 * Intended usage is to call [AsyncJobBuilder.executing] as the last line of the lambda so that it's the result of the lambda.
 */
fun<J : Job> launchJob(builder: AsyncJobBuilder.() -> J) = scope.jobBuilder {}.builder()

fun Runnable.runInCoroutine(scope: CoroutineScope) =
    scope.launch { run() }

fun<T> Supplier<T>.getAsync(scope: CoroutineScope) =
    scope.async { get() }
