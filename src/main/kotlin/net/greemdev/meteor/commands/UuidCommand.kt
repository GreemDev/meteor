/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.misc.*
import net.minecraft.entity.player.PlayerEntity

object UuidCommand : GCommand("uuid", "Get the UUID of a player or yourself.") {
    override fun CommandBuilder.inject() {
        alwaysRuns(::selfUUID)
        then(arg.player()) {
            alwaysRuns {
                val player by it(arg.player())
                playerUUID(player)
            }
        }
    }

    private fun selfUUID(ctx: MinecraftCommandContext) {
        info("Your UUID is '${mc.player().uuid}'")
    }

    private fun playerUUID(player: PlayerEntity) {
        info("${player.gameProfile.name}'s UUID is '${player.uuid}'")
    }
}
