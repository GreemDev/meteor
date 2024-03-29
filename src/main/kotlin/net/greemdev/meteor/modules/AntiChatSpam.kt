/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.type.StringComparisonType
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.invoke

object AntiChatSpam : GModule.Misc(
    "anti-chat-spam", "Prevent messages from displaying.\nUseful for hiding automated messages."
) {
    val filters by sg stringList {
        name("filters")
        description("The contents to check if a message matches.")
    }

    val comparisonType by sg.enum<StringComparisonType> {
        name("comparison")
        description("How to determine if the message should be hidden.")
        defaultValue(StringComparisonType.Contains)
        visible { filters().isNotEmpty() }
    }

    val ignoreCase by sg bool {
        name("ignore-case")
        description("Whether the checking should ignore character casing.")
        defaultValue(true)
        visible(comparisonType::isVisible)
    }

    @EventHandler
    fun receivedMessage(e: ReceiveMessageEvent) {
        filters().forEach {
            if (comparisonType().compare(e.message.string, it, ignoreCase())) {
                e.cancel()
                return
            }
        }
    }
}
