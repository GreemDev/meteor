/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.commands.Commands
import meteordevelopment.meteorclient.systems.hud.HudGroup
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Modules
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.greemdev.meteor.hud.HudElementDescriptor
import net.greemdev.meteor.hud.element.*
import net.greemdev.meteor.modules.damageNumbers.DamageNumbers
import net.greemdev.meteor.modules.flightHud.ElytraFlightHud
import net.greemdev.meteor.util.className
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.starscript.initGStarscript
import net.greemdev.meteor.util.modLoader
import net.minecraft.block.Block
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey

private val category = Category(className<Greteor>(), Items.LIME_CONCRETE_POWDER.defaultStack)
private val hudGroup = HudGroup(className<Greteor>())

object Greteor {
    @JvmStatic
    var gameStartTime: Long = -1
        set(value) {
            if (field == -1L)
                field = value
            else
                logger.warn("Game start timestamp may only be set once. Call ignored.")
        }

    @JvmStatic
    @get:JvmName("logger")
    val logger by log4j("Greteor")

    @JvmStatic
    fun category() = category
    @JvmStatic
    fun hudGroup() = hudGroup

    @JvmStatic
    fun init() {
        Modules.addAll(GModule.subtypeInstances)
        Commands.addAll(GCommand.subtypeInstances)

        HudRenderCallback.EVENT.register { drawContext, _ -> ElytraFlightHud.render(drawContext) }

        WorldRenderEvents.END.register(DamageNumbers::render)

        initGStarscript()
    }

    @JvmStatic
    fun debug(message: String, inProd: Boolean = false) {
        if (modLoader.isDevelopmentEnvironment || inProd)
            logger.info("dbg| $message")
    }

    @JvmStatic
    fun hudElements() = listOf<HudElementDescriptor<*>>(ModuleKeybindHud)

    object Tags {
        val nonStrippedLogs: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, resource("non-stripped-logs"))
    }
}
