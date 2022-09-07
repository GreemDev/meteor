/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("HasPlatformType")

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.utils.StarscriptError
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.stringList

class CommandAliases : GModule(
    "command-aliases", "Use commands dynamically formatted with Starscript via Meteor command 'ca'."
) {
    init {
        runInMainMenu = true
    }

    private var commandScripts = listOf<Script>()
    var mapped = mapOf<String, String>()
        private set

    val commands by sg stringList {
        name("commands")
        description("The command aliases. Format is 'name :: command'")
        defaultValue("sp :: gamemode {player.name} spectator", "cr :: gamemode {player.name} creative")
        onChanged { recompile(it) }
        renderStarscript()
    }

    override fun onActivate() {
        error("This module is meant for configuration; disabling.")
        toggle()
    }

    private fun recompile(scripts: List<String>) {
        commandScripts = scripts.map { MeteorStarscript.compile(it) }

        if (commandScripts.isNotEmpty()) {
            mapped = commandScripts.mapNotNull {
                try {
                    MeteorStarscript.run(it)
                } catch (e: StarscriptError) {
                    error("Command script failed: ${e.message}")
                    null
                }
            }.mapNotNull {
                val name = it.substringBefore("::", "%null%")
                val cstr = it.substringAfter("::", "%null%")
                if ("%null%" in arrayOf(name, cstr)) {
                    error("Command string didn't contain required separator ::")
                    null
                } else name.trim() to cstr.trim()
            }.associate()
        }
    }
}
