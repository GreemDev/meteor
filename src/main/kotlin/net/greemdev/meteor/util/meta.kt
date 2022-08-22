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
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.message.MessageFactory
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
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

fun <T> tryOrIgnore(func: () -> Unit) = try {
    func()
} catch (ignored: Throwable) {
}

fun <T> runOrIgnore(runnable: Runnable) = try {
    runnable.run()
} catch (ignored: Throwable) {
}

// Looks repetitive however each different type we check for has its own special logic in LogManager
fun log4j(value: Any) = lazy {
    when (value) {
        is String -> LogManager.getLogger(value)
        is Class<*> -> LogManager.getLogger(value)
        is KClass<*> -> LogManager.getLogger(value.java)
        is MessageFactory -> LogManager.getLogger(value)
        else -> LogManager.getLogger(value)
    }
}

operator fun FabricLoader.contains(modId: String) = modLoader.isModLoaded(modId)

fun<T> Collection<T>?.getOrEmpty() = this ?: emptySet()

fun textOf(content: String? = null, block: (MutableText.() -> Unit)?): MutableText = if (content == null)
    Text.empty()
else
    Text.literal(content).apply { block?.invoke(this) }

fun textOf(content: String?) = textOf(content, null)
fun textOf() = textOf(null, null)

fun<T> List<T>.indexedForEach(consumer: BiConsumer<Int, T>) = this.forEachIndexed { index, t -> consumer.accept(index, t) }

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

fun String.ensurePrefix(prefix: String): String {
    return if (startsWith(prefix))
        this
    else "$prefix$this"
}

fun String.ensureSuffix(suffix: String): String {
    return if (endsWith(suffix))
        this
    else "$this$suffix"
}
