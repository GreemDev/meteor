/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import meteordevelopment.meteorclient.systems.commands.Command
import net.minecraft.command.CommandSource
import net.minecraft.util.math.Vec3d

/*
 * Kotlin reimplementation of the previously divided HClip and VClip commands.
 * Reimplemented because there's no real need to have 2 functionally similar commands be separate.
 */
class ClipCommand : Command("clip", "Lets you clip through blocks vertically or horizontally.") {
    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        builder.then(
            literal("v")
                .then(argument("distance", DoubleArgumentType.doubleArg())
                    .executes(this::vertical))
        ).then(
            literal("h")
                .then(argument("distance", DoubleArgumentType.doubleArg())
                    .executes(this::horizontal))
        )
    }

    private fun vertical(ctx: CommandContext<CommandSource>): Int {
        val distance = ctx.getArgument("distance", Double::class.java)
        if (mc.player!!.hasVehicle()) {
            val vehicle = mc.player!!.vehicle!!
            vehicle.setPos(vehicle.x, vehicle.y + distance, vehicle.z)
        }
        mc.player!!.setPosition(mc.player!!.x, mc.player!!.y + distance, mc.player!!.z)
        return SINGLE_SUCCESS
    }

    private fun horizontal(ctx: CommandContext<CommandSource>): Int {
        val distance = ctx.getArgument("distance", Double::class.java)
        val forward = Vec3d.fromPolar(0f, mc.player!!.yaw).normalize()
        if (mc.player!!.hasVehicle()) {
            val vehicle = mc.player!!.vehicle!!
            vehicle.setPos(vehicle.x + forward.x * distance, vehicle.y, vehicle.z + forward.z * distance)
        }
        mc.player!!.setPosition(mc.player!!.x + forward.x * distance, mc.player!!.y, mc.player!!.z + forward.z * distance)
        return SINGLE_SUCCESS
    }
}
