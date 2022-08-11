/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.game.ReceiveMessageEvent
import meteordevelopment.meteorclient.utils.player.ChatUtils
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.bool
import net.greemdev.meteor.util.enum
import net.greemdev.meteor.util.group
import net.greemdev.meteor.util.stringList

class AntiChatSpam : GModule(
    "anti-chat-spam",
    "Prevent messages from displaying. Useful for hiding automated messages."
) {

    private val sg = settings.group()

    val filters by sg.stringList {
        name("filters")
        description("The contents to check if a message matches.")
    }

    val comparisonType by sg.enum<StringComparisonType> {
        name("comparison")
        description("How to determine if the message should be hidden.")
        defaultValue(StringComparisonType.Contains)
        visible { filters.get().isNotEmpty() }
    }

    val ignoreCase by sg.bool {
        name("ignore-case")
        description("Whether the checking should ignore character casing.")
        defaultValue(true)
        visible { comparisonType.isVisible }
    }

    @EventHandler
    fun receiveMessage(e: ReceiveMessageEvent) {
        if (filters.get().isNotEmpty()) {
            filters.get().forEach {
                if (comparisonType.get().compare(e.message.string, it, ignoreCase.get()))
                    e.cancel()
            }
        }
    }
}

enum class StringComparisonType {
    Equals,
    Contains,
    StartsWith,
    EndsWith;

    fun compare(base: String, to: String, ignoreCase: Boolean = true) = when (this) {
        Equals -> base.equals(to, ignoreCase)
        Contains -> base.contains(to, ignoreCase)
        StartsWith -> base.startsWith(to, ignoreCase)
        EndsWith -> base.endsWith(to, ignoreCase)
    }

    override fun toString() = when (this) {
        Equals -> "Equals"
        Contains -> "Contains"
        StartsWith -> "Starts With"
        EndsWith -> "Ends With"
    }
}
