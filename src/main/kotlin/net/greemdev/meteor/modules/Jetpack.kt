/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.entity.player.OffGroundSpeedEvent
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.meteor.double
import net.greemdev.meteor.util.misc.player
import net.greemdev.meteor.util.misc.setY

// Based on https://github.com/AntiCope/meteor-rejects/blob/master/src/main/java/anticope/rejects/modules/Jetpack.java
object Jetpack : GModule.Movement("jetpack", "Boosts upward as if flying with a jetpack while holding the jump key.") {
    val speed by sg double {
        name("jetpack-speed")
        description("How fast to ascend while holding the jump key.")
        defaultValue(0.42)
        range(0.1, 5.0)
    }

    @EventHandler
    private fun onTick(ignored: TickEvent.Pre) {
        if (mc.options.jumpKey.isPressed)
            mc.player().velocity.setY(speed())
    }

    @EventHandler
    private fun changeOffGroundSpeed(event: OffGroundSpeedEvent) {
        event.speed = mc.player().movementSpeed * speed.valueAsFloat
    }
}
