/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.game.SendMessageEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.util.meteor.*

// based on https://github.com/Declipsonator/Meteor-Tweaks/blob/main/src/main/java/me/declipsonator/meteortweaks/modules/GroupChat.java
object PrivateChat : GModule("private-chat", "Turns your chat into a private conversation.") {

    val players by sg stringList {
        name("players")
        description("The players to privately message. Allows names & UUIDs. &4Invalid users will be ignored&r.")
    }

    val commandFormat by sg string {
        name("command-format")
        description("The format of the message command on the server.")
        defaultValue("msg {player} {message}")
    }

    @EventHandler
    private fun interceptMessage(event: SendMessageEvent) {
        var foundAny: Boolean
        players().mapNotNull { p ->
            mc.networkHandler.findFirstPlayerListEntry {
                it.profile.name.equals(p, true)
            } ?: mc.networkHandler.findFirstPlayerListEntry {
                it.profile.id.equals(p)
            }
        }.also {
            foundAny = it.isNotEmpty()
        }.forEach {
            mc.sendCommand(commandFormat()
                .replace("{player}", it.profile.name)
                .replace("{message}", event.message)
            )
        }

        if (!foundAny) {
            error("There's no recipients online currently; disabling.")
            toggle()
        }

        event.cancel()
    }
}
