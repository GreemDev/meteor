/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util


import kotlinx.coroutines.*
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import kotlin.concurrent.thread

fun Runtime.addSuspendShutdownHook(func: suspend CoroutineScope.() -> Unit) =
    addShutdownHook(thread(start = false) { runBlocking(block = func) })

fun <T> Collection<T>.launchForEach(scope: CoroutineScope, func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        scope.launch { func(it) }
    }
}

fun <T> Collection<T>.runForEach(func: suspend CoroutineScope.(T) -> Unit) {
    forEach {
        runBlocking { func(it) }
    }
}

fun Job.invokeOnFailure(block: (Throwable) -> Unit) = invokeOnCompletion {
    if (it != null && it !is CancellationException)
        block(it)
}

fun Job.invokeOnSuccess(block: () -> Unit) = invokeOnCompletion {
    if (it == null)
        block()
}

suspend infix fun <T> Deferred<T>.thenTake(block: suspend (T) -> Unit) = thenRun(block)
suspend infix fun <T> Deferred<T>.then(block: suspend (T) -> T): T = block(await())
suspend infix fun <T, R> Deferred<T>.thenRun(block: suspend (T) -> R): R = block(await())

inline fun CoroutineScope.jobBuilder(crossinline block: AsyncJobBuilder.() -> Unit) = object : AsyncJobBuilder(this) {}.apply(block)

inline fun CoroutineScope.wrapJob(job: Job, crossinline block: AsyncJobBuilder.() -> Unit) = jobBuilder(block) executing job

abstract class AsyncJobBuilder(private val scope: CoroutineScope) {
    private var onSuccess: () -> Unit = {}
    private var onFailure: (Throwable) -> Unit = {}
    private var onCancel: (CancellationException) -> Unit = {}

    fun whenDone(block: () -> Unit) {
        onSuccess = block
    }

    fun whenError(block: (Throwable) -> Unit) {
        onFailure = block
    }

    fun whenCancelled(block: (CancellationException) -> Unit) {
        onCancel = block
    }

    infix fun executing(block: suspend CoroutineScope.() -> Unit) = executing(scope.launch(block = block))

    infix fun executing(job: Job) = job.apply {
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

private fun nextId() = threadId++

private val pool = Executors.newScheduledThreadPool(ForkJoinPool.getCommonPoolParallelism().coerceAtLeast(2)) {
    thread(
        start = false,
        name = "Greteor-Work-Thread-${nextId()}",
        isDaemon = true,
        block = it::run
    )
}

val dispatcher by invoking(pool::asCoroutineDispatcher)
val supervisor by lazy(::SupervisorJob)

val scope by lazy {
    CoroutineScope(dispatcher + supervisor + CoroutineExceptionHandler { ctx, t ->
        if (t is Error) {
            supervisor.cancel()
            throw t
        }
        if (t !is CancellationException)
            coroutineLog.error("Unhandled exception in coroutine ${ctx.job}", t)
    })
}

fun<T> coroutines(block: CoroutineScope.() -> T): T = scope.block()

fun launchJob(builder: AsyncJobBuilder.() -> Job): Job {
    return scope.jobBuilder {}.builder()
}
