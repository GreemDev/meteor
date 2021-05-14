/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package minegame159.meteorclient.systems.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import joptsimple.internal.Strings;
import meteordevelopment.orbit.EventHandler;
import minegame159.meteorclient.MeteorClient;
import minegame159.meteorclient.events.packets.PacketEvent;
import minegame159.meteorclient.events.world.TickEvent;
import minegame159.meteorclient.systems.commands.Command;
import minegame159.meteorclient.utils.player.ChatUtils;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ServerAddress;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Formatting;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ServerCommand extends Command {
    private static final List<String> ANTICHEAT_LIST = Arrays.asList("nocheatplus", "negativity", "warden", "horizon","illegalstack","coreprotect","exploitsx");
    private Integer ticks = 0;

    public ServerCommand() {
        super("server", "Prints server information");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            basicInfo();
            return SINGLE_SUCCESS;
        });

        builder.then(literal("info").executes(ctx -> {
            basicInfo();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("gamerules").executes(ctx -> {
            CompoundTag tag = mc.world.getGameRules().toNbt();
            tag.getKeys().forEach((key) -> ChatUtils.prefixInfo("Server", "%s: %s", key, tag.getString(key)));
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("plugins").executes(ctx -> {
            ticks = 0;
            MeteorClient.EVENT_BUS.subscribe(this);
            mc.player.networkHandler.sendPacket(new RequestCommandCompletionsC2SPacket(0, "/"));
            return SINGLE_SUCCESS;
        }));
    }

    private void basicInfo() {
        if (mc.isIntegratedServerRunning()) {
            IntegratedServer server = mc.getServer();

            ChatUtils.prefixInfo("Server","Singleplayer");
            if (server != null) ChatUtils.prefixInfo("Server", "Version: %s", server.getVersion());

            return;
        }

        ServerInfo server = mc.getCurrentServerEntry();

        if (server == null) {
            ChatUtils.prefixError("Server","Couldn't obtain any server information.");
            return;
        }

        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server.address).getHostAddress();
        } catch (UnknownHostException ignored) {}

        if (ipv4.isEmpty()) {
            ChatUtils.prefixInfo("Server", "IP: %s", server.address);
        }
        else {
            ChatUtils.prefixInfo("Server", "IP: %s (%s)", server.address, ipv4);
        }

        ChatUtils.prefixInfo("Server", "Port: %d", ServerAddress.parse(server.address).getPort());

        ChatUtils.prefixInfo("Server","Type: %s", mc.player.getServerBrand() != null ? mc.player.getServerBrand() : "unknown");

        ChatUtils.prefixInfo("Server", "Motd: %s", server.label != null ? server.label.getString() : "unknown");

        ChatUtils.prefixInfo("Server", "Version: %s", server.version.getString());

        ChatUtils.prefixInfo("Server","Protocol version: %d", server.protocolVersion);

        ChatUtils.prefixInfo("Server", "Difficulty: %s", mc.world.getDifficulty().getTranslatableName().getString());
    }
    
    @EventHandler
    public void onTick(TickEvent.Post event) {
        ticks++;

        if (ticks >= 5000) {
            ChatUtils.prefixError("Server", "Plugins check timed out");
            MeteorClient.EVENT_BUS.unsubscribe(this);
            ticks = 0;
        }
    }

    @EventHandler
    public void onReadPacket(PacketEvent.Receive event) {
        try {
            if (event.packet instanceof CommandSuggestionsS2CPacket) {
                CommandSuggestionsS2CPacket packet = (CommandSuggestionsS2CPacket) event.packet;
                List<String> plugins = new ArrayList<>();
                Suggestions matches = packet.getSuggestions();

                if (matches == null) {
                    ChatUtils.prefixError("Server", "Invalid Packet.");
                    return;
                }

                for (Suggestion yes : matches.getList()) {
                    String[] command = yes.getText().split(":");
                    if (command.length > 1) {
                        String pluginName = command[0].replace("/", "");

                        if (!plugins.contains(pluginName)) {
                            plugins.add(pluginName);
                        }
                    }
                }

                Collections.sort(plugins);
                for (int i = 0; i < plugins.size(); i++) {
                    plugins.set(i, formatName(plugins.get(i)));
                }

                if (!plugins.isEmpty()) {
                    ChatUtils.prefixInfo("Server", "Plugins (%d): %s ", plugins.size(), Strings.join(plugins.toArray(new String[0]), ", "));
                } else {
                    ChatUtils.prefixError("Server", "No plugins found.");
                }

                ticks = 0;
                MeteorClient.EVENT_BUS.unsubscribe(this);
            }

        } catch (Exception e) {
            ChatUtils.prefixError("Server", "An error occurred while trying to find plugins");
            ticks = 0;
            MeteorClient.EVENT_BUS.unsubscribe(this);
        }
    }

    private String formatName(String name) {
        if (ANTICHEAT_LIST.contains(name)) {
            return String.format("%s%s(default)", Formatting.RED, name);
        }
        else if (name.contains("exploit") || name.contains("cheat") || name.contains("illegal")) {
            return String.format("%s%s(default)", Formatting.RED, name);
        }

        return String.format("(highlight)%s(default)", name);
    }
}
