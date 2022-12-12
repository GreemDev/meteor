/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture
import net.greemdev.meteor.Initializer
import net.greemdev.meteor.util.scope
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

typealias CommandBuilder = BrigadierBuilder<MinecraftLiteralBuilder>
typealias ArgumentBuilder<T> = BrigadierBuilder<MinecraftArgumentBuilder<T>>
private typealias BArgBuilder<S, T> = com.mojang.brigadier.builder.ArgumentBuilder<S, T>

data class BrigadierBuilder<T : BArgBuilder<CommandSource, T>>(val builder: T) {

    val arg = Arguments

    infix fun requires(predicate: CommandSource.() -> Boolean): BrigadierBuilder<T> {
        builder.requires(predicate)
        return this
    }
    infix fun then(literal: MinecraftLiteralBuilder): BrigadierBuilder<T> {
        builder.then(literal)
        return this
    }

    infix fun<A> then(builder: MinecraftArgumentBuilder<A>): BrigadierBuilder<T> {
        this.builder.then(builder)
        return this
    }
    fun then(name: String, builder: Initializer<CommandBuilder> = {}) = then(command(name, builder))
    infix fun then(command: Pair<String, Initializer<CommandBuilder>>) = then(command(command.first, command.second))
    fun<A> then(name: String, argType: ArgumentType<A>, builder: Initializer<ArgumentBuilder<A>> = {}) =
        then(BrigadierBuilder(argument(name, argType)).apply(builder).builder)
    fun<A> then(argType: ArgumentType<A>, builder: Initializer<ArgumentBuilder<A>> = {}) = then(formatArgType(argType), argType, builder)

    infix fun suggests(suggestionProvider: SuggestionsBuilder.(ctx: MinecraftCommandContext) -> CompletableFuture<Suggestions>): BrigadierBuilder<T> {
        if (builder is RequiredArgumentBuilder<*, *>) {
            builder.suggests { context, builder ->
                @Suppress("UNCHECKED_CAST") //commands in minecraft should always be of this type, period
                builder.suggestionProvider(context as MinecraftCommandContext)
            }
        }
        return this
    }

    infix fun suggestsAsync(suggestionProvider: suspend SuggestionsBuilder.(ctx: MinecraftCommandContext) -> Suggestions) =
        suggests { scope.async { suggestionProvider(it) }.asCompletableFuture() }

    infix fun runs(command: (ctx: MinecraftCommandContext) -> Int): BrigadierBuilder<T> {
        builder.executes(command)
        return this
    }
    infix fun canRun(command: (ctx: MinecraftCommandContext) -> Boolean) =
        runs { ctx -> if (command(ctx)) Command.SINGLE_SUCCESS else 0 }

    infix fun alwaysRuns(command: (ctx: MinecraftCommandContext) -> Unit) =
        runs { ctx -> Command.SINGLE_SUCCESS.also { command(ctx) }}
}
