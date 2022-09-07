/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.meteor.bool

class VanillaFeatures : GModule(
    "vanilla-features", "Adds Meteor settings for easier access to Minecraft's toggle settings.") {

    init {
        if (isActive)
            toggle()
    }

    val hideHud by sg bool {
        name("hide-HUD")
        description("Hide the player's HUD.")
        defaultValue(mc.options.hudHidden)
        onChanged {
            mc.options.hudHidden = it
        }
    }

    val pauseOnLostFocus by sg bool {
        name("pause-on-lost-focus")
        description("Whether or not to pause the game when you're tabbed out.")
        onChanged {
            mc.options.pauseOnLostFocus = it
        }
    }

    val heldItemTooltips by sg bool {
        name("held-item-tooltips")
        description("Whether or not to display an item's name above your hotbar when you swap to it.")
        defaultValue(mc.options.heldItemTooltips)
        onChanged {
            mc.options.heldItemTooltips = it
        }
    }

    val skipMultiplayerWarning by sg bool {
        name("skip-multiplayer-warning")
        description("Skip the Multiplayer warning.")
        defaultValue(mc.options.skipMultiplayerWarning)
        onChanged {
            mc.options.skipMultiplayerWarning = it
        }
    }

    val smoothCamera by sg bool {
        name("cinematic-camera")
        description("Smoothen your camera movements.")
        defaultValue(mc.options.smoothCameraEnabled)
        onChanged {
            mc.options.smoothCameraEnabled = it
        }
    }

    val advancedTooltips by sg bool {
        name("advanced-tooltips")
        description("Advanced item tooltips in your inventory, showing durability, item ID, etc.")
        defaultValue(mc.options.advancedItemTooltips)
        onChanged {
            mc.options.advancedItemTooltips = it
        }
    }

    override fun onActivate() {
        error("This module cannot be enabled; it is representative of existing Minecraft options.")
        toggle()
    }
}
