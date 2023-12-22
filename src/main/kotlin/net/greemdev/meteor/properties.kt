/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("NOTHING_TO_INLINE", "unused")
@file:JvmName("Property")

package net.greemdev.meteor

import java.util.function.BiConsumer
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> invoking(noinline func: Getter<T>) = ReadOnlyProperty<Any?, T> { _, _ -> func() }
inline fun <T> invokingOrNull(noinline func: Getter<T>) = ReadOnlyProperty<Any?, T?> { _, _ -> getOrNull(func) }

class Observable<T>(
    private var value: T,
    private val observers: MutableList<BiValueAction<T, T>>
) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    fun get() = value

    fun set(value: T) {
        val old = this.value
        this.value = value
        observers.forEach { it(old, value) }
    }
}

@FunctionalInterface
interface Observer<T> : BiValueAction<T, T> {
    override operator fun invoke(old: T, new: T) = valueChanged(old, new)

    fun valueChanged(old: T, new: T)

    companion object {
        operator fun<T> invoke(
            action: BiValueAction<T, T>
        ) = object : Observer<T> {
            override fun valueChanged(old: T, new: T) = action(old, new)
        }
    }
}

fun<T> observer(onValueChanged: BiValueAction<T, T>) = Observer(onValueChanged)

fun <T> observable(value: T, observer: BiValueAction<T, T>, vararg otherObservers: BiValueAction<T, T> = arrayOf()) =
    Observable(value, mutableListOf(observer).apply { addAll(otherObservers) })

@Suppress("ClassName") // intended use is `import static`ing from java
object observation {
    @JvmName("observer")
    @JvmStatic
    fun<T> create(onValueChanged: BiConsumer<T, T>) = observer(onValueChanged.kotlin)

    @JvmName("observable")
    @JvmStatic
    fun<T> createObservable(value: T, observer: Observer<T>, vararg otherObservers: Observer<T>) =
        observable(value, observer, *otherObservers)
}
