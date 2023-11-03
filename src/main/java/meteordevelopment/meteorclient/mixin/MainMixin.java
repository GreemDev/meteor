/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import net.greemdev.meteor.modules.MinecraftPresenceKt;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

    @SuppressWarnings("SpellCheckingInspection") //clinit is literally the fucking name of static initializers intellij shut up
    @Inject(method = "<clinit>", at = @At("HEAD"), remap = false, cancellable = true)
    private static void voidStaticInitializer(CallbackInfo info) {
        info.cancel();
    }

    @Inject(method = "main", at = @At("HEAD"), remap = false)
    private static void beforeStart(CallbackInfo ci) {
        MinecraftPresenceKt.setGameStart(System.currentTimeMillis());
    }
}
