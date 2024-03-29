/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public abstract class Command {
    protected static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());

    private final String name;
    protected final String title;
    private final String description;
    private final List<String> aliases = new ArrayList<>();

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        Collections.addAll(this.aliases, aliases);
    }

    // Helper methods to painlessly infer the CommandSource generic type argument
    protected static <T> RequiredArgumentBuilder<CommandSource, T> argument(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    protected static LiteralArgumentBuilder<CommandSource> literal(final String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public final void registerTo(CommandDispatcher<CommandSource> dispatcher) {
        register(dispatcher, name);
        for (String alias : aliases) register(dispatcher, alias);
    }

    public void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder.literal(name);
        build(builder);
        dispatcher.register(builder);
    }

    public abstract void build(LiteralArgumentBuilder<CommandSource> builder);

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final List<String> getAliases() {
        return aliases;
    }

    public String toString() {
        return Commands.prefix() + name;
    }

    public final String toString(String... args) {
        StringBuilder base = new StringBuilder(toString());
        for (String arg : args) base.append(' ').append(arg);
        return base.toString();
    }

    @NotNull
    public String subcommand(String... subcommand) {
        return toString(subcommand);
    }

    @NotNull
    public final <T> T require(@Nullable T value, @NotNull SimpleCommandExceptionType scet) throws CommandSyntaxException {
        if (value == null)
            throw scet.create();

        return value;
    }

    @NotNull
    public final <T> T require(@Nullable T value, @NotNull CommandSyntaxException cse) throws CommandSyntaxException {
        if (value == null)
            throw cse;

        return value;
    }

    @NotNull
    public final <T> T require(@Nullable T value, @NotNull Supplier<@NotNull CommandSyntaxException> cse) throws CommandSyntaxException {
        if (value == null)
            throw cse.get();

        return value;
    }

    public final void require(boolean condition, @NotNull SimpleCommandExceptionType scet) throws CommandSyntaxException {
        if (!condition)
            throw scet.create();
    }

    public final void require(boolean condition, @NotNull CommandSyntaxException cse) throws CommandSyntaxException {
        if (!condition)
            throw cse;
    }

    public final void require(boolean condition, @NotNull Supplier<@NotNull CommandSyntaxException> cse) throws CommandSyntaxException {
        if (!condition)
            throw cse.get();
    }

    public void info(Text message) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(title, message);
    }

    public void info(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.infoPrefix(title, message, args);
    }

    public void warning(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(title, message, args);
    }

    public void error(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(title, message, args);
    }
}
