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
    fun then(name: String, builder: CommandBuilder.() -> Unit = {}) = then(command(name, builder))
    infix fun then(command: Pair<String, CommandBuilder.() -> Unit>) = then(command(command.first, command.second))
    fun<A> then(name: String, argType: ArgumentType<A>, builder: ArgumentBuilder<A>.() -> Unit = {}) =
        then(BrigadierBuilder(argument(name, argType)).apply(builder).builder)
    fun<A> then(argType: ArgumentType<A>, builder: ArgumentBuilder<A>.() -> Unit = {}) = then(formatArgType(argType), argType, builder)

    infix fun suggests(suggestionProvider: SuggestionsBuilder.(MinecraftCommandContext) -> CompletableFuture<Suggestions>): BrigadierBuilder<T> {
        if (builder is RequiredArgumentBuilder<*, *>) {
            builder.suggests { context, builder ->
                @Suppress("UNCHECKED_CAST") //commands in minecraft should always be of this type, period
                builder.suggestionProvider(context as MinecraftCommandContext)
            }
        }
        return this
    }

    infix fun suggestsAsync(suggestionProvider: suspend SuggestionsBuilder.(MinecraftCommandContext) -> Suggestions): BrigadierBuilder<T> {
        if (builder is RequiredArgumentBuilder<*, *>) {
            builder.suggests { context, builder ->
                @Suppress("UNCHECKED_CAST") //commands in minecraft should always be of this type, period
                scope.async {
                    builder.suggestionProvider(context as MinecraftCommandContext)
                }.asCompletableFuture()
            }
        }
        return this
    }

    infix fun runs(command: (MinecraftCommandContext) -> Int): BrigadierBuilder<T> {
        builder.executes(command)
        return this
    }
    infix fun canRun(command: (MinecraftCommandContext) -> Boolean): BrigadierBuilder<T> {
        builder.executes { ctx -> if (command(ctx)) Command.SINGLE_SUCCESS else 0 }
        return this
    }
    infix fun alwaysRuns(command: (MinecraftCommandContext) -> Unit): BrigadierBuilder<T> {
        builder.executes { ctx -> 1.also { command(ctx) }}
        return this
    }
}
