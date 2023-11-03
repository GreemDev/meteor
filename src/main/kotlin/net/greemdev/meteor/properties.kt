/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.greemdev.meteor

import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

inline fun <T> invoking(noinline func: Getter<T>) = ReadOnlyProperty<Any?, T> { _, _ -> func() }
inline fun <T> invokingOrNull(noinline func: Getter<T>) = ReadOnlyProperty<Any?, T?> { _, _ -> getOrNull(func) }

fun <T> observable(value: T, observer: ValueAction<T>, vararg otherObservers: ValueAction<T>) =
    Observable(value, otherObservers.toMutableList()).apply { observers.add(observer) }

class Observable<T>(private var value: T, val observers: MutableList<ValueAction<T>>) : ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = value
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = set(value)

    fun get() = value

    fun set(value: T) {
        this.value = value
        observers.forEach { it(value) }
    }
}

@FunctionalInterface
interface Observer<T> : ValueAction<T> {
    override operator fun invoke(arg: T) = valueChanged(arg)

    fun valueChanged(value: T)
}
