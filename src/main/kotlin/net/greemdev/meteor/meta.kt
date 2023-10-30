/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("utils")
@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalContracts::class)

package net.greemdev.meteor

import com.google.common.base.MoreObjects
import kotlinx.datetime.Clock
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.settings.*
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
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.full.safeCast

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

fun Date.format(fmt: String): String = SimpleDateFormat(fmt).format(this)

fun <T> firstNotNull(vararg nullables: T?) = nullables.firstNotNullOf(Lambdas.selfMapper())
fun <T> Iterable<T?>.firstNotNull(): T = firstNotNullOf(Lambdas.selfMapper())

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

fun any(vararg conditions: Boolean) = conditions.any()

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

// Kotlin to Java & vice-versa lambda type conversions for interoperability


// Action <-> Runnable
inline val Action.java: Runnable
    get() = Runnable(this::invoke)

inline val Runnable.kotlin: Action
    get() = this::run

inline operator fun Runnable.invoke() = kotlin()


// ValueAction <-> Consumer
inline val <T> ValueAction<T>.java: Consumer<T>
    get() = Consumer(this::invoke)

inline val <T> Consumer<T>.kotlin: ValueAction<T>
    get() = this::accept

inline operator fun<T> Consumer<T>.invoke(arg: T) = kotlin(arg)


private typealias JPredicate<T> = java.util.function.Predicate<T>

inline val <T> Predicate<T>.java: JPredicate<T>
    get() = JPredicate(this::invoke)

inline val <T> JPredicate<T>.kotlin: Predicate<T>
    get() = this::test

inline operator fun<T> JPredicate<T>.invoke(arg: T) = kotlin(arg)


// Getter <-> Supplier
inline val <T> Getter<T>.java: Supplier<T>
    get() = Supplier(this::invoke)

inline val <T> Supplier<T>.kotlin: Getter<T>
    get() = this::get

inline operator fun<T> Supplier<T>.invoke() = kotlin()


// Mapper <-> Function
inline val <I, O> Mapper<I, O>.java: Function<I, O>
    get() = Function(this::invoke)

inline val <I, O> Function<I, O>.kotlin: Mapper<I, O>
    get() = this::apply

inline operator fun<I, O> Function<I, O>.invoke(arg: I) = kotlin(arg)

fun <T : Any> optionalOf(value: T? = null): Optional<T> = Optional.ofNullable(value)

fun <T : Any> T.optionally(predicate: Predicate<T>) = optionalOf(takeIf(predicate))
fun <T : Any> T.optionallyNot(predicate: Predicate<T>) = optionalOf(takeUnless(predicate))

inline fun <T> invoking(noinline func: Getter<T>): FunctionProperty<T> = FunctionProperty(func)
inline fun <T> invokingOrNull(noinline func: Getter<T>): FunctionProperty<T?> = FunctionProperty { getOrNull(func) }

class FunctionProperty<T>(private val getter: Getter<T>) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = getter()
}

fun<T> observable(value: T, observer: ValueAction<T>, vararg otherObservers: ValueAction<T>) =
    Observable(value, otherObservers.toMutableList()).apply { this.observers.add(observer) }

class Observable<T>(private var value: T, val observers: MutableList<ValueAction<T>>) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    fun get() = value

    fun set(value: T) {
        this.value = value
        observers.call()
    }

    private fun List<ValueAction<T>>.call() {
        forEach { it(value) }
    }
}

@FunctionalInterface
interface Observer<T> : ValueAction<T> {
    override operator fun invoke(arg: T) = valueChanged(arg)

    fun valueChanged(value: T)
}

operator fun File.div(child: String) = File(this, child)
operator fun Path.div(childPath: String): Path = resolve(childPath)
operator fun Path.div(childPath: Path): Path = resolve(childPath)

@JvmName("apply")
fun <T> `apply-java`(value: T, consumer: Consumer<T>) = value.apply(consumer::accept)

@JvmName("let")
fun <T, R> `let-java`(value: T, mapper: Mapper<T, R>) = value.let(mapper::invoke)

@JvmName("currentTime")
fun `Clock-System-now`() = Clock.System.now()

fun File.filter(predicate: Predicate<File>) = listFiles(FileFilter(predicate))?.toList()

/**
 * Identical to [File.createNewFile],
 * with the addition of returning false if an I/O error occurs instead of throwing the IO error.
 */
fun File.createFile() = getOrNull { createNewFile() } ?: false

/**
 * If the receiver [KClass] is an `object` definition, return the singleton instance, otherwise call the primary constructor with the provided arguments.
 */
fun <T : Any> KClass<T>.findInstance(vararg args: Any?) = objectInstance ?: primaryConstructor?.call(args)

infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

/**
 * Parses a 6-character long hexadecimal sequence to a [Color] with or without the preceding #.
 */
fun parseHexColor(hex: String) = Color(
    hex.takeLast(6).uppercase()
        .optionally { seq ->
            seq.all { it in '0'..'9' || it in 'A'..'F' }
        }
        .map { getOrNull { it.toInt(16) } }
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

inline fun<reified T> Any.cast() =
    if (this::class.java.canonicalName == T::class.java.canonicalName)
        castFast<T>()
    else error("Cannot cast an object of type '${E::class.qualifiedName}' to type '${this::class.qualifiedName}'.")


@Suppress("UNCHECKED_CAST")
fun<T> Any.castFast() = this as T

/**
 * Returns 1 for true, and 0 for false.
 */
fun Boolean.asInt() = if (this) 1 else 0
fun Int.asBoolean() = this >= 1

object Lambdas {
    @JvmField val noOperation: Action = { }
    @JvmStatic fun <T> void(): ValueAction<T> = { }
    @JvmStatic fun <T> constant(value: T): Getter<T> = { value }
    @JvmStatic fun <T> selfMapper(): Mapper<T, T> = { it }
}

typealias Action = () -> Unit
typealias SuspendingAction = suspend () -> Unit

typealias ValueAction<T> = Mapper<T, Unit>
typealias SuspendingValueAction<T> = SuspendingMapper<T, Unit>

typealias Predicate<T> = Mapper<T, Boolean>
typealias SuspendingPredicate<T> = SuspendingMapper<T, Boolean>

typealias Initializer<T> = T.() -> Unit
typealias SuspendingInitializer<T> = suspend T.() -> Unit

typealias Getter<T> = () -> T
typealias SuspendingGetter<T> = suspend () -> T

typealias Mapper<I, O> = (I) -> O
typealias SuspendingMapper<I, O> = suspend (I) -> O

typealias Visitor<T> = (T) -> T
typealias SuspendingVisitor<T> = suspend (T) -> T

typealias VisitorOn<T> = T.() -> T
typealias SuspendingVisitorOn<T> = suspend T.() -> T
