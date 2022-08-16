/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import com.mojang.brigadier.context.CommandContext
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.minecraft.command.CommandSource
import net.minecraft.util.math.Vec3d

/*
 * Kotlin reimplementation of the previously divided HClip and VClip commands.
 * Reimplemented because there's no real need to have 2 functionally similar commands be separate.
 */
class ClipCommand : GCommand("clip", "Lets you clip through blocks vertically or horizontally.") {

    override fun CommandBuilder.build() {
        then(command("v") {
            then("distance", arg.double()) {
                alwaysRuns(this@ClipCommand::vertical)
            }
        }) then command("h") {
            then("distance", arg.double()) {
                alwaysRuns(this@ClipCommand::horizontal)
            }
        }
    }

    private fun vertical(ctx: MinecraftCommandContext) {
        val distance = ctx.argument<Double>("distance")
        if (mc.player!!.hasVehicle()) {
            val vehicle = mc.player!!.vehicle!!
            vehicle.setPos(vehicle.x, vehicle.y + distance, vehicle.z)
        }
        mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + distance, mc.player!!.z)
    }

    private fun horizontal(ctx: MinecraftCommandContext) {
        val distance = ctx.argument<Double>("distance")
        val forward = Vec3d.fromPolar(0f, mc.player!!.yaw).normalize()
        if (mc.player!!.hasVehicle()) {
            val vehicle = mc.player!!.vehicle!!
            vehicle.setPos(vehicle.x + forward.x * distance, vehicle.y, vehicle.z + forward.z * distance)
        }
        mc.player!!.setPosition(
            mc.player!!.x + forward.x * distance,
            mc.player!!.y,
            mc.player!!.z + forward.z * distance
        )
    }
}
