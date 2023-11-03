/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.allowNextChatClear

object ClearChatCommand : GCommand("clearchat", "Clears your chat.", {
    then("andMessageHistory") {
        alwaysRuns {
            clearChat(true)
        }
    }
    alwaysRuns {
        clearChat(false)
    }
}, "cc")

private fun clearChat(clearHistory: Boolean) {
    allowNextChatClear = true
    minecraft.inGameHud.chatHud.clear(clearHistory)
}
