/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import net.greemdev.meteor.*
import net.greemdev.meteor.util.scope
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

typealias MinecraftCommandContext = CommandContext<CommandSource>
typealias MinecraftLiteralBuilder = LiteralArgumentBuilder<CommandSource>
typealias MinecraftArgumentBuilder<T> = RequiredArgumentBuilder<CommandSource, T>

typealias CommandBuilder = BrigadierBuilder<MinecraftLiteralBuilder>
typealias ArgumentBuilder<T> = BrigadierBuilder<MinecraftArgumentBuilder<T>>
private typealias BArgBuilder<S, T> = com.mojang.brigadier.builder.ArgumentBuilder<S, T>

fun literal(text: String): MinecraftLiteralBuilder = MinecraftLiteralBuilder.literal(text)
fun<T> argument(name: String, type: ArgumentType<T>): MinecraftArgumentBuilder<T> = MinecraftArgumentBuilder.argument(name, type)

fun command(name: String, block: Initializer<CommandBuilder> = {}) =
    CommandBuilder(literal(name)).apply(block).builder

data class BrigadierBuilder<T : BArgBuilder<CommandSource, T>>(val builder: T) {

    val arg = Arguments

    infix fun requires(predicate: Predicate<CommandSource>): BrigadierBuilder<T> {
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

    /** Provide a Brigadier executes block via a lambda returning an integer representing the number of executions completed. Typical result is [Command.SINGLE_SUCCESS]. */
    infix fun runs(command: (ctx: MinecraftCommandContext) -> Int): BrigadierBuilder<T> {
        builder.executes(command)
        return this
    }
    /** Provide a Brigadier executes block via a lambda returning a boolean. If the result is true, [Command.SINGLE_SUCCESS] is returned, otherwise, 0 is. */
    infix fun canRun(command: (ctx: MinecraftCommandContext) -> Boolean) =
        runs { if (command(it)) Command.SINGLE_SUCCESS else 0 }

    /**
     * Provide a Brigadier executes block via a lambda that can throw an error, returning [Command.SINGLE_SUCCESS] on success, and 0 on an exception.
     *
     * [errorLogger] allows you to pipe the thrown exception (if there is one) into a logger or other way of informing the user about what happened instead of silentfail.
     * [GCommand.catching] is provided for this purpose to show the error to the player & log it. (`triesRunning(::catching) {}`)
     */
    fun triesRunning(errorLogger: Initializer<Throwable> = { }, command: (ctx: MinecraftCommandContext) -> Unit) =
        runs { it.runCatching(command).onFailure(errorLogger).isSuccess.asInt() }
    /** Provide a Brigadier executes block via a lambda. The executes block always results in [Command.SINGLE_SUCCESS]. */
    infix fun alwaysRuns(command: (ctx: MinecraftCommandContext) -> Unit) =
        runs { ctx -> Command.SINGLE_SUCCESS.also { command(ctx) }}
}
