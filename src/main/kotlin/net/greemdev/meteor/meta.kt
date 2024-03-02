/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("utils")
@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:OptIn(ExperimentalContracts::class)

package net.greemdev.meteor

import com.google.common.base.MoreObjects
import com.google.common.base.Predicates
import kotlinx.datetime.Clock
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.MappingResolver
import net.greemdev.meteor.util.modLoader
import net.minecraft.network.packet.Packet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.MessageFactory
import kotlin.math.*
import java.awt.Color
import java.io.File
import java.io.FileFilter
import java.lang.IllegalStateException
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.*
import java.util.function.Function
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.safeCast
import kotlin.system.measureTimeMillis

inline fun <T> getOrNull(crossinline func: Getter<T>): T? = runCatching(func).getOrNull()
fun <T> supplyOrNull(func: Supplier<T>): T? = getOrNull(func.kotlin)

inline fun tryOrIgnore(crossinline func: Action) = runCatching(func).getOrDefault(Unit)
fun runOrIgnore(runnable: Runnable) = tryOrIgnore(runnable.kotlin)


// Looks repetitive however each different type we check for has its own unique overload in LogManager
fun log4j(value: Any) = lazy<Logger> {
    when (value) {
        is String -> LogManager.getLogger(value)
        is Class<*> -> LogManager.getLogger(value)
        is KClass<*> -> LogManager.getLogger(value.java)
        is MessageFactory -> LogManager.getLogger(value)
        else -> LogManager.getLogger(value)
    }
}

inline fun <T> Collection<T>.getRandomElement(): T { //exists to be used from Java
    return random()
}

inline fun <T : Any> Collection<T>.findFirst() = optionalOf(firstOrNull())
inline fun <T : Any> Collection<T>.findLast() = optionalOf(firstOrNull())
inline fun <T : Any> Collection<T>.find(predicate: Predicate<T>) = optionalOf(firstOrNull(predicate))

fun<T> MutableSet<T>.removeMatching(predicate: Predicate<T>): Int {
    var removed = 0
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (predicate(iterator.next())) {
            iterator.remove()
            removed++
        }
    }
    return removed
}

fun<T> MutableSet<T>.retainMatching(predicate: Predicate<T>) = removeMatching(predicate.not())

fun<T> MutableList<T>.setElements(elements: Collection<T>) {
    clear()
    addAll(elements)
}

fun getLogger(value: Any) = log4j(value).value

inline infix fun Any?.eq(other: Any?) = Objects.equals(this, other)
inline fun hashOf(vararg objects: Any?) = Objects.hash(objects)

inline fun Any.stringHelper(): MoreObjects.ToStringHelper = when (this) {
    is String -> MoreObjects.toStringHelper(this)
    else -> MoreObjects.toStringHelper(this)
}

fun <T : Any> T.descriptorString(omitNulls: Boolean = true): String {
    val sh = stringHelper()
    if (omitNulls)
        sh.omitNullValues()

    return sh.addProperties(this).toString()
}

inline fun <reified T> mapObfuscated() = mapObfuscated(T::class.java)

/**
 * Returns the class' canonical name, and if the class is in the `net.minecraft` package it is mapped via [MappingResolver] before returning.
 */
val Class<*>.qualifiedNameOrMappingName: String
    get() =
        if (packageName.startsWith("net.minecraft"))
            mapObfuscated(this)
        else
            canonicalName

/**
 * Returns the class' simple name, and if the class is in the `net.minecraft` package it is mapped via [MappingResolver] before returning.
 */
val Class<*>.simpleNameOrMappingName: String
    get() = qualifiedNameOrMappingName.split(".").last()


fun mapObfuscated(clazz: Class<*>): String =
    modLoader.mappingResolver.mapClassName(modLoader.mappingResolver.currentRuntimeNamespace, clazz.canonicalName)


fun <T : Any> MoreObjects.ToStringHelper.addProperties(value: T): MoreObjects.ToStringHelper {
    value.javaClass.kotlin.declaredMemberProperties.forEach {
        add(it.name, it.get(value))
    }
    return this
}

inline fun <reified T> stringHelper(): MoreObjects.ToStringHelper = MoreObjects.toStringHelper(T::class.java)

inline operator fun FabricLoader.contains(modId: String) = isModLoaded(modId)

fun <T> T?.coalesce(other: T) = this ?: other
fun <T> Collection<T>?.lastIndex() = orEmpty().size - 1

typealias MeteorColor = meteordevelopment.meteorclient.utils.render.color.Color
typealias AwtColor = Color

