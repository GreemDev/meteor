/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands

import net.greemdev.meteor.GCommand
import net.greemdev.meteor.util.misc.clearChat

object ClearChatCommand : GCommand("clearchat", "Clears your chat.", {
    runs {
        clearChat()
    }
    then("andMessageHistory") {
        runs {
            clearChat(true)
        }
    }
}, "cc")
