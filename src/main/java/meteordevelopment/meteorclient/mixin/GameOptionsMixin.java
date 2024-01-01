/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.game.ChangePerspectiveEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Freecam;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.Perspective;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Shadow @Final @Mutable public KeyBinding[] allKeys;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;allKeys:[Lnet/minecraft/client/option/KeyBinding;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void onInitAfterKeysAll(MinecraftClient client, File optionsFile, CallbackInfo info) {
        // Add category
        Map<String, Integer> categories = KeyBindingAccessor.getCategoryOrderMap();

        int highest = 0;
        for (int i : categories.values()) {
            if (i > highest) highest = i;
        }

        categories.put(MeteorClient.KEYBIND_CATEGORY, highest + 1);

        List<KeyBinding> meteorBinds = MeteorClient.getKeybinds();

        // Add key binding
        KeyBinding[] newBinds = new KeyBinding[allKeys.length + meteorBinds.size()];

        System.arraycopy(allKeys, 0, newBinds, 0, allKeys.length);

        for (int i = 0; i < meteorBinds.size(); i++)
            newBinds[allKeys.length + i] = meteorBinds.get(i);

        allKeys = newBinds;
    }

    @Inject(method = "setPerspective", at = @At("HEAD"), cancellable = true)
    private void setPerspective(Perspective perspective, CallbackInfo info) {
        if (Modules.get() == null) return; // nothing is loaded yet, shouldersurfing compat

        ChangePerspectiveEvent event = MeteorClient.EVENT_BUS.post(ChangePerspectiveEvent.get(perspective));

        if (event.isCancelled()) info.cancel();

        if (Modules.get().isActive(Freecam.class)) info.cancel();
    }
}
