/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.DropItemsEvent;
import meteordevelopment.meteorclient.events.entity.player.ClipAtLedgeEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.systems.modules.movement.NoSlow;
import meteordevelopment.meteorclient.systems.modules.player.SpeedMine;
import net.greemdev.meteor.util.meteor.Meteor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow
    public abstract PlayerAbilities getAbilities();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void clipAtLedge(CallbackInfoReturnable<Boolean> info) {
        ClipAtLedgeEvent event = MeteorClient.EVENT_BUS.post(ClipAtLedgeEvent.get());

        if (event.isSet()) info.setReturnValue(event.isClip());
    }

    @Inject(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At("HEAD"), cancellable = true)
    private void onDropItem(ItemStack stack, boolean bl, boolean bl2, CallbackInfoReturnable<ItemEntity> info) {
        if (world.isClient && !stack.isEmpty()) {
            if (MeteorClient.EVENT_BUS.post(DropItemsEvent.get(stack)).isCancelled())
                info.cancel();
        }
    }

    @Inject(method = "getBlockBreakingSpeed", at = @At(value = "RETURN"), cancellable = true)
    public void onGetBlockBreakingSpeed(BlockState block, CallbackInfoReturnable<Float> cir) {
        SpeedMine module = Modules.get().get(SpeedMine.class);
        if (!module.isActive() || module.mode.get() != SpeedMine.Mode.Normal) return;

        cir.setReturnValue((float) (cir.getReturnValue() * module.modifier.get()));
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void dontJump(CallbackInfo info) {
        Anchor module = Modules.get().get(Anchor.class);
        if (module.isActive() && module.cancelJump) info.cancel();
    }

    @Inject(method = "getMovementSpeed", at = @At("RETURN"), cancellable = true)
    private void onGetMovementSpeed(CallbackInfoReturnable<Float> info) {
        if (!Meteor.module(NoSlow.class).slowness()) return;
        float walkSpeed = getAbilities().getWalkSpeed();
        if (info.getReturnValueF() < walkSpeed) {
            info.setReturnValue(
                isSprinting()
                    ? (float) (walkSpeed * 1.30000001192092896)
                    : walkSpeed
            );
        }
    }
}
