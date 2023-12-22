/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type.interop

import net.greemdev.meteor.Getter
import java.util.function.Supplier

/** Simple class wrapping a Kotlin [lazy] property delegate for use from Java code. */
class JLazy<T>(getter: Getter<T>) : Supplier<T> {
    private val backing by lazy(getter)
    override fun get() = backing


    companion object {
        @JvmStatic
        fun<T> getting(getter: Getter<T>) = JLazy(getter)
    }
}
