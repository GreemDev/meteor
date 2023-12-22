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

object CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", {
        then("alias", ArgType.quotableString()) {
            suggests { matching(CommandAliases.aliases.keys) }
            runs {
                val name by contextArg("alias", ArgType.quotableString())

                minecraft.sendCommand(
                    CommandAliases.find(name)
                        ?: notFound.throwNew(name)
                )
            }
            then("args", ArgType.greedyString()) {
                runs {
                    val name by contextArg("alias", ArgType.quotableString())
                    val args by contextArg("args", ArgType.greedyString())

                    var formattedCommand = CommandAliases.find(name) ?: notFound.throwNew(name)
                    if ("%s" in formattedCommand)
                        formattedCommand = formattedCommand.format(args)

                    minecraft.sendCommand(formattedCommand)
                }
            }
        }
    }, "ca"
)

private val notFound by CommandExceptions.dynamic<String> {
    addString("No alias with the name '$it' was found.")
}
