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
import net.greemdev.meteor.util.invoking

class GameInputEvent private constructor(private var backingEvent: Cancellable) : Cancellable() {

    //both upcast as Cancellable to prevent recursion
    constructor(e: MouseButtonEvent) : this(e as Cancellable)
    constructor(e: KeyEvent) : this(e as Cancellable)

    fun button() =
        if (isKey)
            asKey.key
        else
            asMouse.button


    fun action(): KeyAction = when {
        isKey -> asKey.action
        else -> asMouse.action
    }

    infix fun actionIs(action: KeyAction) = action == action()

    fun wasReleased() = actionIs(KeyAction.Release)
    fun wasPressed() = actionIs(KeyAction.Press)
    fun isHeld() = actionIs(KeyAction.Repeat)

    infix operator fun contains(keybind: Keybind) = keybind.matches(isKey, button())

    val isKey by invoking { backingEvent is KeyEvent }
    val asMouse by invoking {
        backingEvent as? MouseButtonEvent ?: error("backing event is a keyboard input, not a mouse input")
    }
    val asKey by invoking {
        backingEvent as? KeyEvent ?: error("backing event is a mouse input, not a keyboard input")
    }

    override fun cancel() = backingEvent.cancel()
}
