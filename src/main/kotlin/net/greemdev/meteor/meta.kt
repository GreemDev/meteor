/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("utils")
@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.greemdev.meteor

import com.google.common.base.MoreObjects
import com.google.common.base.Predicates
import com.google.common.base.Suppliers
import com.google.common.util.concurrent.Runnables
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.utils.java.Loop as JavaLoop
import net.fabricmc.loader.api.FabricLoader
import net.fabricmc.loader.api.MappingResolver
import net.greemdev.meteor.util.modLoader
import net.minecraft.network.packet.Packet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.MessageFactory
import org.joml.Vector3f
import org.joml.Vector4f
import kotlin.math.*
import java.awt.Color
import java.io.File
import java.nio.file.Path
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Callable
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.io.path.Path
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.cast
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.system.measureTimeMillis

inline fun <T> getOrNull(crossinline func: Getter<T>): T? = runCatching(func).getOrNull()
fun <T> callOrNull(func: Callable<T>): T? = getOrNull(func::call)

inline fun tryOrIgnore(crossinline func: Action) = runCatching(func).getOrDefault(Unit)
fun runOrIgnore(action: ErrorProneRunnable) = tryOrIgnore(action::invoke)

fun interface ErrorProneRunnable {
    @Throws(Throwable::class) operator fun invoke()
}


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

inline fun <T> Collection<T>.getRandomElement(): T = //exists to be used from Java
    random()


inline fun <T : Any> Collection<T>.findFirst() = optionalOf(firstOrNull())
inline fun <T : Any> Collection<T>.findLast() = optionalOf(lastOrNull())
inline fun <T : Any> Collection<T>.find(predicate: Predicate<T>) = optionalOf(firstOrNull(predicate))

val Any.logger
    get() = log4j(this).value

inline infix fun Any?.eq(other: Any?) = Objects.equals(this, other)
inline fun hashOf(vararg objects: Any?) = Objects.hash(objects)

inline fun <reified T> stringHelper() = stringHelper(T::class.java)
inline fun stringHelper(cls: Any): MoreObjects.ToStringHelper = when (cls) {
    is String -> MoreObjects.toStringHelper(cls)
    else -> MoreObjects.toStringHelper(cls)
}

inline fun createDescriptorString(cls: Any, builder: Initializer<MoreObjects.ToStringHelper>) =
    stringHelper(cls).apply(builder).toString()


fun <T : Any> T.descriptorString(omitNulls: Boolean = false) =
    createDescriptorString(this) {
        if (omitNulls)
            omitNullValues()

        addKotlinPropertiesOf(this)
    }


fun <T : Any> MoreObjects.ToStringHelper.addKotlinPropertiesOf(value: T): MoreObjects.ToStringHelper {
    value.javaClass.kotlin.declaredMemberProperties.forEach {
        add(it.name, it.get(value))
    }
    return this
}

inline fun <reified T> mapObfuscated() = mapObfuscated(T::class.java)

/**
 * Returns the class' canonical name, and if the class is in the `net.minecraft` package it is mapped via [MappingResolver] before returning.
 */
val Class<*>.qualifiedNameOrMappingName: String
    get() =
        if (packageName.startsWith("net.minecraft"))
            mappedCanonicalName
        else
            canonicalName

/**
 * Returns the class' simple name, and if the class is in the `net.minecraft` package it is mapped via [MappingResolver] before returning.
 */
val Class<*>.simpleNameOrMappingName: String
    get() = qualifiedNameOrMappingName.split(".").last()


fun mapObfuscated(clazz: Class<*>): String =
    modLoader.mappingResolver.mapClassName(modLoader.mappingResolver.currentRuntimeNamespace, clazz.canonicalName)

val Class<*>.mappedCanonicalName: String
    get() = mapObfuscated(this)




inline operator fun FabricLoader.contains(modId: String) = isModLoaded(modId)

fun <T> Collection<T>?.lastIndex() = orEmpty().size - 1
fun <T> Array<T>.lastIndex() = size - 1

