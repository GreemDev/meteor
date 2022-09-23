/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.sendCommand
import net.greemdev.meteor.util.text.textOf
import net.greemdev.meteor.util.meteor.*
import net.minecraft.command.CommandSource

class CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", {
        then("alias", arg.greedyString()) {
            suggests {
                CommandSource.suggestMatching(Meteor.module<CommandAliases>().mapped.keys, this)
            }
            alwaysRuns { ctx ->
                val name by ctx(arg.greedyString(),"alias")
                val mapping = Meteor.module<CommandAliases>().mapped.entries.firstOrNull {
                    it.key.equals(name, true)
                } ?: notFound.throwNew(name)

                if (Meteor.module<CommandAliases>().chatFeedback)
                    ChatUtils.info("Executing '${mapping.value.ensurePrefix("/")}'")

                mc.sendCommand(mapping.value)
            }
        }
    }, "ca"
) {
    companion object {
        private val notFound by dynamicCommandException {
            textOf("No alias with the name '$it' was found.")
        }
    }
}
