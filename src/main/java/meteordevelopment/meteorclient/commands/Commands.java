/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Commands {
    public static final CommandRegistryAccess REGISTRY_ACCESS = CommandManager.createRegistryAccess(BuiltinRegistries.createWrapperLookup());
    public static final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
    public static final CommandSource COMMAND_SOURCE = new ClientCommandSource(null, mc);
    public static final List<Command> COMMANDS = new ArrayList<>();

    @PostInit
    public static void init() {
        Reflection.streamSubtypes(Command.class, Command.class.getPackageName())
            .map(Reflection::callNoArgsConstructor)
            .filter(Objects::nonNull)
            .forEach(Commands::add);

        COMMANDS.sort(Comparator.comparing(Command::getName));
    }

    public static void add(Command command) {
        COMMANDS.removeIf(existing -> existing.getName().equals(command.getName()));
        command.registerTo(DISPATCHER);
        COMMANDS.add(command);
    }

    public static void dispatch(String message) throws CommandSyntaxException {
        DISPATCHER.execute(message, COMMAND_SOURCE);
    }

    @Nullable
    public static Command get(String name) {
        return COMMANDS.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(null);
    }

    @NotNull
    public static Command get(Class<? extends Command> commandClass) {
        return COMMANDS.stream().filter(c -> c.getClass().equals(commandClass)).findFirst().orElseThrow();
    }

    @NotNull
    public static String prefix() {
        return Config.get().prefix.get();
    }
}
