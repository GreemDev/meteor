/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Modules
import net.greemdev.meteor.commands.CommandAliasesCommand
import net.greemdev.meteor.modules.AutoMessage
import net.greemdev.meteor.modules.CommandAliases
import net.greemdev.meteor.util.Meteor
import net.minecraft.item.Items

object Greteor {

    private val category = Category("Greteor", Items.SCULK_SENSOR.defaultStack)

    fun moduleCategory() = category

    @JvmStatic
    fun modules() {
        arrayOf(
            AutoMessage(),
            CommandAliases(),
        ).forEach(Meteor.modules()::add)
    }

    @JvmStatic
    fun commands() {
        arrayOf(
            CommandAliasesCommand(),
        ).forEach(Meteor.commands()::add)
    }

    @JvmStatic
    fun categories() {
        Modules.registerCategory(moduleCategory())
    }

}
