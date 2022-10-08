/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.systems.hud.HudGroup
import meteordevelopment.meteorclient.systems.modules.Category
import net.greemdev.meteor.hud.HudElementMetadata
import net.greemdev.meteor.hud.element.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.starscript.initGStarscript
import net.greemdev.meteor.util.misc.TitleScreenInfo
import net.minecraft.item.Items
import java.lang.invoke.MethodHandles

private val category = Category("Greteor", Items.LIME_CONCRETE_POWDER.defaultStack)
private val hudGroup = HudGroup("Greteor")

object Greteor {

    @JvmStatic
    fun logger() = logger

    internal val logger by log4j("Greteor")

    fun category() = category
    fun hudGroup() = hudGroup

    @JvmStatic
    fun init() {
        findInstancesOfSubtypesOf<GModule>("net.greemdev.meteor.modules")
            .forEach(Meteor.modules()::add)

        findInstancesOfSubtypesOf<GCommand>("net.greemdev.meteor.commands")
            .forEach(Meteor.commands()::add)

        TitleScreenInfo.loadLatestRevision()
        initGStarscript()
    }

    @JvmStatic
    fun hudElements() = listOf<HudElementMetadata<*>>(ModuleKeybindHud)

    @JvmStatic
    fun lambdaFactoriesFor(vararg packages: String) =
        packages.forEach {
            MeteorClient.EVENT_BUS.registerLambdaFactory(it) { lookupInMethod, klass ->
                lookupInMethod(null, klass, MethodHandles.lookup())
                    as MethodHandles.Lookup
            }
        }
}
