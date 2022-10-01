/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

/*package net.greemdev.meteor.type

import com.mojang.text2speech.Narrator
import net.greemdev.meteor.modules.GameTweaks
import net.greemdev.meteor.util.getOrNull
import net.greemdev.meteor.util.invoking
import net.greemdev.meteor.util.meteor.Meteor

object DynamicNarrator : Narrator {

    val narrator by lazy<Narrator> { Narrator.getNarrator() }
    val silenced by invoking {
        getOrNull { Meteor.module<GameTweaks>().silenceNarrator() } ?: false
    }

    override fun say(msg: String?, interrupt: Boolean) {
        if (silenced) return

        narrator.say(msg, interrupt)
    }

    override fun clear() = narrator.clear()

    override fun active() = false

    override fun destroy() = narrator.destroy()
}*/
