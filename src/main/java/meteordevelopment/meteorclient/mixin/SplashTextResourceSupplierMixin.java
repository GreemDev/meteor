/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;

@Mixin(SplashTextResourceSupplier.class)
public class SplashTextResourceSupplierMixin {
    private boolean override = true;

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void onApply(CallbackInfoReturnable<String> cir) {
        if (Config.get() == null || !Config.get().useCustomSplashes.get()) return;

        if (override) {
            var splashes = Config.get().customSplashes.get();
            cir.setReturnValue(splashes.get(ThreadLocalRandom.current().nextInt(splashes.size())).replace('&', '§'));
        }
        override = !override;
    }
}
