/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.gui.screens.ModuleScreen
import meteordevelopment.meteorclient.gui.screens.ModulesScreen
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.misc.*

// Based on https://github.com/AntiCope/meteor-rejects/blob/master/src/main/java/anticope/rejects/modules/Boost.java
class Dash : GModule("dash", "Boosts you forward in the direction you're looking.") {
    val power by sg double {
        name("power")
        description("The strength of your dash.")
        defaultValue(2.75)
        min(0.5)
        max(10.0)
        saneSlider()
    }

    init {
        chatFeedback = false
    }

    override fun onActivate() {
        if (mc.player != null) {
            when (mc.currentScreen) {
                is ModuleScreen, is ModulesScreen ->
                    info("Did not activate dash because you're in a module menu. Please use the keybind in-game.")
                else ->
                    mc.player() + mc.rotationVecClient() * power()
            }
        }
        toggle()
    }
}
