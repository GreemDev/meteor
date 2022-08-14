/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud

import meteordevelopment.meteorclient.systems.hud.*

interface HudElementMetadata<T : HudElement> {
    val elementInfo: HudElementInfo<T>
}
