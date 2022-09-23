/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.*
import net.minecraft.util.math.Vec3d

/*
 * Kotlin reimplementation of the previously divided HClip and VClip commands.
 * Reimplemented because there's no real need to have 2 functionally similar commands be separate.
 */
class ClipCommand : GCommand("clip", "Lets you clip through blocks vertically or horizontally.", {
    then("v") {
        then("distance", arg.double()) {
            alwaysRuns {
                val distance by it(arg.double(), "distance")
                mc.player?.vehicle?.editPos(y = distance)
                mc.player().editPos(y = distance)
            }
        }
    }
    then("h") {
        then("distance", arg.double()) {
            alwaysRuns {
                val distance by it(arg.double(), "distance")
                val forward = Vec3d.fromPolar(0f, mc.player().yaw).normalize()
                mc.player?.vehicle?.editPos(
                    x = forward.x * distance,
                    z = forward.z * distance
                )
                mc.player().editPos(
                    x = forward.x * distance,
                    z = forward.z * distance
                )
            }
        }
    }
})
