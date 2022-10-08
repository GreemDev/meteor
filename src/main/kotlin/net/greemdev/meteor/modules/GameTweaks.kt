/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*

// based on https://github.com/Declipsonator/Meteor-Tweaks/blob/main/src/main/java/me/declipsonator/meteortweaks/modules/GameTweaks.java
object GameTweaks : GModule(
    "game-tweaks", "Minor changes to the game experience to improve gameplay."
) {
    private val sgGP = settings group "Gameplay"
    private val sgR = settings group "Render"

    val lessAnnoyingBats by sgGP bool {
        name("less-annoying-bats")
        description("Makes bats less annoying and obtrusive.")
        defaultValue(true)
    }

    /*val silenceNarrator by sgGP bool {
        name("silence-narrator")
        description("Silences Narrator. Permanently. As long as the checkbox is ticked.")
        defaultValue(false)
    }*/

    val migratorCapes by sgR bool {
        name("migrator-capes")
        description("Whether or not to render Migrator capes on players.")
        defaultValue(true)
    }

    val clipboardScreenshots by sg bool {
        name("copy-screenshots")
        description("Copy screenshots to the clipboard instead of creating a file in the game directory.")
        defaultValue(false)
    }

    val showScore by sg bool {
        name("score")
        description("Whether or not to show the weird Score on the death screen.")
        defaultValue(true)
    }

    fun bats() = isActive and lessAnnoyingBats()
    fun noMigrators() = isActive and !migratorCapes()
    //fun silenceNarrator() = isActive and silenceNarrator.get()
    fun noScore() = isActive and !showScore()
    fun screenshots() = isActive and clipboardScreenshots()
}
