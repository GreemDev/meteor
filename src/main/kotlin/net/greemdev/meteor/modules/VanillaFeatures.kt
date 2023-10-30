/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.meteor.*

object VanillaFeatures : GModule(
    "vanilla-features", "Adds Meteor settings for easier access to some of Minecraft's toggle settings."
) {
    init {
        canBind = false
        canActivate = false
        serialize = false
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
        defaultValue(mc.options.pauseOnLostFocus)
        onChanged {
            mc.options.pauseOnLostFocus = it
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
}
