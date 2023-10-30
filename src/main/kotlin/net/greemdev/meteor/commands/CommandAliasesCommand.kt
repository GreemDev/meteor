/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.sendCommand
import net.greemdev.meteor.util.text.textOf

object CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", {
        then("alias", arg.greedyString()) {
            suggests { matching(CommandAliases.aliases.keys) }
            alwaysRuns { ctx ->
                val name by ctx.argument(arg.greedyString(), "alias")
                val mapping = CommandAliases.find(name) ?: notFound.throwNew(name)

                minecraft.sendCommand(mapping.value)
            }
        }
    }, "ca"
)

private val notFound by CommandExceptions dynamic {
    textOf("No alias with the name '$it' was found.")
}
