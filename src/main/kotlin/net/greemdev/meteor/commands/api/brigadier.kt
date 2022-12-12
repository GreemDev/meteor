/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.greemdev.meteor.Initializer
import net.minecraft.command.CommandSource

typealias MinecraftCommandContext = CommandContext<CommandSource>
typealias MinecraftLiteralBuilder = LiteralArgumentBuilder<CommandSource>
typealias MinecraftArgumentBuilder<T> = RequiredArgumentBuilder<CommandSource, T>
typealias arg = Arguments

fun literal(text: String): MinecraftLiteralBuilder = MinecraftLiteralBuilder.literal(text)
fun<T> argument(name: String, type: ArgumentType<T>): MinecraftArgumentBuilder<T> = MinecraftArgumentBuilder.argument(name, type)

fun command(name: String, block: Initializer<CommandBuilder> = {}) =
    CommandBuilder(literal(name)).apply(block).builder
