/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("HasPlatformType")

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.type.StringComparisonType
import net.greemdev.meteor.util.*

class AntiChatSpam : GModule(
    "anti-chat-spam",
    "Prevent messages from displaying. Useful for hiding automated messages."
) {

    private val sg = settings.group()

    val filters by sg stringList {
        name("filters")
        description("The contents to check if a message matches.")
    }

    val comparisonType by sg.enum<StringComparisonType> {
        name("comparison")
        description("How to determine if the message should be hidden.")
        defaultValue(StringComparisonType.Contains)
        visible { filters.get().isNotEmpty() }
    }

    val ignoreCase by sg bool {
        name("ignore-case")
        description("Whether the checking should ignore character casing.")
        defaultValue(true)
        visible { comparisonType.isVisible }
    }

    @EventHandler
    fun receiveMessage(e: ReceiveMessageEvent) {
        filters.get().forEach {
            val comparer = comparisonType.get()
            if (comparer.compare(e.message.string, it, ignoreCase.get()))
                e.cancel()
        }
    }
}
