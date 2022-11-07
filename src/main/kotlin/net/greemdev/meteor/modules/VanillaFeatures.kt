/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.meteor.bool

object VanillaFeatures : GModule(
    "vanilla-features", "Adds Meteor settings for easier access to some of Minecraft's toggle settings."
) {
    init {
        canBind = false
        canActivate = false
    }

    val hideHud by sg bool {
        name("hide-HUD")
        description("Hide the player's HUD.")
        defaultValue(mc.options.hudHidden)
        serialize(false)
        onChanged {
            mc.options.hudHidden = it
        }
    }

    val pauseOnLostFocus by sg bool {
        name("pause-on-lost-focus")
        description("Whether or not to pause the game when you're tabbed out.")
        serialize(false)
        onChanged {
            mc.options.pauseOnLostFocus = it
        }
    }

    val heldItemTooltips by sg bool {
        name("held-item-tooltips")
        description("Whether or not to display an item's name above your hotbar when you swap to it.")
        defaultValue(mc.options.heldItemTooltips)
        serialize(false)
        onChanged {
            mc.options.heldItemTooltips = it
        }
    }

    val skipMultiplayerWarning by sg bool {
        name("skip-multiplayer-warning")
        description("Skip the Multiplayer warning.")
        defaultValue(mc.options.skipMultiplayerWarning)
        serialize(false)
        onChanged {
            mc.options.skipMultiplayerWarning = it
        }
    }

    val smoothCamera by sg bool {
        name("cinematic-camera")
        description("Smoothen your camera movements.")
        defaultValue(mc.options.smoothCameraEnabled)
        serialize(false)
        onChanged {
            mc.options.smoothCameraEnabled = it
        }
    }

    val advancedTooltips by sg bool {
        name("advanced-tooltips")
        description("Advanced item tooltips in your inventory, showing durability, item ID, etc.")
        defaultValue(mc.options.advancedItemTooltips)
        serialize(false)
        onChanged {
            mc.options.advancedItemTooltips = it
        }
    }
}
