/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import kotlin.jvm.functions.Function1;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
    @Unique
    private boolean override = true;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<SplashTextRenderer> cir) {
        if (Config.get() == null || !Config.get().useCustomSplashes.get()) return;

        if (override) {
            var splashes = Config.get().customSplashes.get();
            cir.setReturnValue(new SplashTextRenderer(splashes.get(ThreadLocalRandom.current().nextInt(splashes.size())).replace('&', '§')));
        }
        override = !override;
    }

}
