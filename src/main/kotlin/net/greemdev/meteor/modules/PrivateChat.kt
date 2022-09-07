/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.events.game.SendMessageEvent
import meteordevelopment.orbit.EventHandler
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.util.meteor.*

// based on https://github.com/Declipsonator/Meteor-Tweaks/blob/main/src/main/java/me/declipsonator/meteortweaks/modules/GroupChat.java
class PrivateChat : GModule("private-chat", "Turns your chat into a private conversation.") {

    val players by sg stringList {
        name("players")
        description("The players to privately message. Invalid users will be ignored.")
    }

    val dmFormat by sg string {
        name("command-format")
        description("The format of the message command on the server.")
        defaultValue("msg {player} {message}")
    }

    @EventHandler
    private fun interceptMessage(event: SendMessageEvent) {
        val playerList = mc.networkHandler?.playerList.getOrEmpty()
        var foundAny: Boolean
        players().mapNotNull { p ->
            playerList.firstOrNull {
                it.profile.name.equals(p, true)
            }
        }.also {
            foundAny = it.isNotEmpty()
        }.forEach {
            mc.player().sendCommand(dmFormat().withoutPrefix("/")
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
