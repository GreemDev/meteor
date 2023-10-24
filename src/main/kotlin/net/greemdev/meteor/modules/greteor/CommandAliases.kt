/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.greteor

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.utils.StarscriptError
import net.greemdev.meteor.GModule
import net.greemdev.meteor.onFailureOf
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*

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

    fun find(name: String) = aliases.entries.firstOrNull {
        it.key.equals(name, true)
    }

    private var compiledCommands = mapOf<String, Script>()
    var aliases = mapOf<String, String>()
        private set

    val commands by sg stringMap {
        name("aliases")
        description("The command aliases.")
        defaultValue(
            "sp" to "gamemode {player.name} spectator",
            "cr" to "gamemode {player.name} creative"
        )
        wide()
        onChanged(::recompile)
        renderStarscript()
    }


    private fun recompile(scripts: Map<String, String>) {
        compiledCommands = buildMap {
            scripts.forEach { (name, script) ->
                this[name] = MeteorStarscript.compile(script)
            }
        }

        if (compiledCommands.isNotEmpty()) {
            aliases = compiledCommands.mapNotNull { (name, script) ->
                runCatching {
                    name to MeteorStarscript.run(script)
                }.apply {
                    onFailureOf<StarscriptError> {
                        error("Command script failed: ${it.message}")
                    }
                }.getOrNull()
            }.toMap()
        }
    }
}
