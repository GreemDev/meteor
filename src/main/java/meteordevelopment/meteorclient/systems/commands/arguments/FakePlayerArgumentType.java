/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerEntity;
import meteordevelopment.meteorclient.utils.entity.fakeplayer.FakePlayerManager;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.command.CommandSource;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class FakePlayerArgumentType implements ArgumentType<String> {

    public static FakePlayerArgumentType create() {
        return new FakePlayerArgumentType();
    }

    public static FakePlayerEntity get(CommandContext<?> context) {
        return FakePlayerManager.get(context.getArgument("fp", String.class));
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FakePlayerManager.stream().map(FakePlayerEntity::getEntityName), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return MeteorClient.authors();
    }
}
