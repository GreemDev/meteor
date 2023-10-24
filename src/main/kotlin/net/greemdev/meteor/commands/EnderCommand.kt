/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.player.EChestMemory
import net.greemdev.meteor.GCommand
import net.minecraft.item.Items

object EnderCommand : GCommand("ender", "Open a preview of your ender chest.", {
    alwaysRuns {
        if (EChestMemory.ITEMS.isEmpty())
            error("Can't open ender chest; no known items. Open an ender chest in the world first.")
        else {
            Utils.openContainer(
                Items.ENDER_CHEST.defaultStack,
                EChestMemory.ITEMS.toArray(emptyArray()),
                true
            )
        }
    }
})
