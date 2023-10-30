/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.meteor.*
import net.minecraft.SharedConstants
import net.greemdev.meteor.invoke

// based on https://github.com/Declipsonator/Meteor-Tweaks/blob/main/src/main/java/me/declipsonator/meteortweaks/modules/GameTweaks.java
object GameTweaks : GModule(
    "game-tweaks", "Minor changes to the game experience to improve gameplay."
) {
    val clipboardScreenshots by sg bool {
        name("copy-screenshots")
        description("Copy screenshots to the clipboard instead of creating a file in the game directory.")
        defaultValue(false)
    }

    val developerMode by sg bool {
        name("developer-mode")
        description("Enable debugging features.")
        defaultValue(SharedConstants.isDevelopment)
        onChanged {
            SharedConstants.isDevelopment = it
        }
    }

    val showScore by sg bool {
        name("score")
        description("Whether or not to show the weird Score on the death screen.")
        defaultValue(true)
    }

    fun screenshots() = isActive and clipboardScreenshots()
    fun noScore() = isActive and !showScore()
}
