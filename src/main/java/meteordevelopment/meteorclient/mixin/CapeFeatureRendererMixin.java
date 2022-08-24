/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import net.greemdev.meteor.modules.GameTweaks;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(CapeFeatureRenderer.class)
public abstract class CapeFeatureRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At("HEAD"), cancellable = true)
    private void ignoreMigratorCape(MatrixStack stack, VertexConsumerProvider vertex, int i, AbstractClientPlayerEntity playerEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        var gameTweaks = Modules.get().get(GameTweaks.class);
        if (gameTweaks.noMigrators() && mc.getNetworkHandler() != null) {
            var playerListEntry = mc.getNetworkHandler().getPlayerListEntry(playerEntity.getUuid());
            if (playerListEntry != null &&
                (gameTweaks.getTextureBlacklist().contains(playerListEntry.getCapeTexture()) || gameTweaks.getTextureBlacklist().contains(playerListEntry.getElytraTexture()))
            ) {
                ci.cancel();
            }
        }
    }

}
