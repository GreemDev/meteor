/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.commands.builtin;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.greemdev.meteor.util.text.FormattedText;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Predicate;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class CommandsCommand extends Command {
    public CommandsCommand() {
        super("commands", "List of all commands.", "help");
    }

    @Override
    public void build(LiteralArgumentBuilder<ClientCommandSource> builder) {
        builder.executes(context -> {
            List<Command> commands = Commands.COMMANDS.values().stream().toList();
            ChatUtils.info("--- Commands ((highlight)%d(default)) ---", commands.size());

            ChatUtils.sendMsg(new FormattedText() {{
                commands.forEach(command ->
                    addText(getCommandText(commands, commands.size() - 1, command))
                );
            }});

            return SINGLE_SUCCESS;
        });
    }

    private MutableText getCommandText(List<? extends Command> commandList, int lastIndex, Command command) {
        // Hover tooltip
        MutableText tooltip = Text.empty();

        tooltip.append(Text.literal(Utils.nameToTitle(command.getName())).formatted(Formatting.BLUE, Formatting.BOLD)).append("\n");

        MutableText aliases = Text.literal(Commands.prefix() + command.getName());
        if (!command.getAliases().isEmpty()) {
            aliases.append(", ");
            for (String alias : command.getAliases().stream().filter(Predicate.not(String::isEmpty)).toList()) {
                aliases.append(Commands.prefix() + alias);
                if (!alias.equals(command.getAliases().get(command.getAliases().size() - 1))) aliases.append(", ");
            }
        }
        tooltip.append(aliases.formatted(Formatting.GRAY)).append("\n\n");

        tooltip.append(Text.literal(command.getDescription()).formatted(Formatting.WHITE));

        // Text
        MutableText text = Text.literal(Utils.nameToTitle(command.getName()));
        if (command != commandList.get(lastIndex))
            text.append(Text.literal(", ").formatted(Formatting.GRAY));

        text.setStyle(text.getStyle()
            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip))
            .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command.toString()))
        );

        return text;
    }

}
