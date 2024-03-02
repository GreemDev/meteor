/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.PlayerUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.cast
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
                info(buildText {
                    addString("Target NBT: ")
                    addText(it.asPrettyText()) {
                        clicked(actions.clipboardCopy, it.asString())
                    }
                })
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
    val hitResult = PlayerUtils.getCrosshairTarget(minecraft.player, 512.0, false) { !it.isSpectator }
    if (hitResult == null || hitResult.type == null) return null

    when (hitResult.type!!) {
        HitResult.Type.ENTITY -> return hitResult.cast<EntityHitResult>().entity.writeNbt(NbtCompound())
        HitResult.Type.BLOCK -> {
            minecraft.currentWorld().getBlockEntity(hitResult.cast<BlockHitResult>().blockPos)
                ?.also {
                    return it.createNbt()
                } ?: TargetNBTCommand.warning("The block you are looking at does not have NBT.")
        }
        HitResult.Type.MISS -> TargetNBTCommand.warning("You are not looking at an entity.")
    }

    return null
}