fun colorOf(value: Any): MeteorColor = when (value) {
    is String -> {
        when {
            value.contains(",") -> MeteorColor.fromString(value)

            (value.startsWith("#") && value.length == 7) || value.length == 6 ->
                MeteorColor(value.takeLast(6).toInt(16))

            (value.startsWith("#") && value.length == 9) || value.length == 8 ->
                MeteorColor(value.takeLast(8).take(6).toInt(16)).apply {
                    a = value.takeLast(2).toInt(16)
                }

            else -> throw NumberFormatException("Illegal or unknown color value. Only accepts R,G,B(,A); (#)RRGGBB; and (#)RRGGBBAA")
        }
    }

    is Int -> MeteorColor(value)
    else -> throw IllegalArgumentException("Illegal or unknown color value. Only accepts R,G,B(,A); (#)RRGGBB; and (#)RRGGBBAA, or a raw RGB int.")
}

@Suppress("FunctionName")
@JvmName("err")
fun `i fucking hate checked exceptions`(t: Throwable): Nothing =
    throw t

fun Date.format(fmt: String): String = SimpleDateFormat(fmt).format(this)

fun <T> firstNotNull(vararg nullables: T?) = nullables.firstNotNullOf(Lambdas.identity())
fun <T> Iterable<T?>.firstNotNull(): T = firstNotNullOf(Lambdas.identity())

fun <T> List<T>.indexedForEach(consumer: BiConsumer<Int, T>) =
    this.forEachIndexed { index, t -> consumer.accept(index, t) }

/**
 * Sets the [KMutableProperty]'s value and then returns the new value.
 */
fun <T> KMutableProperty<T>.coalesce(newValue: T): T {
    setter.call(newValue)
    return getter.call()
}

fun <T> Iterable<T>.max(selector: (T) -> Double) = maxOf(selector)


fun <T> Iterable<T>.min(selector: (T) -> Double) = minOf(selector)

fun Class<out Packet<*>>.isC2S() = simpleName.contains("C2S")
fun Class<out Packet<*>>.isS2C() = simpleName.contains("S2C")

fun AwtColor.meteor() = MeteorColor(red, green, blue, alpha)

fun any(vararg conditions: Boolean) = conditions.any(Lambdas.identity())

fun <T, R : Comparable<R>> List<T>.sorted(
    sorted: Boolean = true,
    isAscending: Boolean = true,
    sorter: Mapper<T, R?>
): List<T> = toMutableList().apply {
    if (sorted) {
        if (isAscending)
            sortBy(sorter)
        else
            sortByDescending(sorter)
    }
}

fun <T : Any> optionalOf(value: T? = null): Optional<T> = Optional.ofNullable(value)
fun<T : Any> T?.opt() = optionalOf(this)

fun <T : Any> T.optionalIf(predicate: Predicate<T>) = optionalOf(takeIf(predicate))
fun <T : Any> T.optionalUnless(predicate: Predicate<T>) = optionalOf(takeUnless(predicate))

fun<T> Optional<T>.test(predicate: Predicate<in T>): Boolean {
    if (isEmpty) return false
    return filter(predicate).isPresent
}



operator fun File.div(child: String) = File(this, child)
operator fun Path.div(childPath: String): Path = resolve(childPath)
operator fun Path.div(childPath: Path): Path = resolve(childPath)

@JvmName("apply")
fun <T> `apply-java`(value: T, consumer: Consumer<T>) = value.apply(consumer::accept)

@JvmName("let")
fun <T, R> `let-java`(value: T, mapper: Mapper<T, R>) = value.let(mapper::invoke)

