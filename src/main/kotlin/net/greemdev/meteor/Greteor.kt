/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.systems.hud.HudGroup
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Modules
import net.greemdev.meteor.gui.theme.round.RoundedTheme
import net.greemdev.meteor.hud.element.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.misc.TitleScreenInfo
import net.minecraft.item.Items
import java.lang.invoke.MethodHandles

private val category = Category("Greteor", Items.LIME_CONCRETE_POWDER.defaultStack)
private val hudGroup = HudGroup("Greteor")

object Greteor {

    @JvmStatic
    val logger by log4j("Greteor")

    fun category() = category
    fun hudGroup() = hudGroup

    @JvmStatic
    fun init() {
        createSubtypesOf<GModule>("net.greemdev.meteor.modules")
            .forEach(Meteor.modules()::add)
        createSubtypesOf<GCommand>("net.greemdev.meteor.commands")
            .forEach(Meteor.commands()::add)
        TitleScreenInfo.loadLatestRevision()
        initGStarscript()
    }

    @JvmStatic
    fun categories() {
        Modules.registerCategory(category())
    }

    @JvmStatic
    fun roundedTheme() {
        GuiThemes.add(RoundedTheme())
    }

    @JvmStatic
    fun hudElements() = listOf(ModuleKeybindHud)

    @JvmStatic
    fun lambdaFactoriesFor(vararg packages: String) =
        packages.forEach {
            MeteorClient.EVENT_BUS.registerLambdaFactory(it) { lookupInMethod, klass ->
                lookupInMethod(null, klass, MethodHandles.lookup())
                    as MethodHandles.Lookup
            }
        }
}