typealias MeteorColor = meteordevelopment.meteorclient.utils.render.color.Color
typealias AwtColor = Color

fun colorOf(value: Any): MeteorColor = when (value) {
    is String -> {
        when {
            value.contains(",") -> MeteorColor.fromString(value)

            value.length == 6 || (value.startsWith("#") && value.length == 7) ->
                MeteorColor(value.takeLast(6).toInt(16))

            value.length == 8 || (value.startsWith("#") && value.length == 9) ->
                MeteorColor(value.takeLast(8).take(6).toInt(16), value.takeLast(2).toInt(16))

            else -> throw NumberFormatException("Illegal or incorrectly formatted color value. Only accepts R,G,B(,A); (#)RRGGBB; and (#)RRGGBBAA")
        }
    }

    is Int -> MeteorColor(value)
    is Vector4f -> MeteorColor(value)
    is Vector3f -> MeteorColor(value)
    else -> throw IllegalArgumentException("Unknown color value. Only accepts R,G,B(,A); (#)RRGGBB; and (#)RRGGBBAA, a raw RGB int, or 3F/4F vectors.")
}

fun Date.format(fmt: String): String = formats.computeIfAbsent(fmt, ::SimpleDateFormat).format(this)

private val formats = mutableMapOf<String, SimpleDateFormat>()

fun <T> firstNotNull(vararg nullables: T?): T & Any = nullables.firstNotNullOf(Lambdas.identity())
fun <T> Iterable<T?>.firstNotNull(): T = firstNotNullOf(Lambdas.identity())

@JvmName("forEachIndexed")
fun <T> List<T>.indexedForEach(consumer: BiConsumer<Int, T>) = forEachIndexed(consumer.kotlin)

/**
 * Sets the [KMutableProperty]'s value and then returns the new value.
 */
fun <T> KMutableProperty<T>.coalesce(newValue: T): T {
    setter.call(newValue)
    return newValue
}

//for java
fun<T> T?.coalesce(default: T) = this ?: default

fun <T> Iterable<T>.max(selector: (T) -> Double) = maxOf(selector)

fun <T> Iterable<T>.min(selector: (T) -> Double) = minOf(selector)

@get:JvmName("isServerbound")
val Class<out Packet<*>>.isServerbound
    get() = mappedCanonicalName.split('.').last().contains("C2S")

@get:JvmName("isClientbound")
val Class<out Packet<*>>.isClientbound
    get() = mappedCanonicalName.split('.').last().contains("S2C")

fun AwtColor.meteor() = MeteorColor(red, green, blue, alpha)

fun any(vararg conditions: Boolean) = conditions.any()

fun <T, R : Comparable<R>> List<T>.sorted(
    sorted: Boolean = true,
    isAscending: Boolean = true,
    sorter: Mapper<T, R?>
): List<T> = toMutableList().apply {
    if (sorted)
        if (isAscending)
            sortBy(sorter)
        else
            sortByDescending(sorter)
}



fun <T : Any> optionalOf(value: T? = null): Optional<T> = Optional.ofNullable(value)
fun<T : Any> T?.opt() = optionalOf(this)

fun <T : Any> T.optionalIf(predicate: Predicate<T>) = optionalOf(takeIf(predicate))
fun <T : Any> T.optionalUnless(predicate: Predicate<T>) = optionalOf(takeUnless(predicate))

fun<T> Optional<T>.test(predicate: JPredicate<in T>): Boolean {
    if (isEmpty) return false
    return filter(predicate).isPresent
}

fun <T, R : Any> Optional<T>.mapNullable(mapper: Mapper<T, R?>): Optional<R> = flatMap { optionalOf(mapper(it)) }


operator fun File.div(child: String) = File(this, child)
operator fun Path.div(subpath: String): Path = resolve(subpath)
operator fun Path.div(subpath: Path): Path = resolve(subpath)
operator fun String.div(child: String) = Path(this, child)

