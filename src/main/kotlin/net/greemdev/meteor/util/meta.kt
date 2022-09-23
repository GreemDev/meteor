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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.MessageFactory
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.awt.Color
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

operator fun FabricLoader.contains(modId: String) = modLoader.isModLoaded(modId)

fun<T> T?.coalesce(other: T) = this ?: other
fun <T> Collection<T>?.getOrEmpty() = this ?: emptySet()
fun <T> Collection<T>?.lastIndex() = getOrEmpty().size - 1

typealias MeteorColor = meteordevelopment.meteorclient.utils.render.color.Color
typealias AwtColor = Color
fun colorOf(value: Any): MeteorColor {
    return try {
        when (value) {
            is String -> {
                when {
                    value.contains(",") -> MeteorColor().apply { parse(value) }

                    (value.startsWith("#") && value.length == 7) || value.length == 6 ->
                        MeteorColor(value.takeLast(6).toInt(16))

                    (value.startsWith("#") && value.length == 9) || value.length == 8 ->
                        MeteorColor(value.takeLast(8).toInt(16))

                    else -> throw NumberFormatException()
                }
            }
            is Int -> MeteorColor(value)
            else -> throw IllegalArgumentException()
        }
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid color value. Only accepts R,G,B; (#)RRGGBB; and (#)AARRGGBB.").apply { addSuppressed(e) }
    }
}

fun <T> firstNotNull(vararg nullables: T?) = nullables.firstNotNullOf { it }
fun <T> Iterable<T?>.firstNotNull(): T = firstNotNullOf { it }

fun <T> List<T>.indexedForEach(consumer: BiConsumer<Int, T>) =
    this.forEachIndexed { index, t -> consumer.accept(index, t) }

fun String.toCamelCase(separator: String = "-"): String {
    return split(separator)
        .mapIndexed { i, part ->
            if (i == 0)
                part.lowercase()
            else
                part.replaceFirstChar { it.uppercase() }
        }.joinToString("")
}

/**
 * Sets the [KMutableProperty]'s value and then returns the new value.
 */
fun <T> KMutableProperty<T>.coalesce(newValue: T): T {
    setter.call(newValue)
    return newValue
}

fun MeteorColor.awt() = AwtColor(packed)
fun AwtColor.meteor() = MeteorColor(rgb)


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

fun <T : Any> optionalOf(value: T? = null): Optional<T> = if (value == null) Optional.empty() else Optional.of(value)

fun <T> invoking(func: () -> T): FunctionProperty<T> = FunctionProperty(func)
fun <T> invokingOrNull(func: () -> T): FunctionProperty<T?> = FunctionProperty { getOrNull(func) }

class FunctionProperty<T>(private val producer: () -> T) : ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = producer()
}

fun StringSetting.Builder.renderStarscript(): StringSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)
fun StringListSetting.Builder.renderStarscript(): StringListSetting.Builder =
    renderer(StarscriptTextBoxRenderer::class.java)

fun <P1, P2> Collection<Pair<P1, P2>>.associate() = associate { it }

fun IntSetting.Builder.saneSlider(): IntSetting.Builder = sliderRange(min, max)
fun DoubleSetting.Builder.saneSlider(): DoubleSetting.Builder = sliderRange(min, max)

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

infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

fun String.ensurePrefix(prefix: String, ignoreCase: Boolean = false): String {
    return if (startsWith(prefix, ignoreCase))
        this
    else "$prefix$this"
}

fun String.withoutPrefix(prefix: String, ignoreCase: Boolean = false): String {
    return if (startsWith(prefix, ignoreCase))
        substringAfter(prefix)
    else this
}

fun String.ensureSuffix(suffix: String, ignoreCase: Boolean = false): String {
    return if (endsWith(suffix, ignoreCase))
        this
    else "$this$suffix"
}

fun String.withoutSuffix(suffix: String, ignoreCase: Boolean = false): String {
    return if (endsWith(suffix, ignoreCase))
        substringBeforeLast(suffix)
    else this
}

/**
 * Parses a 6-character long hexadecimal sequence to a [Color] with or without the preceding #.
 */
fun parseHexColor(hex: String): Color = Color(
    optionalOf(hex.takeLast(6).uppercase()
        .takeIf { chars ->
            chars.all {
                it in '0'..'9' || it in 'A'..'F'
            }
        }
    ).orElseThrow { IllegalArgumentException("Illegal hexadecimal sequence.") }
        .toInt(16)
)

