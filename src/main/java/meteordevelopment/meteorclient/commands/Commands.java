/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PostInit;
import net.greemdev.meteor.util.Reflection;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.BuiltinRegistries;
import net.minecraft.server.command.CommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Commands {
    @NotNull
    public static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    @NotNull
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    @NotNull
    public static final CommandSource COMMAND_SOURCE = new ClientCommandSource(null, MeteorClient.mc);
    @NotNull
    public static final Map<Class<? extends Command>, Command> COMMANDS = new Reference2ReferenceArrayMap<>();

    @PostInit
    public static void init() {
        Reflection.streamSubtypes(Command.class, Command.class.getPackageName())
            .map(Reflection::callNoArgsConstructor)
            .filter(Objects::nonNull)
            .forEach(Commands::add);
    }

    public static void add(Command command) {
        COMMANDS.remove(command.getClass());
        command.registerTo(DISPATCHER);
        COMMANDS.put(command.getClass(), command);
    }

    public static void addAll(Collection<Command> commands) {
        commands.forEach(Commands::add);
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        DISPATCHER.execute(message, COMMAND_SOURCE);
    }

    @Nullable
    public static Command get(String name) {
        return COMMANDS.entrySet()
            .stream()
            .filter(entry ->
                entry.getKey().getSimpleName().equalsIgnoreCase(name) ||
                    entry.getValue().getName().equalsIgnoreCase(name)
            )
            .findFirst()
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    @NotNull
    public static Command get(Class<? extends Command> commandClass) {
        return COMMANDS.get(commandClass);
    }

    @NotNull
    public static String prefix() {
        return Config.get().prefix.get();
    }
}