@JvmName("apply")
fun <T> `apply-java`(value: T, consumer: Consumer<T>) = value.apply(consumer::accept)

@JvmName("let")
fun <T, R> `let-java`(value: T, mapper: Mapper<T, R>) = value.let(mapper::invoke)

@JvmField
val forLoop = object : Loops() {}

abstract class Loops {
    @JvmSynthetic
    infix fun int(builder: Initializer<JavaLoop<Int>>) = JavaLoop.ofInt().apply(builder).run()

    @JvmSynthetic
    infix fun float(builder: Initializer<JavaLoop<Float>>) = JavaLoop.ofFloat().apply(builder).run()

    @JvmSynthetic
    infix fun double(builder: Initializer<JavaLoop<Double>>) = JavaLoop.ofDouble().apply(builder).run()
}

@JvmName("sleepCurrentThread")
fun grossSynchronousShit(millis: Long) {
    try {
        Thread.sleep(millis)
    } catch (e: InterruptedException) {
        e.printStackTrace()
    }
}

fun File.filter(predicate: Predicate<File>): List<File>? = listFiles(predicate)?.toList()

/**
 * Identical to [File.createNewFile],
 * with the addition of returning false if an I/O error occurs instead of throwing the IO error.
 */
fun File.createFile() = getOrNull { createNewFile() } ?: false

/**
 * If the receiver [KClass] is an `object` definition, return the singleton instance, otherwise call the primary constructor with the provided arguments.
 */
fun <T : Any> KClass<T>.findInstance(vararg args: Any?) = objectInstance ?: primaryConstructor?.call(args)

infix fun <T : WPressable> T.action(func: ValueAction<T>): T = action { func(this) }

/**
 * Parses a 6-character long hexadecimal sequence to a [Color] with or without the preceding #.
 */
fun parseHexColor(hex: String) = AwtColor(
    hex.takeLast(6).uppercase()
        .optionalIf { seq ->
            seq.all { it in '0'..'9' || it in 'A'..'F' }
        }
        .flatMap { optionalOf(it.toIntOrNull(16)) }
        .orElseThrow { NumberFormatException("Illegal hexadecimal sequence.") }
)

inline infix fun <P1, P2, P3> Pair<P1, P2>.then(third: P3) = Triple(first, second, third)
inline infix fun <P1, P2, P3, P4> Triple<P1, P2, P3>.with(fourth: P4) = Quad(first, second, third, fourth)


data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

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
 */
inline fun <R> catchErrors(func: Getter<R>) =
    runCatching(func).let {
        it.getOrNull() to it.exceptionOrNull()
    }

inline fun <R> Result<R>.throwIfFailure(lazyMessage: Getter<String>): Result<R> {
    if (isFailure) throw IllegalStateException(lazyMessage(), exceptionOrNull())

    return this
}

inline fun <T, R> Result<T>.mapTo(
    failedMessage: Getter<String> = { "mapTo can only be used on a successful Result<R>" },
    transform: Mapper<T, R>
) =
    throwIfFailure(failedMessage)
        .map(transform)
        .getOrThrow()

inline fun <T, R> Result<T>.mapToOrNull(transform: Mapper<T, R>) = map(transform).getOrNull()

inline fun<T, E : Throwable> Result<T>.onFailureOf(type: KClass<E>, action: ValueAction<E>): Result<T> {
    exceptionOrNull()?.also {
        if (type.isInstance(it))
            action(type.cast(it))
    }

    return this
}

fun measureTime(task: Runnable) = measureTimeMillis(task.kotlin)


