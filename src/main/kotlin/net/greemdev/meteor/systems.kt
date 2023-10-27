/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import meteordevelopment.meteorclient.commands.Command
import meteordevelopment.meteorclient.systems.modules.Categories
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.findInstancesOfSubtypesOf
import net.greemdev.meteor.util.meteor.group
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.text.*
import net.minecraft.command.CommandSource

sealed class GModule(name: String, description: String, category: Category = Greteor.category()) : Module(category, name, description) {
    protected val sg by lazy(settings::group)

    sealed class Combat(name: String, description: String) : GModule(name, description, Categories.Combat)
    sealed class Player(name: String, description: String) : GModule(name, description, Categories.Player)
    sealed class Movement(name: String, description: String) : GModule(name, description, Categories.Movement)
    sealed class Render(name: String, description: String) : GModule(name, description, Categories.Render)
    sealed class World(name: String, description: String) : GModule(name, description, Categories.World)
    sealed class Misc(name: String, description: String) : GModule(name, description, Categories.Misc)

    companion object {
        fun findAll() = findInstancesOfSubtypesOf<GModule>("net.greemdev.meteor.modules")
    }
}

abstract class GCommand(
    name: String,
    description: String,
    private val b: (context(GCommand) CommandBuilder.() -> Unit)? = null,
    private vararg val aliases: String
) : Command(name, description) {

    constructor(name: String, description: String, vararg aliases: String) : this(name, description, null, *aliases)

    override fun getAliases() = aliases.toList()

    protected open fun CommandBuilder.inject() {
        kotlin.error("The base implementation of GCommand#inject should never be called! " +
            "You forgot to override the method or provide the builder in the abstract class constructor for command $name.")
    }

    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        with(CommandBuilder(builder)) {
            b?.invoke(this@GCommand, this) ?: inject()
        }
    }

    /**
     * Show the exception to the user and log it.
     * Useful for [BrigadierBuilder]'s `triesRunning` for simple command error display.
     */
    fun catching(t: Throwable) {
        ChatUtils.sendMsg(title, buildText {
            addString(t.message ?: "Uncaught exception without message. Check game logs for stacktrace information.") {
                colored(red)
            }
            addString("Click here to open your game logs folder.") {
                val path = (minecraft.runDirectory / "logs").path
                clicked(actions.openFile, path)
                hovered(actions.showText, textOf(path))
                colored(MeteorColor.HYPERLINK_BLUE).underlined()
            }
        })
        Greteor.logger.catching(t)
    }

    companion object {
        fun findAll() = findInstancesOfSubtypesOf<GCommand>("net.greemdev.meteor.commands")
    }
}
