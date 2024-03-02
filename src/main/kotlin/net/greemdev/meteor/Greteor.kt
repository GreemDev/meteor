/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.commands.Commands
import meteordevelopment.meteorclient.systems.hud.HudGroup
import meteordevelopment.meteorclient.systems.modules.Category
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.greemdev.meteor.hud.HudElementMetadata
import net.greemdev.meteor.hud.element.ModuleKeybindHud
import net.greemdev.meteor.modules.damageNumbers.DamageNumbers
import net.greemdev.meteor.util.className
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.starscript.initGStarscript
import net.greemdev.meteor.util.misc.GVersioning
import net.greemdev.meteor.util.modLoader
import net.greemdev.meteor.util.tryGet
import net.minecraft.block.Block
import net.minecraft.client.option.KeyBinding
import net.minecraft.item.Items
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.lang.reflect.Modifier

private val category = Category(className<Greteor>(), Items.LIME_CONCRETE_POWDER.defaultStack)
private val hudGroup = HudGroup(className<Greteor>())

object Greteor {

    @JvmStatic
    @get:JvmName("logger")
    val logger by log4j("Greteor")

    @JvmStatic
    fun category() = category
    @JvmStatic
    fun hudGroup() = hudGroup

    @JvmStatic
    fun init() {
        GModule.subtypes().forEach(Meteor.modules()::add)

        GCommand.subtypes().forEach(Commands::add)

        initGStarscript()

        WorldRenderEvents.AFTER_ENTITIES.register(DamageNumbers::render)
    }

    @JvmStatic
    fun hudElements() = listOf<HudElementMetadata<*>>(ModuleKeybindHud)

    object Tags {
        val nonStrippedLogs: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, resource("non-stripped-logs"))
    }

    @JvmStatic
    fun debug(message: String, inProd: Boolean) {
        if (modLoader.isDevelopmentEnvironment || inProd)
            logger.info("dbg -> $message")
    }

    @JvmStatic
    infix fun debug(message: String) {
        if (modLoader.isDevelopmentEnvironment)
            logger.info("dbg -> $message")
    }

    @JvmStatic
    infix fun debug(message: Getter<String>) = debug(message())

    @JvmStatic
    fun debug(inProd: Boolean = false, message: Getter<String>) = debug(message(), inProd)

    @JvmStatic
    @get:JvmName("keybinds")
    val keybindings: List<KeyBinding> by lazy {
        MeteorClient::class.java.fields
            .filter {
                KeyBinding::class.java.isAssignableFrom(it.type) and Modifier.isStatic(it.modifiers)
            }.mapNotNull(Field::tryGet)
    }
}
