/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.invoke
import net.minecraft.client.gui.screen.Screen

// Based on https://github.com/AntiCope/meteor-rejects/blob/master/src/main/java/anticope/rejects/modules/Boost.java
object Dash : GModule("dash", "Boosts you forward in the direction you're looking.") {
    val power by sg double {
        name("power")
        description("The strength of your dash.")
        defaultValue(2.75)
        range(0.5, 10.0)
    }

    val autoDash by sg bool {
        name("auto-dash")
        description("Automatically dashes based on the defined interval.")
        defaultValue(false)
    }

    val autoDashInterval by sg int {
        name("auto-dash-interval")
        description("Boost interval in ticks.")
        visible(autoDash)
        defaultValue(20)
        range(10, 300)
    }

    init {
        chatFeedback = false
        allowChatFeedback = false
    }

    private var timer = 0

    override fun onActivate() {
        timer = autoDashInterval()

        if (!autoDash()) {
            if (mc.player != null) {
                when (mc.currentScreen) {
                    is Screen -> info("Did not activate dash because you're in a menu. Please use the keybind in-game.")
                    else -> dash()
                }
            }

            toggle()
        }
    }

    @EventHandler
    private fun onTick(event: TickEvent.Pre) {
        if (!autoDash()) return
        if (timer < 1) {
            dash()
            timer = autoDashInterval()
        } else {
            timer--;
        }
    }

    private fun dash() {
        mc.player() + mc.clientRotationVec() * power()
    }
}
