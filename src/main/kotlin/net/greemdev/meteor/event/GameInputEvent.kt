/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.event

import meteordevelopment.meteorclient.events.Cancellable
import meteordevelopment.meteorclient.events.meteor.KeyEvent
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent
import meteordevelopment.meteorclient.utils.misc.Keybind
import meteordevelopment.meteorclient.utils.misc.input.KeyAction
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.util.invoking

class GameInputEvent private constructor(private var backingEvent: Cancellable) : Cancellable() {

    //both upcast as Cancellable to prevent recursion
    constructor(e: MouseButtonEvent) : this(e as Cancellable)
    constructor(e: KeyEvent) : this(e as Cancellable)

    fun keyId() =
        if (isKey)
            asKey.key
        else
            asMouse.button


    fun action(): KeyAction = when {
        isKey -> asKey.action
        else -> asMouse.action
    }

    infix fun actionIs(action: KeyAction) = action == action()

    infix operator fun contains(keybind: Keybind) = keybind.matches(isKey, keyId())

    val isKey by invoking { backingEvent is KeyEvent }
    val asMouse by invoking {
        if (!isKey)
            backingEvent as MouseButtonEvent
        else error("backing event is a keyboard input, not a mouse input")
    }
    val asKey by invoking {
        if (isKey)
            backingEvent as KeyEvent
        else error("backing event is a mouse input, not a keyboard input")
    }

    override fun cancel() {
        backingEvent.cancel()
    }
}
