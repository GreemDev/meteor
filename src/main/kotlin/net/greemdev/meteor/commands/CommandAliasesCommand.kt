/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.*
import net.minecraft.command.CommandSource

class CommandAliasesCommand : GCommand(
    "command-aliases", "Configured by the module of the same name.", "ca"
) {
    companion object {
        private val notFound by dynamicCommandException {
            textOf("No alias with the name '$it' was found.")
        }
    }

    override fun CommandBuilder.build() {
        then("alias", arg.wordString()) {
            suggests {
                CommandSource.suggestMatching(Meteor.module<CommandAliases>().mapped.keys, this)
            }
            alwaysRuns(this@CommandAliasesCommand::executeCommand)
        }
    }

    @Throws(CommandSyntaxException::class)
    private fun executeCommand(ctx: MinecraftCommandContext) {
        val name = ctx.argument<String>("alias")
        val mapping = Meteor.module<CommandAliases>().mapped.entries.firstOrNull {
            it.key.equals(name, true)
        } ?: throw notFound.create(name)

        if (Meteor.module<CommandAliases>().chatFeedback)
            info("Executing '${mapping.value.ensurePrefix("/")}'")

        mc.sendCommand(mapping.value)
    }
}
