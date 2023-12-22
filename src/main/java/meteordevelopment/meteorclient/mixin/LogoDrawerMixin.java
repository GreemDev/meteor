/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import net.greemdev.meteor.modules.GameTweaks;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(LogoDrawer.class)
public abstract class LogoDrawerMixin {
    @ModifyArg(method = "draw(Lnet/minecraft/client/gui/DrawContext;IFI)V", index = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V", ordinal = 0))
    private Identifier onDrawLogo(Identifier original) {
        return GameTweaks.minceraft()
            ? LogoDrawer.MINCERAFT_TEXTURE
            : original;
    }
}
