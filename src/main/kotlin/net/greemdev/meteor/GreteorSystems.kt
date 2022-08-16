/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.commands.api.CommandBuilder
import net.minecraft.command.CommandSource

abstract class GModule(name: String, description: String) : Module(Greteor.moduleCategory(), name, description)

abstract class GCommand(name: String, description: String, vararg val aliases: String)
    : meteordevelopment.meteorclient.systems.commands.Command(name, description) {

    protected fun modify(builder: CommandBuilder, func: CommandBuilder.() -> Unit) = builder.func()

    override fun getAliases() = aliases.toMutableList()

    abstract fun CommandBuilder.build()

    override fun build(builder: LiteralArgumentBuilder<CommandSource>) {
        CommandBuilder(builder).build()
    }
}
