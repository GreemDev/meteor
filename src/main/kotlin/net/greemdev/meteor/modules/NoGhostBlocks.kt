/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.entity.player.BreakBlockEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.misc.currentWorld

object NoGhostBlocks : GModule.World("no-ghost-blocks", "Attempts to prevent ghost blocks arising from breaking blocks quickly. Especially useful with multiconnect.") {
    @EventHandler
    fun breakBlock(event: BreakBlockEvent) {
        if (mc.isInSingleplayer) return

        event.cancel()

        // play the related sounds and particles for the user.
        val blockState = mc.currentWorld().getBlockState(event.blockPos)
        blockState.block.onBreak(mc.world, event.blockPos, blockState, mc.player) // this doesn't alter the state of the block in the world
    }
}
