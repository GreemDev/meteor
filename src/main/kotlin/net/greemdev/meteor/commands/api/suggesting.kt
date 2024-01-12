/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Suggest")
package net.greemdev.meteor.commands.api

import com.mojang.brigadier.Message
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.greemdev.meteor.Mapper
import net.greemdev.meteor.Predicate
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.command.CommandSource.RelativePosition
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

// SuggestionsBuilder extension versions of CommandSource.suggestX methods
// mostly because using these methods directly on the SuggestionsBuilder is cleaner than via a static method on CommandSource.

/** @sample identifiers */
fun SuggestionsBuilder.identifiers(candidates: Iterable<Identifier>, prefix: String? = null): CompletableFuture<Suggestions> {
    return CommandSource.suggestIdentifiers(candidates, this,
        prefix ?: return CommandSource.suggestIdentifiers(candidates, this)
    )
}

/** @sample identifiers */
fun SuggestionsBuilder.identifiers(candidates: Stream<Identifier>, prefix: String? = null): CompletableFuture<Suggestions> {
    return CommandSource.suggestIdentifiers(candidates, this,
        prefix ?: return CommandSource.suggestIdentifiers(candidates, this)
    )
}

/** @sample fromIdentifier */
fun<T> SuggestionsBuilder.fromIdentifier(
    candidates: Iterable<T>,
    identifier: Mapper<T, Identifier>,
    tooltip: Mapper<T, Message>
): CompletableFuture<Suggestions> =
    CommandSource.suggestFromIdentifier(candidates, this, identifier, tooltip)

/** @sample fromIdentifier */
fun<T> SuggestionsBuilder.fromIdentifier(
    candidates: Stream<T>,
    identifier: Mapper<T, Identifier>,
    tooltip: Mapper<T, Message>
): CompletableFuture<Suggestions> =
    CommandSource.suggestFromIdentifier(candidates, this, identifier, tooltip)

/** @sample positions */
fun SuggestionsBuilder.positions(
    remaining: String,
    candidates: Collection<RelativePosition>,
    predicate: Predicate<String>
): CompletableFuture<Suggestions> =
    CommandSource.suggestPositions(remaining, candidates, this, predicate)

/** @sample columnPositions */
fun SuggestionsBuilder.columnPositions(
    remaining: String,
    candidates: Collection<RelativePosition>,
    predicate: Predicate<String>
): CompletableFuture<Suggestions> =
    CommandSource.suggestColumnPositions(remaining, candidates, this, predicate)

infix fun SuggestionsBuilder.matching(candidates: Iterable<String>): CompletableFuture<Suggestions> =
    CommandSource.suggestMatching(candidates, this)

infix fun SuggestionsBuilder.matching(candidates: Stream<String>): CompletableFuture<Suggestions> =
    CommandSource.suggestMatching(candidates, this)

infix fun SuggestionsBuilder.matching(candidates: Array<String>): CompletableFuture<Suggestions> =
    CommandSource.suggestMatching(candidates, this)

fun<T> SuggestionsBuilder.matching(
    candidates: Iterable<T>,
    suggestionText: Mapper<T, String>,
    tooltip: Mapper<T, Message>
): CompletableFuture<Suggestions> =
    CommandSource.suggestMatching(candidates, this, suggestionText, tooltip)

