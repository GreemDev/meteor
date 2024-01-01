/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.arguments.ModuleArgumentType;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ClientCommandSource;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ResetCommand extends Command {

    public ResetCommand() {
        super("reset", "Resets specified settings.");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.then(
            literal("settings")
                .then(argument("module", ModuleArgumentType.create())
                    .executes(context -> {
                        Module module = context.getArgument("module", Module.class);
                        module.settings.forEach(group -> group.forEach(Setting::reset));
                        module.info("Reset all settings.");
                        return SINGLE_SUCCESS;
                    }))
                .then(literal("all")
                    .executes(context -> {
                        Modules.get().getAll().forEach(module -> module.settings.forEach(group -> group.forEach(Setting::reset)));
                        ChatUtils.infoPrefix("Modules", "Reset all module settings");
                        return SINGLE_SUCCESS;
                    }))
        ).then(
            literal("bind")
                .then(argument("module", ModuleArgumentType.create())
                    .executes(context -> {
                        Module module = context.getArgument("module", Module.class);

                        module.keybind.set(true, -1);
                        module.info("Reset bind.");

                        return SINGLE_SUCCESS;
                    }))
                .then(literal("all")
                    .executes(context -> {
                        Modules.get().getAll().forEach(module -> module.keybind.set(true, -1));
                        ChatUtils.infoPrefix("Modules", "Reset all binds.");
                        return SINGLE_SUCCESS;
                    }))
        ).then(literal("gui")
            .executes(context -> {
                GuiThemes.get().clearWindowConfigs();
                ChatUtils.info("Reset GUI positioning.");
                return SINGLE_SUCCESS;
            })
        ).then(literal("hud")
            .executes(context -> {
                Hud.get().resetToDefaultElements();
                ChatUtils.infoPrefix("HUD", "Reset all elements.");
                return SINGLE_SUCCESS;
            })
        ).then(literal("hidden-modules")
            .executes(context -> {
                Modules.get().getAllHidden()
                    .forEach(module -> module.setHidden(false));
                ChatUtils.infoPrefix("Reset", "Revealed all hidden modules.");
                return SINGLE_SUCCESS;
            })
        );
    }
}
