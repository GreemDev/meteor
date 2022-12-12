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
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.starscript.CompiledStarscripts

object CommandAliases : GModule(
    "command-aliases",
    "Use commands dynamically formatted with Starscript.\nAliases are accessible via Meteor command 'ca'."
) {
    init {
        runInMainMenu = true
        canBind = false
        canActivate = false
        forceDisplayChatFeedbackCheckbox = true
    }

    fun find(name: String) = mapped.entries.firstOrNull {
        it.key.equals(name, true)
    }

    private val commandScripts = CompiledStarscripts()
    var mapped = mapOf<String, String>()
        private set

    val commands by sg stringList {
        name("commands")
        description("The command aliases.\nFormat is &zname :: command")
        defaultValue("sp :: gamemode {player.name} spectator", "cr :: gamemode {player.name} creative")
        onChanged { recompile(it) }
        renderStarscript()
    }


    private fun recompile(scripts: List<String>) {
        commandScripts.setScripts(scripts)

        if (commandScripts.isNotEmpty()) {

            val (results, errors) = commandScripts.runAll()

            errors.forEach { error("Command script failed: ${it.message}") }


            mapped = results.mapNotNull {
                val name = it.substringBefore("::", "%null%")
                val cstr = it.substringAfter("::", "%null%")
                if ("%null%" in arrayOf(name, cstr)) {
                    error("Command string '$it' didn't contain required separator ::")
                    null
                } else name.trim() to cstr.trim()
            }.toMap()

        }
    }
}
