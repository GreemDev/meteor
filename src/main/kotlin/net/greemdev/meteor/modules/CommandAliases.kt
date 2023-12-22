/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.utils.StarscriptError
import net.greemdev.meteor.*
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
    }?.value

    private var compiledCommands = mapOf<String, Script>()
    var aliases = mapOf<String, String>()
        private set

    val commands by sg stringMap {
        name("aliases")
        description("The command aliases. You can use &z%s &ras a placeholder for arguments you pass to the Command Aliases command.")
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
            putAll(scripts.map { (name, script) ->
                name to MeteorStarscript.compile(script)
            })
        }

        if (compiledCommands.isNotEmpty()) {
            aliases = compiledCommands.mapNotNull { (name, script) ->
                runCatching {
                    name to MeteorStarscript.run(script)
                }.onFailureOf(StarscriptError::class) {
                    error("Command script for alias '$name' failed: ${it.message}")
                }.getOrNull()
            }.toMap()
        }
    }
}
