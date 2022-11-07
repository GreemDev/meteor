/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Util")

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.settings.*
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.Packet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.MessageFactory
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import kotlin.math.*
import java.awt.Color
import java.io.File
import java.io.FileFilter
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor

fun <T> getOrNull(func: () -> T): T? = try {
    func()
} catch (t: Throwable) {
    null
}

fun <T> supplyOrNull(func: Supplier<T>): T? = try {
    func.get()
} catch (t: Throwable) {
    null
}

fun tryOrIgnore(func: () -> Unit) = try {
    func()
} catch (ignored: Throwable) {
}

fun runOrIgnore(runnable: Runnable) = try {
    runnable.run()
} catch (ignored: Throwable) {
}

// Looks repetitive however each different type we check for has its own special logic in LogManager
fun log4j(value: Any) = lazy<Logger> {
    when (value) {
        is String -> LogManager.getLogger(value)
        is Class<*> -> LogManager.getLogger(value)
        is KClass<*> -> LogManager.getLogger(value.java)
        is MessageFactory -> LogManager.getLogger(value)
        else -> LogManager.getLogger(value)
    }
}

fun getLogger(value: Any) = log4j(value).getValue(null, ::meteor)

operator fun FabricLoader.contains(modId: String) = modLoader.isModLoaded(modId)

fun <T> T?.coalesce(other: T) = this ?: other
fun <T> Collection<T>?.lastIndex() = orEmpty().size - 1

typealias MeteorColor = meteordevelopment.meteorclient.utils.render.color.Color
typealias AwtColor = Color

fun colorOf(value: Any): MeteorColor = try {
    when (value) {
        is String -> {
            when {
                value.contains(",") -> MeteorColor.fromString(value)

                (value.startsWith("#") && value.length == 7) || value.length == 6 ->
                    MeteorColor(value.takeLast(6).toInt(16))

                (value.startsWith("#") && value.length == 9) || value.length == 8 ->
                    MeteorColor(value.takeLast(8).take(6).toInt(16)).apply {
                        a = value.takeLast(2).toInt(16)
                    }

                else -> throw NumberFormatException()
            }
        }

        is Int -> MeteorColor(value)
        else -> throw IllegalArgumentException()
    }
} catch (e: Exception) {
    throw IllegalArgumentException("Invalid color value. Only accepts R,G,B(,A); (#)RRGGBB; and (#)RRGGBBAA.").apply {
        addSuppressed(e)
    }
}

fun Date.format(fmt: String): String = SimpleDateFormat(fmt).format(this)


fun <T> firstNotNull(vararg nullables: T?) = nullables.firstNotNullOf { it }
fun <T> Iterable<T?>.firstNotNull(): T = firstNotNullOf { it }

fun <T> List<T>.indexedForEach(consumer: BiConsumer<Int, T>) =
    this.forEachIndexed { index, t -> consumer.accept(index, t) }

/**
 * Sets the [KMutableProperty]'s value and then returns the new value.
 */
fun <T> KMutableProperty<T>.coalesce(newValue: T): T {
    setter.call(newValue)
    return newValue
}

fun Class<out Packet<*>>.isC2S() = simpleName.contains("C2S")
fun Class<out Packet<*>>.isS2C() = simpleName.contains("S2C")

fun MeteorColor.awt() = AwtColor(r, g, b, a)
fun AwtColor.meteor() = MeteorColor(red, green, blue, alpha)


fun any(vararg conditions: Boolean) = conditions.any()
fun <T, R : Comparable<R>> List<T>.sorted(
    sorted: Boolean = true,
    isAscending: Boolean = true,
    sorter: (T) -> R?
): List<T> = toMutableList().apply {
    if (sorted) {
        if (isAscending)
            sortBy(sorter)
        else
            sortByDescending(sorter)
    }
}

fun <T : Any> optionalOf(value: T? = null): Optional<T> = Optional.ofNullable(value)

fun <T> invoking(func: () -> T): FunctionProperty<T> = FunctionProperty(func)
fun <T> invokingOrNull(func: () -> T): FunctionProperty<T?> = FunctionProperty { getOrNull(func) }

class FunctionProperty<T>(private val producer: () -> T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = producer()
}

fun StringSetting.Builder.renderStarscript(): StringSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)
fun StringListSetting.Builder.renderStarscript(): StringListSetting.Builder =
    renderer(StarscriptTextBoxRenderer::class.java)

inline fun <reified T> javaSubtypesOf(pkg: String): Set<Class<out T>> =
    Reflections(
        ConfigurationBuilder()
            .forPackage(pkg)
            .addScanners(Scanners.SubTypes)
    ).getSubTypesOf(T::class.java)

inline fun <reified T : Any> subtypesOf(pkg: String): List<KClass<out T>> =
    javaSubtypesOf<T>(pkg).map { it.kotlin }

inline fun <reified T : Any> createSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        getOrNull { it.primaryConstructor?.call() }
    }

inline fun <reified T : Any> findInstancesOfSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        getOrNull { it.findInstance() }
    }

operator fun File.div(child: String) = File(this, child)

fun File.listed(predicate: (File) -> Boolean): List<File>? = listFiles(FileFilter(predicate))?.toList()

/**
 * Identical to [File.createNewFile],
 * with the addition of returning false if an I/O error occurs instead of throwing the IO error.
 */
fun File.createFile() = getOrNull { createNewFile() } ?: false

fun <T : Any> KClass<T>.findInstance(vararg args: Any?) = objectInstance ?: primaryConstructor?.call(args)

infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

/**
 * Parses a 6-character long hexadecimal sequence to a [Color] with or without the preceding #.
 */
fun parseHexColor(hex: String): Color = Color(
    optionalOf(hex.takeLast(6).uppercase()
        .takeIf {
            it.all { ch ->
                ch in '0'..'9' || ch in 'A'..'F'
            }
        }
    ).orElseThrow { IllegalArgumentException("Illegal hexadecimal sequence.") }
        .toInt(16)
)

/**
 * Kotlin try-with-resources
 * @sample using
 */
fun<T : AutoCloseable, V> using(value: T, func: T.() -> V) = with(value) {
    func().also { close() }
}

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
inline fun<V> catchErrors(func: () -> V) = try {
    func() to null
} catch (t: Throwable) {
    null to t
}
