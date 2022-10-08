/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import io.netty.buffer.Unpooled;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.CustomPayloadC2SPacketAccessor;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.s2c.play.KeepAliveS2CPacket;
import net.minecraft.network.packet.s2c.play.ResourcePackSendS2CPacket;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;

public class ServerSpoof extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPing = settings.createGroup("Ping", false);

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

    private final Setting<Boolean> spoofPing = sgPing.add(new BoolSetting.Builder()
        .name("spoof-ping")
        .description("Whether or not to spoof your ping.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> strict = sgPing.add(new BoolSetting.Builder()
        .name("strict")
        .description("Responds as fast as possible to keep alive packets send by the server.")
        .defaultValue(false)
        .visible(spoofPing::get)
        .build()
    );

    private final Setting<Integer> ping = sgPing.add(new IntSetting.Builder()
        .name("ping")
        .description("The ping to set.")
        .defaultValue(250)
        .min(0)
        .sliderMin(100)
        .sliderMax(1000)
        .noSlider()
        .visible(strict::isVisible)
        .build()
    );

    private final Setting<Integer> deviation = sgPing.add(new IntSetting.Builder()
        .name("deviation")
        .description("Randomize the ping by this amount.")
        .defaultValue(0)
        .min(0)
        .sliderMin(0)
        .sliderMax(50)
        .noSlider()
        .visible(strict::isVisible)
        .build()
    );

    private long id;
    private long timer;
    private int next;

    public ServerSpoof() {
        super(Categories.Misc, "server-spoof", "Spoof various client information sent to the server.");

        MeteorClient.EVENT_BUS.subscribe(new Listener());
    }

    @Override
    public void onActivate() {
        id = -1;
        timer = System.currentTimeMillis();

    }

    private int getPing() {
        if (deviation.get() == 0)
            return ping.get();
        return (int)(Math.random() * (deviation.get() * 2) - deviation.get() + ping.get());
    }

    public boolean noSignatures() {
        return isActive() && noSignatures.get();
    }

    private class Listener {

        @EventHandler
        private void onPreTick(TickEvent.Pre event) {
            if (spoofPing.get()) {
                if (System.currentTimeMillis() - timer >= next && id >= 0 && !strict.get()) {
                    mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(id));
                    next = getPing();
                }
            }
        }

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
            if (spoofPing.get() && event.packet instanceof KeepAliveC2SPacket packet && id != packet.getId()) {
                if (strict.get())
                    event.cancel();
                else if (ping.get() != 0) {
                    id = packet.getId();
                    timer = System.currentTimeMillis();
                    event.cancel();
                }
            }
        }

        @EventHandler
        private void onPacketReceive(PacketEvent.Receive event) {
            if (!isActive()) return;
            if (resourcePack.get() && event.packet instanceof ResourcePackSendS2CPacket packet) {
                event.cancel();
                MutableText msg = Text.literal("This server has ");
                msg.append(packet.isRequired() ? "a required " : "an optional ");
                MutableText link = Text.literal("resource pack");
                link.setStyle(link.getStyle()
                    .withColor(Formatting.BLUE)
                    .withUnderline(true)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, packet.getURL()))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to download")))
                );
                msg.append(link);
                msg.append(".");
                info(msg);
            }

            if (spoofPing.get() && strict.get() && event.packet instanceof KeepAliveS2CPacket packet) {
                id = packet.getId();
                mc.getNetworkHandler().sendPacket(new KeepAliveC2SPacket(id));
            }
        }
    }
}
