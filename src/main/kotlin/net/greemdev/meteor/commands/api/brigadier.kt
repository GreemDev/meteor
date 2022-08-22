/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.CommandSource

typealias MinecraftCommandContext = CommandContext<CommandSource>
typealias MinecraftLiteralBuilder = LiteralArgumentBuilder<CommandSource>
typealias MinecraftArgumentBuilder<T> = RequiredArgumentBuilder<CommandSource, T>

fun command(name: String, block: CommandBuilder.() -> Unit = {}) =
    CommandBuilder(LiteralArgumentBuilder.literal(name))
        .apply(block).builder
