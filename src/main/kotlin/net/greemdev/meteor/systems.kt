/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import meteordevelopment.meteorclient.systems.commands.Command
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.commands.api.CommandBuilder
import net.minecraft.command.CommandSource

abstract class GModule(name: String, description: String) : Module(Greteor.moduleCategory(), name, description)

abstract class GCommand(
    name: String,
    description: String,
    private val b: (CommandBuilder.() -> Unit)? = null,
    vararg val aliases: String
    ) : Command(name, description) {

    constructor(name: String, description: String, vararg aliases: String) : this(name, description, null, *aliases)

    override fun getAliases() = aliases.toMutableList()

    protected open fun CommandBuilder.build() {
        error("The base implementation of GCommand#build should never be called! " +
            "You forgot to override the method or provide the builder in the abstract class constructor.")
    }

    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        CommandBuilder(builder).apply {
            if (b != null)
                b.invoke(this)
            else
                build()
        }
    }
}
