/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.util.text.*
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket

object RenameCommand : GCommand("rename", "Renames the item in your hand.", {
    then("name", arg.greedyString()) {
        alwaysRuns {
            val stack = mc.player().usableItemStack() ?: noItem.throwNew()
            val name by it.argument(arg.greedyString(), "name")
            val newName = name.replace('&', '§')

            stack.setCustomName(textOf(newName))
            info("Changed the item's name.")
            mc network { sendPacket(RenameItemC2SPacket(newName)) }
        }
    }
})

private val noItem by CommandExceptions simple textOf("You aren't holding anything.")
