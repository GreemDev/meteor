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

class GameInputEvent private constructor(private var backingEvent: Any) : Cancellable() {

    constructor(e: MouseButtonEvent) : this(e as Any)
    constructor(e: KeyEvent) : this(e as Any)

    fun keyId() =
        if (isKey)
            asKey.key
        else if (isMouse)
            asMouse.button
        else error("shouldn't happen")


    fun action(): KeyAction =
        if (isKey)
            asKey.action
        else if (isMouse)
            asMouse.action
        else error("shouldn't happen")

    fun isAction(action: KeyAction) = action == action()

    fun matches(keybind: Keybind) = keybind.matches(isKey, keyId())



    val isKey by invoking { backingEvent is KeyEvent }
    val isMouse by invoking { backingEvent is MouseButtonEvent }
    val asMouse by invoking {
        if (isMouse)
            backingEvent as MouseButtonEvent
        else error("backing event is a keyboard input, not a mouse input")
    }
    val asKey by invoking {
        if (isKey)
            backingEvent as KeyEvent
        else error("backing event is a mouse input, not a keyboard input")
    }
}

interface GameInputHandler {
    fun onGameInput(event: GameInputEvent)

    @EventHandler
    private fun onKey(e: KeyEvent) {
        onGameInput(GameInputEvent(e))
    }

    @EventHandler
    private fun onMouse(e: MouseButtonEvent) {
        onGameInput(GameInputEvent(e))
    }
}
