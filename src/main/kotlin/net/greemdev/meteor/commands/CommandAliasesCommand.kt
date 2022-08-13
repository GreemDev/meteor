/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import meteordevelopment.meteorclient.systems.commands.Command
import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.Meteor
import net.greemdev.meteor.util.ensurePrefix
import net.minecraft.command.CommandSource
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.concurrent.CompletableFuture

class CommandAliasesCommand : Command(
    "command-aliases", "Configured by the module of the same name.",
    "ca"
) {
    companion object {
        private fun notFound(name: String) =
            SimpleCommandExceptionType(Text.literal("No alias with the name '$name' was found."))
    }

    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        builder.then(argument("alias", StringArgumentType.word())
            .suggests { _, sb ->
                CommandSource.suggestMatching(Meteor.module<CommandAliases>().mapped.keys, sb)
            }
            .executes { ctx ->
                val name = StringArgumentType.getString(ctx, "alias")
                val mapping = Meteor.module<CommandAliases>().mapped.entries.firstOrNull {
                    it.key.equals(name, true)
                } ?: throw notFound(name).create()

                if (Meteor.module<CommandAliases>().chatFeedback)
                    info("Executing command '${mapping.value.ensurePrefix("/")}'")

                mc.player?.sendCommand(mapping.value)
                SINGLE_SUCCESS
            }
        )
    }
}
