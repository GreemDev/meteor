/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("ChatFeedback")

package net.greemdev.meteor.util.text

import net.greemdev.meteor.invoking
import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.meteor.Meteor

@get:JvmName("prefix")
val feedbackPrefix by invoking {
    val textColor = Meteor.config().meteorPrefixColor()
    val bracketsColor = Meteor.config().meteorPrefixBracketsColor()
    val prefix = Meteor.config().chatPrefix()
    val brackets = Meteor.config().meteorPrefixBrackets()

    FormattedText()
        .colored(bracketsColor)
        .addString(brackets.left)
        .addString(prefix.toString(), textColor)
        .addString("${brackets.right} ")
}
