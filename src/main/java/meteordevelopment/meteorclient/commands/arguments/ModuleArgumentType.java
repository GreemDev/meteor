/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.arguments;

import com.google.common.base.Predicates;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.greemdev.meteor.commands.api.Suggest;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ModuleArgumentType implements ArgumentType<@NotNull Module> {

    public ModuleArgumentType(@Nullable Predicate<Module> predicate, String message) {
        this.predicate = Objects.requireNonNullElse(predicate, Predicates.alwaysTrue());
        this.predicateFailedMessage = Objects.requireNonNullElse(message, "That module cannot be used for this argument.");
    }

    private static final DynamicCommandExceptionType NO_SUCH_MODULE = new DynamicCommandExceptionType(name -> Text.literal("Module with name " + name + " doesn't exist."));
    private static final DynamicCommandExceptionType PREDICATE_FAILED = new DynamicCommandExceptionType(message -> Text.literal("Argument predicate failed: " + message));

    public static ModuleArgumentType create() {
        return create(null);
    }
    public static ModuleArgumentType create(Predicate<Module> predicate) {
        return create(predicate, null);
    }

    public static ModuleArgumentType create(Predicate<Module> predicate, String message) {
        return new ModuleArgumentType(predicate, message);
    }

    @NotNull
    public static Module get(CommandContext<?> context) {
        return context.getArgument("module", Module.class);
    }

    public final Predicate<Module> predicate;
    public final String predicateFailedMessage;

    @NotNull
    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String argument = reader.readString();
        Module module = Modules.get().get(argument);
        if (module == null) throw NO_SUCH_MODULE.create(argument);
        if (!predicate.test(module)) throw PREDICATE_FAILED.create(predicateFailedMessage);

        return module;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return Suggest.matching(builder, Modules.get().getAll().stream().filter(predicate).map(module -> module.name));
    }

    @Override
    public Collection<String> getExamples() {
        return Modules.get()
            .stream()
            .filter(predicate)
            .limit(5)
            .map(module -> module.name)
            .toList();
    }
}
