/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("ChatFeedback")

package net.greemdev.meteor.util.text

import meteordevelopment.meteorclient.systems.config.Config
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*

val feedbackPrefix by invoking {
    val textColor = Config.get().meteorPrefixColor()
    val bracketsColor = Config.get().meteorPrefixBracketsColor()
    val prefix = Config.get().meteorPrefix()
    val brackets = Config.get().meteorPrefixBrackets()

    buildText {
        colored(bracketsColor)
        addString(brackets.left)
        addString(prefix, textColor)
        addString("${brackets.right} ")
    }
}
