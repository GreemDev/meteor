/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.greemdev.meteor.util.text.FormattedText;
import net.minecraft.command.CommandSource;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindsCommand extends Command {
    public BindsCommand() {
        super("binds", "List of all bound modules.");
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
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, getTooltip(module));

                ChatUtils.sendMsg(msg -> {
                    msg.append(module.title);
                    msg.formatted(Formatting.WHITE);
                    msg.onHovered(hoverEvent);
                    msg.append(" - ", b -> {
                        b.onHovered(hoverEvent);
                        b.formatted(Formatting.GRAY);
                    });
                    msg.append(module.keybind.toString(), b -> {
                        b.onHovered(hoverEvent);
                        b.formatted(Formatting.GRAY);
                    });
                });
            }

            return SINGLE_SUCCESS;
        });
    }

    private MutableText getTooltip(Module module) {
        return FormattedText.builder()
            .append(module.title)
            .formatted(Formatting.BLUE).bold()
            .append("\n\n")
            .append(module.description, Formatting.WHITE)
            .mutableText();
    }
}
