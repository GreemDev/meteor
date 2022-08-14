/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.world;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixin.SignEditScreenAccessor;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class AutoSign extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private String[] text;

    public AutoSign() {
        super(Categories.World, "auto-sign", "Automatically writes signs.");
    }

    private Setting<Keybind> copyThis = sgGeneral.add(new KeybindSetting.Builder()
        .name("copy-target-sign")
        .description("Use this keybind to copy the currently looked at sign's content.")
        .defaultValue(Keybind.none())
        .build()
    );

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && copyThis.get().matches(true, event.key))
            copyTarget();
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && copyThis.get().matches(false, event.button))
            copyTarget();
    }

    private void copyTarget() {
        if (Utils.canUpdate()) {
            if (mc.crosshairTarget == null) {
                error("You're not looking at anything.");
                return;
            }
            var entity = mc.world.getBlockEntity(new BlockPos(mc.crosshairTarget.getPos()));
            if (entity == null) {
                error("You're not looking at anything.");
                return;
            }
            if (entity instanceof SignBlockEntity signBlock) {
                info("Copied the following sign: ");
                for (var i = 0; i < 4; i++) {
                    var row = text[i] = signBlock.getTextOnRow(i, false).copyContentOnly().getString();
                    info("Line %s -> %s", i + 1, row);
                }
            }
        }
    }

    @Override
    public void onActivate() {
        text = new String[4];
    }

    @Override
    public void onDeactivate() {
        text = null;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (!(event.screen instanceof SignEditScreen) || text == null) return;

        SignBlockEntity sign = ((SignEditScreenAccessor) event.screen).getSign();

        mc.player.networkHandler.sendPacket(new UpdateSignC2SPacket(sign.getPos(), text[0], text[1], text[2], text[3]));

        event.cancel();
    }
}
