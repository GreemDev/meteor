/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.contextArg
import net.greemdev.meteor.util.*

object UuidCommand : GCommand("uuid", "Get the UUID of a player or yourself.", {
    runs {
        info("Your UUID is '${minecraft.player?.uuid}'")
    }
    then(ArgType.player()) {
        runs {
            val player by contextArg(ArgType.player())
            info("${player.gameProfile.name}'s UUID is '${player.uuid}'")
        }
    }
})
