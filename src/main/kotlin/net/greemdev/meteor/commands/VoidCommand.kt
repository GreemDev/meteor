/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.util.misc.player
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType

class VoidCommand : GCommand("void", "Delete the item stack in your hand.", {
    alwaysRuns {
        val id = 36 + mc.player().inventory.selectedSlot
        val button = 50

        val screenHandler = mc.player().currentScreenHandler
        val slots = screenHandler.slots

        val items = slots.map { it.stack.copy() }

        val stacks = Int2ObjectOpenHashMap<ItemStack>()

        for (i in 0 until slots.size) {
            val s1 = items[i]
            val s2 = slots[i].stack

            if (!ItemStack.areEqual(s1, s2))
                stacks.put(i, s2.copy())
        }

        mc.networkHandler?.sendPacket(
            ClickSlotC2SPacket(0, screenHandler.revision, id, button, SlotActionType.SWAP, screenHandler.cursorStack.copy(), stacks)
        )
    }
})
