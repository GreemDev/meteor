/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud

import meteordevelopment.meteorclient.systems.hud.*

abstract class HudElementMetadata<T : HudElement>(val info: HudElementInfo<T>) {
    constructor(group: HudGroup, name: String, description: String, supplier: () -> T) : this(HudElementInfo(group, name, description, supplier))
}
