/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.meteor.KeyEvent
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent
import meteordevelopment.meteorclient.utils.misc.Keybind
import meteordevelopment.meteorclient.utils.misc.input.KeyAction
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.event.GameInputEvent
import net.greemdev.meteor.util.*

// Based on https://github.com/AntiCope/meteor-rejects/blob/master/src/main/java/anticope/rejects/modules/Boost.java
class Dash : GModule("dash", "Boosts you forward in the direction you're looking.") {

    val power by sg double {
        name("power")
        description("The strength of your dash.")
        defaultValue(2.75)
        min(0.5)
        max(10.0)
        saneSlider()
    }

    val activation by sg keybind {
        name("activate")
        description("Perform the dash.")
        defaultValue(Keybind.none())
    }

    val allowRepeat by sg bool {
        name("allow-hold")
        description("Allows you to dash repeatedly by holding down the button.")
        defaultValue(false)
        visible { mc.isInSingleplayer } //repeat is too fast for servers and never works
    }

    private fun onKeybindPress(event: GameInputEvent) {
        if (any(
                !allowRepeat() and (event.isAction(KeyAction.Repeat) or event.isAction(KeyAction.Release)),
                allowRepeat() and event.isAction(KeyAction.Release),
                isActive && mc.inGameHud.chatHud.chatScreen != null
        )) return

        if (isActive and event.matches(activation()))
            mc.player() + mc.rotationVecClient().multiply(power())
    }

    @EventHandler
    fun onKey(event: KeyEvent) {
        onKeybindPress(GameInputEvent(event))
    }

    @EventHandler
    fun onMouse(event: MouseButtonEvent) {
        onKeybindPress(GameInputEvent(event))
    }
}
