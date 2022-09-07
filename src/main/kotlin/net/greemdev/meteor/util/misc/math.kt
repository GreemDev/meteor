/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.utils.Utils

fun Number.clamp(min: Number, max: Number): Double =
    Utils.clamp(this.toDouble(), min.toDouble(), max.toDouble())
