/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.hud.notification.Notification
import net.greemdev.meteor.hud.notification.NotificationBuilder
import net.greemdev.meteor.hud.notification.notification
import net.greemdev.meteor.modules.greteor.CommandAliases
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.sendCommand
import net.greemdev.meteor.util.text.textOf
import net.greemdev.meteor.util.text.ChatColor

object CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", {
        then("alias", arg.greedyString()) {
            suggests { matching(CommandAliases.aliases.keys) }
            alwaysRuns { ctx ->
                val name by ctx.argument(arg.greedyString(), "alias")
                val mapping = CommandAliases.find(name) ?: notFound.throwNew(name)

                notification {
                    title = "&zCommand Aliases"
                    description = "Executing '${mapping.value.ensurePrefix("/")}'"
                    color = ChatColor.gold.asMeteor()

                    textMapper(onlyDescription)
                    fallbackPredicate(CommandAliases::chatFeedback)
                    fallback(ChatUtils::sendMsg)
                }.sendOrFallback()

                mc.sendCommand(mapping.value)
            }
        }
    }, "ca"
)

private val notFound by CommandExceptions dynamic {
    textOf("No alias with the name '$it' was found.")
}
