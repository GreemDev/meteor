/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.commands.Commands
import meteordevelopment.meteorclient.systems.hud.HudGroup
import meteordevelopment.meteorclient.systems.modules.Category
//import net.greemdev.meteor.hud.element.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.starscript.initGStarscript
import net.greemdev.meteor.util.misc.GVersioning
import net.minecraft.item.Items
import java.lang.invoke.MethodHandles

private val category = Category(Greteor::class.simpleName, Items.LIME_CONCRETE_POWDER.defaultStack)
private val hudGroup = HudGroup(Greteor::class.simpleName)

object Greteor {

    @JvmStatic
    @get:JvmName("logger")
    val logger by log4j("Greteor")

    fun category() = category
    fun hudGroup() = hudGroup

    @JvmStatic
    fun init() {
        GModule.findAll().forEach(Meteor.modules()::add)

        GCommand.findAll().forEach(Commands::add)

        GVersioning.reloadLatestRevision()
        initGStarscript()
    }

    /*@JvmStatic
    fun hudElements() = listOf(ModuleKeybindHud, NotificationHud)*/

    @JvmStatic
    fun lambdaFactoriesFor(vararg packages: String) =
        packages.forEach {
            MeteorClient.EVENT_BUS.registerLambdaFactory(it) { lookupInMethod, klass ->
                lookupInMethod(null, klass, MethodHandles.lookup())
                    as MethodHandles.Lookup
            }
        }
}
