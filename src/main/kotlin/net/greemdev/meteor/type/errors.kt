/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type

import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt
import net.greemdev.meteor.util.getOrNull
import net.greemdev.meteor.util.tryOrIgnore
import java.lang.Exception

class ErrorPrompt(cause: Throwable? = null, p: () -> OkPrompt) : Exception(null, cause) {
    val prompt by lazy(p)
    fun tryShow() = getOrNull(prompt::show) ?: false
}
