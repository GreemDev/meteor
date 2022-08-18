/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.Message
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType

fun simpleCommandException(message: Message) = lazy {
    SimpleCommandExceptionType(message)
}

fun dynamicCommandException(func: (Any) -> Message) = lazy {
    DynamicCommandExceptionType(func)
}
