/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
    public static final List<Command> COMMANDS = new ArrayList<>();

    @PostInit
    public static void init() {
        Reflection.streamSubtypes(Command.class, Command.class.getPackageName())
            .map(Reflection::callNoArgsConstructor)
            .filter(Objects::nonNull)
            .forEach(Commands::add);

        /*add(new VClipCommand());
        add(new HClipCommand());
        add(new DismountCommand());
        add(new DamageCommand());
        add(new DropCommand());
        add(new EnchantCommand());
        add(new FakePlayerCommand());
        add(new FriendsCommand());
        add(new CommandsCommand());
        add(new InventoryCommand());
        add(new LocateCommand());
        add(new NbtCommand());
        add(new NotebotCommand());
        add(new PeekCommand());
        add(new EnderChestCommand());
        add(new ProfilesCommand());
        add(new ReloadCommand());
        add(new ResetCommand());
        add(new SayCommand());
        add(new ServerCommand());
        add(new SwarmCommand());
        add(new ToggleCommand());
        add(new SettingCommand());
        add(new SpectateCommand());
        add(new GamemodeCommand());
        add(new SaveMapCommand());
        add(new MacroCommand());
        add(new ModulesCommand());
        add(new BindsCommand());
        add(new GiveCommand());
        add(new BindCommand());
        add(new FovCommand());
        add(new RotationCommand());
        add(new WaypointCommand());
        add(new InputCommand());*/

        COMMANDS.sort(Comparator.comparing(Command::getName));
    }

    public static void add(Command command) {
        COMMANDS.removeIf(existing -> existing.getName().equals(command.getName()));
        command.registerTo(DISPATCHER);
        COMMANDS.add(command);
    }

    public static void addAll(Collection<Command> commands) {
        commands.forEach(Commands::add);
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
