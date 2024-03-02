/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Coroutines")
@file:OptIn(InternalCoroutinesApi::class)
package net.greemdev.meteor.util


import kotlinx.coroutines.*
import net.greemdev.meteor.*
import java.io.Closeable
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.function.Supplier
import kotlin.concurrent.thread

fun Runtime.addSuspendShutdownHook(func: SuspendingInitializer<CoroutineScope>) =
    addShutdownHook(thread(start = false) { runBlocking(block = func) })

context(CoroutineScope)
fun <T> Collection<T>.forEachLaunch(func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        launch { this.func(it) }
    }
}

fun <T> Collection<T>.forEachBlocking(func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        runBlocking { func(it) }
    }
}

fun <T, R> Collection<T>.mapAsync(func: suspend CoroutineScope.(T) -> R): Collection<Deferred<R>> {
    return map { scope.async { func(it) } }
}

@JvmName("getDeferred")
fun<T> `deferred-result-from-java`(deferred: Deferred<T>): T {
    return runBlocking { deferred.await() }
}

suspend inline infix fun <T> Deferred<T>.thenAccept(block: SuspendingValueAction<T>) = thenMap(block)
suspend inline infix fun <T> Deferred<T>.then(block: SuspendingPipe<T>): T = block(await())

inline infix fun <T, R> Deferred<T>.thenAsync(crossinline block: suspend T.() -> R): Deferred<R> =
    scope.async { thenMap(block) }

suspend inline infix fun <T, R> Deferred<T>.thenMap(block: SuspendingMapper<T, R>): R = block(await())

fun CoroutineScope.newJobBuilder() = object : AsyncJobBuilder(this) {}

inline fun<J : Job> CoroutineScope.wrapJob(job: J, crossinline block: Initializer<AsyncJobBuilder>) = newJobBuilder().apply(block) executing job

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


    infix fun<J : Job> executing(job: J): DisposableCoroutine<J> {
        val handle = job.invokeOnCompletion(onCancelling = true, invokeImmediately = true) {
            when (it) {
                null -> onSuccess()
                is CancellationException -> onCancel(it)
                else -> onFailure(it)
            }
        }
        return DisposableCoroutine(handle, job)
    }
}

class DisposableCoroutine<J : Job>(handle: DisposableHandle, val job: J) : Job by job, DisposableHandle by handle, Closeable {
    override fun close() {
        cancel()
        dispose()
    }
}

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
            Greteor.logger.error("Unhandled exception in coroutine ${ctx.job}", t)
    }
}

@get:JvmName("scope")
val scope by lazy {
    CoroutineScope(dispatcher + supervisor + coroutineExceptionHandler)
}

fun scoped(block: CoroutineScope.() -> Unit) = coroutines(block)

inline fun<T> coroutines(block: CoroutineScope.() -> T): T = scope.block()

/**
 * Intended usage is to call [AsyncJobBuilder.executing] on the last line of the lambda, so that it's the result of the lambda,
 * and because [AsyncJobBuilder.executing] is actually what starts the job; and as such it should be called absolutely last.
 */
fun<J : Job> buildJob(coroutineScope: CoroutineScope = scope, build: AsyncJobBuilder.() -> J) = coroutineScope.newJobBuilder().build()

@JvmOverloads
fun Runnable.runInCoroutine(coroutineScope: CoroutineScope = scope) = coroutineScope.launch { run() }

@JvmOverloads
fun<T> Supplier<T>.getAsync(coroutineScope: CoroutineScope = scope) = coroutineScope.async { get() }
