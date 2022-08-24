/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.argument
import net.greemdev.meteor.commands.api.simpleCommandException
import net.greemdev.meteor.commands.api.throwNew
import net.greemdev.meteor.util.*
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket

class RenameCommand : GCommand("rename", "Renames the item in your hand.", {
    then("name", arg.greedyString()) {
        alwaysRuns {
            val stack = mc.player().usableItemStack() ?: noItem.throwNew()
            val newName = it.argument<String>("name").replace('&', '§')

            stack.setCustomName(textOf(newName))
            ChatUtils.info("Changed the item's name.")
            mc.networkHandler?.sendPacket(RenameItemC2SPacket(newName))
        }
    }
}) {
    companion object {
        val noItem by simpleCommandException(textOf("You aren't holding anything."))
    }
}
