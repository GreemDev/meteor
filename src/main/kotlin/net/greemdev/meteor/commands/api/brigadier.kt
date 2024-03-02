/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName") // API

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.future.future
import net.greemdev.meteor.*
import net.greemdev.meteor.util.scope
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

typealias MinecraftCommandContext = CommandContext<CommandSource>
typealias MinecraftLiteralBuilder = LiteralArgumentBuilder<CommandSource>
typealias MinecraftArgumentBuilder<T> = RequiredArgumentBuilder<CommandSource, T>

typealias CommandBuilder = BrigadierBuilder<MinecraftLiteralBuilder>
typealias ArgumentBuilder<T> = BrigadierBuilder<MinecraftArgumentBuilder<T>>

@DslMarker
annotation class BrigadierDsl

@BrigadierDsl
class BrigadierBuilder<T : com.mojang.brigadier.builder.ArgumentBuilder<CommandSource, T>>(val builder: T) {
    val ArgType = Arguments

    infix fun requires(predicate: Predicate<CommandSource>): BrigadierBuilder<T> {
        builder.requires(predicate)
        return this
    }

    fun then(literal: MinecraftLiteralBuilder): BrigadierBuilder<T> {
        builder.then(literal)
        return this
    }

    infix fun<A> then(requiredArgument: MinecraftArgumentBuilder<A>): BrigadierBuilder<T> {
        builder.then(requiredArgument)
        return this
    }

    fun then(name: String, builder: Initializer<CommandBuilder> = {}) =
        then(CommandBuilder(MinecraftLiteralBuilder.literal(name)).apply(builder).builder)
    infix fun then(command: Pair<String, Initializer<CommandBuilder>>) = then(command.first, command.second)
    fun<A> then(name: String, argType: ArgumentType<A>, builder: Initializer<ArgumentBuilder<A>> = {}) =
        then(BrigadierBuilder(MinecraftArgumentBuilder.argument(name, argType)).apply(builder).builder)
    fun<A> then(argType: ArgumentType<A>, builder: Initializer<ArgumentBuilder<A>> = {}) = then(formatArgType(argType), argType, builder)

    /** Provide a Brigadier executes block via a lambda returning an integer representing the number of executions completed. Typical result is [Command.SINGLE_SUCCESS]. */
    infix fun executes(command: MinecraftCommandContext.() -> Int): BrigadierBuilder<T> {
        builder.executes(command)
        return this
    }

    /** Provide a Brigadier executes block via a lambda returning a boolean. If the result is true, [Command.SINGLE_SUCCESS] is returned, otherwise, 0 is. */
    infix fun canRun(command: MinecraftCommandContext.() -> Boolean) =
        executes { command().asInt() }

    /**
     * Provide a Brigadier executes block via a lambda that can throw an error, returning [Command.SINGLE_SUCCESS] on success, and 0 on an exception.
     *
     * [errorLogger] allows you to pipe the thrown exception (if there is one) into a logger or other way of informing the user about what happened instead of silentfail.
     * [GCommand.catching] is provided for this purpose to show the error to the player & log it. (`triesRunning(::catching) {...}`)
     */
    fun triesRunning(errorLogger: ValueAction<Throwable> = { }, command: Initializer<MinecraftCommandContext>) =
        canRun {
            this@canRun.runCatching(command) // Brigadier has special handling for CommandSyntaxExceptions; so they shouldn't be routed through errorLogger or caught
                .onFailureOf(CommandSyntaxException::class) { throw it }
                .onFailure(errorLogger)
                .isSuccess
        }

    /** Provide a Brigadier executes block via a lambda. The executes block always results in [Command.SINGLE_SUCCESS]. */
    infix fun runs(command: Initializer<MinecraftCommandContext>) =
        executes { command(); Command.SINGLE_SUCCESS }
}

fun<T> BrigadierBuilder<MinecraftArgumentBuilder<T>>.suggests(
    suggestionProvider: SuggestionsBuilder.(ctx: MinecraftCommandContext) -> CompletableFuture<Suggestions>
): ArgumentBuilder<T> {
    builder.suggests { context, builder -> builder.suggestionProvider(context) }
    return this
}

infix fun<T> BrigadierBuilder<MinecraftArgumentBuilder<T>>.suggestsAsync(
    suggestionProvider: suspend SuggestionsBuilder.(ctx: MinecraftCommandContext) -> Suggestions
) =
    suggests { scope.future { suggestionProvider(it) } }
