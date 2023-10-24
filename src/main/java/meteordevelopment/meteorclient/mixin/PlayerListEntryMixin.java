/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.NameProtect;
import net.greemdev.meteor.modules.greteor.GameTweaks;
import net.greemdev.meteor.util.meteor.Meteor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Shadow
    public abstract GameProfile getProfile();

    @Unique
    private static final String MIGRATOR_CAPE_URL = "http://textures.minecraft.net/texture/2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933";

    @Inject(method = "method_2956", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private void ignoreMigrator(MinecraftProfileTexture.Type type, Identifier id, MinecraftProfileTexture texture, CallbackInfo ci) {
        Meteor.module(GameTweaks.class, gt -> {
            if (gt.noMigrators()) {
                if (type == MinecraftProfileTexture.Type.CAPE || type == MinecraftProfileTexture.Type.ELYTRA) {
                    if (texture.getUrl().equals(MIGRATOR_CAPE_URL)) {
                        ci.cancel();
                    }
                }
            }
        });
    }

    @Inject(method = "getSkinTexture", at = @At("HEAD"), cancellable = true)
    private void protectSkinTexture(CallbackInfoReturnable<Identifier> info) {
        if (!getProfile().getName().equals(MeteorClient.mc.getSession().getUsername())) return;

        Meteor.module(NameProtect.class, np -> {
            if (np.protectSkins()) info.setReturnValue(DefaultSkinHelper.getTexture());
        });
    }
}
