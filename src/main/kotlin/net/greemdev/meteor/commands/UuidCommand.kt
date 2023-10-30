/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.argument
import net.greemdev.meteor.util.*

object UuidCommand : GCommand("uuid", "Get the UUID of a player or yourself.", {
    alwaysRuns {
        info("Your UUID is '${minecraft.player?.uuid}'")
    }
    then(arg.player()) {
        alwaysRuns {
            val player by it.argument(arg.player())
            info("${player.gameProfile.name}'s UUID is '${player.uuid}'")
        }
    }
})
