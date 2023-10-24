/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.CustomPayloadC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.greemdev.meteor.util.text.ChatColor;
import net.greemdev.meteor.util.text.actions;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.text.*;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

import static net.greemdev.meteor.util.accessors.text;

public class ServerSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> spoofBrand = sgGeneral.add(new BoolSetting.Builder()
        .name("brand")
        .description("Spoof your client's brand name when logging in.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> brand = sgGeneral.add(new StringSetting.Builder()
        .name("client-brand")
        .description("Specify the brand that will be sent to the server.")
        .defaultValue("vanilla")
        .visible(spoofBrand::get)
        .build()
    );

    private final Setting<Boolean> resourcePack = sgGeneral.add(new BoolSetting.Builder()
        .name("resource-pack")
        .description("Spoof accepting the server resource pack.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> noSignatures = sgGeneral.add(new BoolSetting.Builder()
        .name("no-signatures")
        .description("Prevents the client from sending chat signatures.")
        .defaultValue(false)
        .build()
    );

    public ServerSpoof() {
        super(Categories.Misc, "server-spoof", "Spoof various client information sent to the server.");

        MeteorClient.EVENT_BUS.subscribe(new Listener());
    }

    public boolean noSignatures() {
        return isActive() && noSignatures.get();
    }

    private class Listener {
        @EventHandler
        private void onPacketSend(PacketEvent.Send event) {
            if (!isActive()) return;
            if (spoofBrand.get() && event.packet instanceof CustomPayloadC2SPacket) {
                CustomPayloadC2SPacketAccessor packet = (CustomPayloadC2SPacketAccessor) event.packet;

                if (packet.getChannel().equals(CustomPayloadC2SPacket.BRAND)) {
                    packet.setData(new PacketByteBuf(Unpooled.buffer()).writeString(brand.get()));
                }
                else if (StringUtils.containsIgnoreCase(packet.getData().toString(StandardCharsets.UTF_8), "fabric") && brand.get().equalsIgnoreCase("fabric")) {
                    event.cancel();
                }
            }
        }

        @EventHandler
        private void onPacketReceive(PacketEvent.Receive event) {
            if (!isActive()) return;
            if (resourcePack.get() && event.packet instanceof ResourcePackSendS2CPacket packet) {
                event.cancel();
                info(text(msg -> {
                    msg.addString("This server has ");
                    msg.addString(packet.isRequired() ? "a required" : "an optional");
                    msg.add(" resource pack", link -> {
                        link.colored(ChatColor.blue).underlined();
                        link.clicked(actions.openURL, packet.getURL());
                        link.hoveredText(Text.literal("Click to download"));
                    });
                    msg.addString(".");
                }));
            }
        }
    }
}