inline fun <T : Throwable> T.suppressAll(first: Throwable, vararg otherThrowables: Throwable): T {
    addSuppressed(first)

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

inline fun<reified T> GsonBuilder.registerTypeAdapter(deserializer: JsonDeserializer<T>): GsonBuilder =
    registerTypeAdapter(T::class.java, deserializer)

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
    @JvmField val noOperation: Action = Runnables.doNothing().kotlin
    @JvmStatic fun <T> void(): ValueAction<T> = { }
    @JvmStatic fun <T> constant(value: T): Getter<T> = Suppliers.ofInstance(value).kotlin
    @JvmStatic fun <T> identity(): Mapper<T, T> = Function.identity<T>().kotlin

    @JvmName("ktAllowAll")
    fun <T> allowAll(): Predicate<T> = Predicates.alwaysTrue<T>().kotlin

    @JvmName("allowAll")
    @JvmStatic fun <T> `java-allowAll`(): JPredicate<T> = Predicates.alwaysTrue<T>()

    @JvmName("ktAllowNone")
    fun <T> allowNone(): Predicate<T> = Predicates.alwaysFalse<T>().kotlin

    @JvmName("allowNone")
    @JvmStatic fun <T> `java-allowNone`(): JPredicate<T> = Predicates.alwaysFalse<T>()

    @JvmStatic fun invert(supplier: Supplier<Boolean>) = Supplier { !supplier() }

    @JvmStatic fun<T> not(predicate: Predicate<T>): Predicate<T> = JPredicate.not(predicate.java).kotlin

    fun aboveZero(): Predicate<Int> = { it > 0 }
    fun aboveOrZero(): Predicate<Int> = { it >= 0 }

    fun belowZero(): Predicate<Int> = { it < 0 }

    fun belowOrZero(): Predicate<Int> = { it <= 0 }
}

fun Supplier<Boolean>.inverse() = Lambdas.invert(this)
fun<T> Predicate<T>.not() = Lambdas.not(this)

// Kotlin <-> Java lambda type conversions for interoperability


// Action <-> Runnable
@get:JvmName("java")
inline val Action.java: Runnable get() = Runnable(this::invoke)
@get:JvmName("kotlin")
inline val Runnable.kotlin: Action get() = this::run

// ValueAction <-> Consumer
@get:JvmName("java")
inline val <T> ValueAction<T>.java: Consumer<T> get() = Consumer(this::invoke)
@get:JvmName("kotlin")
inline val <T> Consumer<T>.kotlin: ValueAction<T> get() = this::accept

// BiValueAction <-> BiConsumer
@get:JvmName("java")
inline val <T1, T2> BiValueAction<T1, T2>.java: BiConsumer<T1, T2> get() = BiConsumer(this::invoke)
@get:JvmName("kotlin")
inline val <T1, T2> BiConsumer<T1, T2>.kotlin: BiValueAction<T1, T2> get() = this::accept

private typealias JPredicate<T> = java.util.function.Predicate<T>

@get:JvmName("java")
inline val <T> Predicate<T>.java: JPredicate<T> get() = JPredicate(this::invoke)
@get:JvmName("kotlin")
inline val <T> JPredicate<T>.kotlin: Predicate<T> get() = this::test

// Getter <-> Supplier
@get:JvmName("java")
inline val <T> Getter<T>.java: Supplier<T> get() = Supplier(this::invoke)
@get:JvmName("kotlin")
inline val <T> Supplier<T>.kotlin: Getter<T> get() = this::get

// Mapper <-> Function
@get:JvmName("java")
inline val <I, O> Mapper<I, O>.java: Function<I, O> get() = Function(this::invoke)
@get:JvmName("kotlin")
inline val <I, O> Function<I, O>.kotlin: Mapper<I, O> get() = this::apply


inline operator fun Runnable.invoke() = run()
inline operator fun<T> Consumer<T>.invoke(arg: T) = accept(arg)
inline operator fun<T1, T2> BiConsumer<T1, T2>.invoke(arg1: T1, arg2: T2) = accept(arg1, arg2)
inline operator fun<T> JPredicate<T>.invoke(arg: T) = test(arg)
inline operator fun<T> Supplier<T>.invoke() = get()
inline operator fun<I, O> Function<I, O>.invoke(arg: I) = apply(arg)

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
