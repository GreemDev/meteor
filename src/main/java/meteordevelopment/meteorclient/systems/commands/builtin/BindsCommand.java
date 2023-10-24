/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.greemdev.meteor.util.text.ChatColor;
import net.greemdev.meteor.util.text.ChatEvents;
import net.greemdev.meteor.util.text.actions;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.util.List;

import static net.greemdev.meteor.util.accessors.textBuilder;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds", "List all bound modules.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            // Modules
            List<Module> modules = Modules.get().getAll().stream()
                    .filter(module -> module.keybind.isSet())
                    .toList();

            ChatUtils.info("--- Bound Modules ((highlight)%d(default)) ---", modules.size());

            for (Module module : modules) {
                HoverEvent hoverEvent = ChatEvents.hover(actions.showText, getTooltip(module));

                ChatUtils.sendMsg(msg -> {
                    msg.addString(module.title);
                    msg.colored(ChatColor.white);
                    msg.onHovered(hoverEvent);
                    msg.addString(" - ", b -> {
                        b.onHovered(hoverEvent);
                        b.colored(ChatColor.grey);
                    });
                    msg.add(module.keybind, b -> {
                        b.onHovered(hoverEvent);
                        b.colored(ChatColor.grey);
                    });
                });
            }

            return SINGLE_SUCCESS;
        });
    }

    private Text getTooltip(Module module) {
        return textBuilder()
            .addString(module.title)
            .colored(ChatColor.blue).bold()
            .newline(2)
            .addString(module.description, ChatColor.white)
            .text();
    }
}
