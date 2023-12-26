/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.entity.player.InteractBlockEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.Greteor
import net.greemdev.meteor.invoke
import net.greemdev.meteor.test
import net.greemdev.meteor.util.meteor.bool
import net.greemdev.meteor.util.meteor.resource
import net.greemdev.meteor.util.misc.*
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import java.util.Optional
import java.util.function.Predicate

object AntiLogStrip : GModule.World("anti-log-strip", "Prevents you from stripping logs.") {

    val swingHand by sg bool {
        name("swing-hand")
        description("Shows hand swing animation when stripping is blocked.")
        defaultValue(false)
    }

    private val _chatFeedback by sg bool {
        name("chat-feedback")
        description("Notifies you in chat when an attempt to strip a log was blocked.")
        defaultValue(false)
    }

    @EventHandler
    private fun blockInteracted(event: InteractBlockEvent) {
        if (mc.player().mainHandStack.registryEntry in ItemTags.AXES) {
            mc.crosshairTarget.ifBlock { bhr ->
                if (bhr.blockPos.getStateInWorld().test { it.registryEntry in Greteor.Tags.nonStrippedLogs }) {
                    if (swingHand()) mc.player().swingHand(mc.player().activeHand)
                    if (_chatFeedback()) info("Log stripping prevented!")
                    event.cancel()
                }
            }
        }
    }
}
