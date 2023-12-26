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
import net.greemdev.meteor.commands.api.*
import net.greemdev.meteor.util.javaSubtypesOf
import net.greemdev.meteor.util.meteor.group
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.rethrowSpecial
import net.greemdev.meteor.util.text.*
import net.minecraft.client.network.ClientCommandSource
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

abstract class GModule private constructor(name: String, description: String, category: Category) : Module(category, name, description) {
    companion object : SubtypeInstances<GModule>("net.greemdev.meteor.modules", GModule::class)

    protected val sg by lazy(settings::group)

    abstract class Combat(name: String, description: String) : GModule(name, description, Categories.Combat)
    abstract class Player(name: String, description: String) : GModule(name, description, Categories.Player)
    abstract class Movement(name: String, description: String) : GModule(name, description, Categories.Movement)
    abstract class Render(name: String, description: String) : GModule(name, description, Categories.Render)
    abstract class World(name: String, description: String) : GModule(name, description, Categories.World)
    abstract class Misc(name: String, description: String) : GModule(name, description, Categories.Misc)

    fun info(textBuilder: FormattedText.() -> Unit) = info(buildText(block = textBuilder))
}

abstract class GCommand(
    name: String,
    description: String,
    private val b: (context(GCommand) CommandBuilder.() -> Unit)? = null,
    vararg aliases: String
) : Command(name, description, *aliases) {
    companion object : SubtypeInstances<GCommand>("net.greemdev.meteor.commands", GCommand::class)

    constructor(name: String, description: String, vararg aliases: String) : this(name, description, null, *aliases)

    protected open fun inject(builder: CommandBuilder) = b!!.invoke(this, builder)


    final override fun build(builder: LiteralArgumentBuilder<ClientCommandSource>) = inject(CommandBuilder(builder))


    /**
     * Show the exception to the user and log it.
     * Useful for [BrigadierBuilder.triesRunning]'s first parameter for simple command error display.
     *
     * Rethrows any passed [net.minecraft.util.crash.CrashException]s or [com.mojang.brigadier.exceptions.CommandSyntaxException]s as they have specialized handling and shouldn't be caught.
     */
    fun catching(t: Throwable) {
        t.rethrowSpecial()

        info {
            addString(t.message ?: "Uncaught exception without message. Check game logs for stacktrace information.", ChatColor.red)
            addFileHyperlink(
                "Click here to open your game logs folder.",
                minecraft.runDirectory / "logs"
            )
        }
        Greteor.logger.catching(t)
    }

    fun info(textBuilder: FormattedText.() -> Unit) = info(buildText(block = textBuilder))
}

open class SubtypeInstances<T : Any>(
    pkg: String,
    supertype: KClass<T>
) {
    val subtypeInstances by lazy {
        javaSubtypesOf(supertype.java, pkg)
            .filter { !Modifier.isAbstract(it.modifiers) }
            .toList()
            .mapNotNull { getOrNull { it.kotlin.findInstance() } }
    }
}