@JvmName("sleepCurrentThread")
fun grossSynchronousShit(millis: Long) {
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

@JvmName("currentTime")
fun `Clock-System-now`() = Clock.System.now()

fun File.filter(predicate: Predicate<File>): List<File>? = listFiles(FileFilter(predicate))?.toList()

/**
 * Identical to [File.createNewFile],
 * with the addition of returning false if an I/O error occurs instead of throwing the IO error.
 */
fun File.createFile() = getOrNull { createNewFile() } ?: false

/**
 * If the receiver [KClass] is an `object` definition, return the singleton instance, otherwise call the primary constructor with the provided arguments.
 */
fun <T : Any> KClass<T>.findInstance(vararg args: Any?) = objectInstance ?: primaryConstructor?.call(args)

fun <T : Any> KClass<T>.tryFindInstance(vararg args: Any?) = objectInstance ?: getOrNull { primaryConstructor?.call(args) }

infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

/**
 * Parses a 6-character long hexadecimal sequence to a [Color] with or without the preceding #.
 */
fun parseHexColor(hex: String) = Color(
    hex.takeLast(6).uppercase()
        .optionalIf { seq ->
            seq.all { it in '0'..'9' || it in 'A'..'F' }
        }
        .flatMap { optionalOf(it.toIntOrNull(16)) }
        .orElseThrow { IllegalArgumentException("Illegal hexadecimal sequence.") }
)

/**
 * ### Functional error handling
 *
 * Intended for use via destructuring:
 * ```val (value, error) = catchErrors { ... }```
 *
 * ### Note:
 * If one value in the pair is present, the other is not.
 * That is to say, if you check whether the error is present, and it isn't, you can assume that the value *is* present.
 *
 * Conversely, if you check whether the value is present, and it isn't, you can assume that an error occurred.
 *
 *
 * Recommended practice is to always check if the exception is present before any further operations, returning if necessary.
 *
 * @sample catchErrors
 */
inline fun <R> catchErrors(func: Getter<R>) =
    runCatching(func).let {
        it.getOrNull() to it.exceptionOrNull()
    }

inline fun <R> Result<R>.throwIfFailure(lazyMessage: Getter<String>): Result<R> {
    contract {
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }

    if (isFailure) {
        throw IllegalStateException(lazyMessage(), exceptionOrNull())
    }
    return this
}

infix fun <P1, P2, P3> Pair<P1, P2>.then(value: P3) = Triple(first, second, value)

inline fun <T, R> Result<T>.mapTo(
    failedMessage: String = "mapTo can only be run on a successful Result<R>",
    transform: Mapper<T, R>
) =
    throwIfFailure(Lambdas.constant(failedMessage))
        .map(transform)
        .getOrThrow()

inline fun<T, reified E : Throwable> Result<T>.onFailureOf(type: KClass<E>, action: ValueAction<E>): Result<T> {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    type.safeCast(exceptionOrNull())?.let(action)

    return this
}

inline fun <T, R> Result<T>.mapToOrNull(transform: Mapper<T, R>) = map(transform).getOrNull()


inline fun <T : Throwable> T.suppressAll(first: Throwable, vararg otherThrowables: Throwable): T {
    addSuppressed(first)

    if (otherThrowables.isEmpty()) return this

    otherThrowables.forEach(this::addSuppressed)
    return this
}

inline fun<reified T> Any.castChecked() =
    if (this::class.java.canonicalName == T::class.java.canonicalName)
        cast<T>()
    else error("Cannot cast an object of type '${E::class.qualifiedName}' to type '${this::class.qualifiedName}'.")


@Suppress("UNCHECKED_CAST")
fun<T> Any.cast() = this as T

@Suppress("UNCHECKED_CAST")
fun<T> Any.tryCast() = this as? T



/**
 * Returns 1 for true, and 0 for false.
 */
fun Boolean.asInt() = if (this) 1 else 0
fun Int.asBoolean() = this >= 1

fun measureTime(action: Runnable) = measureTimeMillis(action.kotlin)

fun tickTask(delayBetween: Int, action: Action) = Ticker.Builder().tickLimit(delayBetween).action(action).build()
fun tickTask(delayBetween: Int, conditionModifier: Pipe<Boolean>, action: Action) =
    Ticker.Builder().tickLimit(delayBetween).condition(conditionModifier).action(action).build()
fun Action.runEvery(ticks: Int, conditionModifier: Pipe<Boolean> = Lambdas.identity()) = tickTask(ticks, conditionModifier, this)

fun ticker(builder: Initializer<Ticker.Builder>) = Ticker.Builder().apply(builder).build()

class Ticker private constructor(
    private val tickLimit: Getter<Int>,
    private val conditionModifier: Pipe<Boolean> = Lambdas.identity(),
    private val action: Action
) : Runnable by action.java {
    private var ticked = 0
    fun tick() {
        if (conditionModifier(ticked >= tickLimit())) {
            run()
            ticked = 0
        } else ticked++
    }

    class Builder {
        private var tickLimit: Getter<Int>? = null
        private var conditionModifier: Pipe<Boolean> = Lambdas.identity()
        private var action: Action? = null

        fun tickLimit(limit: Int) = tickLimit(Lambdas.constant(limit))

        fun tickLimit(limit: Getter<Int>): Builder {
            tickLimit = limit
            return this
        }

        fun condition(modifier: Pipe<Boolean>): Builder {
            conditionModifier = modifier
            return this
        }

        fun action(action: Action): Builder {
            this.action = action
            return this
        }

        fun build() = Ticker(
            tickLimit ?: error("Cannot create a Ticker without specifying a tick limit."),
            conditionModifier,
            action ?: error("Cannot create a Ticker without specifying an action to run.")
        )
    }
}

object Lambdas {
    @JvmField val noOperation: Action = { }
    @JvmStatic fun <T> void(): ValueAction<T> = { }
    @JvmStatic fun <T> constant(value: T): Getter<T> = { value }
    @JvmStatic fun <T> identity(): Mapper<T, T> = { it }

    @JvmName("ktAllowAll")
    fun <T> allowAll(): Predicate<T> = { true }

    @JvmName("allowAll")
    @JvmStatic fun <T> `java-allowAll`(): JPredicate<T> = Predicates.alwaysTrue<T>()

    @JvmName("ktAllowNone")
    fun <T> allowNone(): Predicate<T> = { false }

    @JvmName("allowNone")
    @JvmStatic fun <T> `java-allowNone`(): JPredicate<T> = Predicates.alwaysFalse<T>()

    @JvmStatic inline fun invert(supplier: Supplier<Boolean>) = Supplier { !supplier() }

    @JvmStatic inline fun<T> not(crossinline predicate: Predicate<T>): Predicate<T> = { !predicate(it) }

    fun aboveZero(): Predicate<Int> = { it > 0 }
    fun aboveOrZero(): Predicate<Int> = { it >= 0 }

    fun belowZero(): Predicate<Int> = { it < 0 }

    fun belowOrZero(): Predicate<Int> = { it <= 0 }
}

inline fun Supplier<Boolean>.inverse() = Lambdas.invert(this)
inline fun<T> Predicate<T>.not() = Lambdas.not(this)

// Kotlin <-> Java lambda type conversions for interoperability


// Action <-> Runnable
@get:JvmName("java")
inline val Action.java: Runnable get() = Runnable(this)
@get:JvmName("kotlin")
inline val Runnable.kotlin: Action get() = this::run

// ValueAction <-> Consumer
@get:JvmName("java")
inline val <T> ValueAction<T>.java: Consumer<T> get() = Consumer(this)
@get:JvmName("kotlin")
inline val <T> Consumer<T>.kotlin: ValueAction<T> get() = this::accept

// BiValueAction <-> BiConsumer
@get:JvmName("java")
inline val <T1, T2> BiValueAction<T1, T2>.java: BiConsumer<T1, T2> get() = BiConsumer(this)
@get:JvmName("kotlin")
inline val <T1, T2> BiConsumer<T1, T2>.kotlin: BiValueAction<T1, T2> get() = this::accept

private typealias JPredicate<T> = java.util.function.Predicate<T>

@get:JvmName("java")
inline val <T> Predicate<T>.java: JPredicate<T> get() = JPredicate(this)
@get:JvmName("kotlin")
inline val <T> JPredicate<T>.kotlin: Predicate<T> get() = this::test

// Getter <-> Supplier
@get:JvmName("java")
inline val <T> Getter<T>.java: Supplier<T> get() = Supplier(this)
@get:JvmName("kotlin")
inline val <T> Supplier<T>.kotlin: Getter<T> get() = this::get

// Mapper <-> Function
@get:JvmName("java")
inline val <I, O> Mapper<I, O>.java: Function<I, O> get() = Function(this)
@get:JvmName("kotlin")
inline val <I, O> Function<I, O>.kotlin: Mapper<I, O> get() = this::apply


inline operator fun Runnable.invoke() = run()
inline operator fun<T> JPredicate<T>.invoke(arg: T) = test(arg)
inline operator fun<T> Supplier<T>.invoke() = get()

inline operator fun<T> Consumer<T>.invoke(arg: T) = accept(arg)
inline operator fun<T1, T2> BiConsumer<T1, T2>.invoke(arg1: T1, arg2: T2) = accept(arg1, arg2)

inline operator fun<I, O> Function<I, O>.invoke(arg: I) = apply(arg)
inline operator fun<I1, I2, O> BiFunction<I1, I2, O>.invoke(arg1: I1, arg2: I2) = apply(arg1, arg2)

typealias Action = () -> Unit
typealias SuspendingAction = suspend () -> Unit

typealias Initializer<T> = T.() -> Unit
typealias SuspendingInitializer<T> = suspend T.() -> Unit

typealias Getter<T> = () -> T
typealias SuspendingGetter<T> = suspend () -> T

typealias Mapper<I, O> = (I) -> O
typealias SuspendingMapper<I, O> = suspend (I) -> O

typealias BiValueAction<T1, T2> = (T1, T2) -> Unit
typealias SuspendingBiValueAction<T1, T2> = suspend (T1, T2) -> Unit

typealias ValueAction<T> = Mapper<T, Unit>
typealias SuspendingValueAction<T> = SuspendingMapper<T, Unit>

typealias Predicate<T> = Mapper<T, Boolean>
typealias SuspendingPredicate<T> = SuspendingMapper<T, Boolean>

typealias Pipe<T> = (T) -> T
typealias SuspendingPipe<T> = suspend (T) -> T

typealias PipeOn<T> = T.() -> T
typealias SuspendingPipeOn<T> = suspend T.() -> T
