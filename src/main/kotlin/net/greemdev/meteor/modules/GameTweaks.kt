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
object GameTweaks : GModule.Misc(
    "game-tweaks", "Minor changes to the game experience to improve gameplay."
) {
    private val clipboardScreenshots by sg bool {
        name("copy-screenshots")
        description("Copy screenshots to the clipboard instead of creating a file in the game directory.")
        defaultValue(false)
    }

    private val specialLogo by sg bool {
        name("special-logo")
        description("Subtly changes the Minecraft logo on the title screen.")
        defaultValue(false)
    }

    private val saveScreenshotFileToo by sg bool {
        name("save-screenshots")
        description("Save screenshots into their normal folder as well as copying the file to the clipboard.")
        visible(clipboardScreenshots)
        defaultValue(false)
    }

    private val showScore by sg bool {
        name("score")
        description("Whether or not to show the weird Score on the death screen.")
        defaultValue(true)
    }

    @JvmStatic
    fun screenshots() = isActive and clipboardScreenshots()
    @JvmStatic
    fun screenshotFile() = screenshots() and saveScreenshotFileToo()
    @JvmStatic
    fun noScore() = isActive and !showScore()

    @JvmStatic
    fun minceraft() = isActive and specialLogo()
}
