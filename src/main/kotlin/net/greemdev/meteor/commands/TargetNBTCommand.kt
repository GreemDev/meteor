/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.PlayerUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.asPrettyText
import net.greemdev.meteor.util.misc.currentWorld
import net.greemdev.meteor.util.text.actions
import net.greemdev.meteor.util.text.buildText
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult

object TargetNBTCommand : GCommand("target-nbt", "Gets NBT data of the entity you are looking at.", {
    then("show") {
        runs {
            getTargetNbt()?.also {
                info {
                    addString("Target NBT: ")
                    addText(it.asPrettyText()) {
                        clicked(actions.clipboardCopy, it.asString())
                    }
                }
            }
        }
    }
    then("copy") {
        runs {
            getTargetNbt()?.also {
                minecraft.keyboard.clipboard = it.asString()
                info("NBT successfully copied to clipboard.")
            }
        }
    }
})

private fun getTargetNbt(): NbtCompound? {
    val hitResult = PlayerUtils.getCrosshairTarget(minecraft.player, 512.0, false)
    if (hitResult?.type == null) return null

    when (hitResult) {
        is EntityHitResult -> return hitResult.entity.writeNbt(NbtCompound())
        is BlockHitResult ->
            minecraft.currentWorld().getBlockEntity(
                hitResult.takeUnless {
                    it.type == HitResult.Type.MISS
                }?.blockPos ?: run {
                    TargetNBTCommand.warning("You are not looking at anything.")
                    return null
                }
            )?.also {
                return it.createNbt()
            } ?: TargetNBTCommand.warning("The block you are looking at does not have NBT.")
    }

    return null
}
