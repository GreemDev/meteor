/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import net.greemdev.meteor.GModule

object IgnoreWorldBorder : GModule.World(
    "ignore-border",
    "Removes world border collision client-side."
)
