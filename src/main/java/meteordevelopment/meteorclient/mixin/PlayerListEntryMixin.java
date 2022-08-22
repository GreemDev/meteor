/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.greemdev.meteor.modules.GameTweaks;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerListEntry.class)
public abstract class PlayerListEntryMixin {

    @Unique
    private static final String MIGRATOR_CAPE_URL = "https://textures.minecraft.net/texture/2340c0e03dd24a11b15a8b33c2a7e9e32abb2051b2481d0ba7defd635ca7a933";

    @Inject(method = "method_2956", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private void ignoreMigrator(MinecraftProfileTexture.Type type, Identifier id, MinecraftProfileTexture texture, CallbackInfo ci) {
        if (Modules.get().get(GameTweaks.class).noMigrators()) {
            if (type == MinecraftProfileTexture.Type.CAPE || type == MinecraftProfileTexture.Type.ELYTRA) {
                if (texture.getUrl().equals(MIGRATOR_CAPE_URL)) {
                    ci.cancel();
                }
            }
        }
    }
}
