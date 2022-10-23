/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.hud.notification.Notification
import net.greemdev.meteor.hud.notification.notifications
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.sendCommand
import net.greemdev.meteor.util.text.textOf
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.text.ChatColor
import net.minecraft.command.CommandSource

object CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", {
        then("alias", arg.greedyString()) {
            suggests {
                CommandSource.suggestMatching(CommandAliases.mapped.keys, this)
            }
            alwaysRuns { ctx ->
                val name by ctx(arg.greedyString(),"alias")
                val mapping = CommandAliases.find(name) ?: notFound.throwNew(name)

                Notification.command("&zCommand Aliases", "Executing '${mapping.value.ensurePrefix("/")}'", ChatColor.gold.asMeteor())
                    .sendOrRun {
                        if (CommandAliases.chatFeedback)
                            ChatUtils.info(description)
                    }

                notifications.sendOrRun(
                    Notification.command("&zCommand Aliases", "Executing '${mapping.value.ensurePrefix("/")}'", ChatColor.gold.asMeteor()),
                    "Executing '${mapping.value.ensurePrefix("/")}'") {
                    if (CommandAliases.chatFeedback)
                        ChatUtils.info(it)
                }

                mc.sendCommand(mapping.value)
            }
        }
    }, "ca"
)

private val notFound by dynamicCommandException {
    textOf("No alias with the name '$it' was found.")
}
