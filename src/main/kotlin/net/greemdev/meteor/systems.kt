/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import meteordevelopment.meteorclient.events.meteor.KeyEvent
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent
import meteordevelopment.meteorclient.systems.commands.Command
import meteordevelopment.meteorclient.systems.modules.Categories
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.commands.api.CommandBuilder
import net.greemdev.meteor.event.GameInputEvent
import net.greemdev.meteor.util.meteor.group
import net.minecraft.command.CommandSource

abstract class GModule(name: String, description: String, category: Category = Greteor.category()) : Module(category, name, description) {
    protected val sg by lazy { settings.group() }
    open fun onGameInput(event: GameInputEvent) {}

    @EventHandler
    private fun onKey(e: KeyEvent) {
        onGameInput(GameInputEvent(e))
    }

    @EventHandler
    private fun onMouse(e: MouseButtonEvent) {
        onGameInput(GameInputEvent(e))
    }

    abstract class Combat(name: String, description: String) : GModule(name, description, Categories.Combat)
    abstract class Player(name: String, description: String) : GModule(name, description, Categories.Player)
    abstract class Movement(name: String, description: String) : GModule(name, description, Categories.Movement)
    abstract class Render(name: String, description: String) : GModule(name, description, Categories.Render)
    abstract class World(name: String, description: String) : GModule(name, description, Categories.World)
    abstract class Misc(name: String, description: String) : GModule(name, description, Categories.Misc)
}

abstract class GCommand(
    name: String,
    description: String,
    private val b: (CommandBuilder.() -> Unit)? = null,
    vararg val aliases: String
    ) : Command(name, description) {

    constructor(name: String, description: String, vararg aliases: String) : this(name, description, null, *aliases)

    override fun getAliases() = aliases.toMutableList()

    protected open fun CommandBuilder.inject() {
        error("The base implementation of GCommand#injectBrigadier should never be called! " +
            "You forgot to override the method or provide the builder in the abstract class constructor.")
    }

    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        CommandBuilder(builder).apply {
            b?.also { it() } ?: inject()
        }
    }
}
