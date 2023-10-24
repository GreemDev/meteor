/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.hud.element.NotificationSource
import net.greemdev.meteor.hud.notification.Notification
import net.greemdev.meteor.hud.notification.notification
import net.greemdev.meteor.hud.notification.notifications
import net.greemdev.meteor.util.misc.neq
import net.greemdev.meteor.util.misc.network
import net.greemdev.meteor.util.misc.player
import net.minecraft.item.ItemStack
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket
import net.minecraft.screen.slot.SlotActionType

object VoidCommand : GCommand("void", "Delete the item stack in your hand.", {
    alwaysRuns {
        val slotId = 36 + mc.player().inventory.selectedSlot
        val button = 50

        val screenHandler = mc.player().currentScreenHandler
        val slots = screenHandler.slots

        val items = slots.map { it.stack.copy() }

        val stacks = Int2ObjectOpenHashMap<ItemStack>()

        for (i in 0 until slots.size) {
            val s1 = items[i]
            val s2 = slots[i].stack

            if (s1 neq s2)
                stacks.put(i, s2.copy())
        }

        mc network {
            sendPacket(
                ClickSlotC2SPacket(
                    0,
                    screenHandler.revision,
                    slotId,
                    button,
                    SlotActionType.SWAP,
                    screenHandler.cursorStack.copy(),
                    stacks
                )
            )
        }

        notification {
            title = "&zItem Stack Deleted"
            source = NotificationSource.Default
        }.sendOrFallback()
    }
})
