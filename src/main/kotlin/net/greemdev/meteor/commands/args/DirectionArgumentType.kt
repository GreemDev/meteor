/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.args

import net.minecraft.command.argument.EnumArgumentType
import net.minecraft.util.math.Direction

class DirectionArgumentType private constructor() : EnumArgumentType<Direction>(Direction.CODEC, ::enumValues) {
    companion object {
        @JvmStatic fun create() = DirectionArgumentType()
    }
}
