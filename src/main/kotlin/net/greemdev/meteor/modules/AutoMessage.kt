/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("HasPlatformType")

package net.greemdev.meteor.modules

import kotlinx.coroutines.*
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.orbit.EventHandler
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.utils.StarscriptError
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.misc.*
import net.minecraft.util.Util

class AutoMessage : GModule(
    "auto-message", "Sends a configurable Starscript message every so often."
) {
    private val sgm = settings.group("Message")
    private val sgc = settings.group("Commands", false)

    private var elapsedTicks: Int = 0
    private var elapsedTicksCommands: Int = 0
    private var messageScript: Script? = null
    private var commandScripts = listOf<Script>()

    val message by sgm string {
        name("message")
        description("The message to send.")
        defaultValue("I {baritone.isPathing ? \"do\" : \"dont\"} like hackers!")
        onChanged { recompile(it) }
        renderStarscript()
    }

    val messageDelay by sgm int {
        name("delay")
        description("The delay of sending a message, in game ticks. Can be no lower than 60 seconds. Default is 5 minutes.")
        defaultValue(6000)
        min(1200)
        max(432000)
        saneSlider()
    }

    val commands by sgc stringList {
        name("commands")
        description("The commands to automatically send.")
        defaultValue("msg GreemDev hi!")
        onChanged { recompile(it) }
        renderStarscript()
    }

    val commandsDelay by sgc int {
        name("delay")
        description("The delay of sending the commands, in game ticks. Can be no lower 600 seconds/10 minutes. Default is 1 hour.")
        defaultValue(72000)
        min(12000)
        max(432000)
        saneSlider()
    }

    @EventHandler
    private fun postTick(ignored: TickEvent.Post) {
        if (messageScript == null) elapsedTicks = 0
        if (commandScripts.isEmpty()) elapsedTicksCommands = 0

        if (mc.isInGame() and isActive) {
            if (elapsedTicks >= messageDelay.get()) {
                MeteorStarscript.run(messageScript)?.also {
                    mc.sendChatMessage(it)
                }
                elapsedTicks = 0
            }
            if (elapsedTicksCommands >= commandsDelay.get()) {
                scope.launch {
                    commandScripts.forEach { script ->
                        delay(250)
                        MeteorStarscript.run(script)
                            ?.removePrefix("/")
                            ?.also(mc::sendCommand)
                    }
                }
                elapsedTicksCommands = 0
            }
            elapsedTicks++
            elapsedTicksCommands++
        } else {
            elapsedTicks = 0
            elapsedTicksCommands = 0
        }
    }

    override fun getWidget(theme: GuiTheme): WWidget =
        theme.table().apply {
            add(theme.button("Starscript Info") {
                Util.getOperatingSystem().open("https://github.com/GreemDev/meteor/wiki/Starscript")
            })

            add(theme.button("Test Current") {
                try {
                    messageScript?.let {
                        MeteorStarscript.run(it)
                    }
                } catch (e: StarscriptError) {
                    error("Message failed: ${e.message}")
                    null
                }?.also {
                    info("Message success: $it")
                }

                if (commandScripts.isEmpty()) {
                    warning("Not testing any command scripts; there are none.")
                } else {
                    commandScripts.forEach {
                        try {
                            MeteorStarscript.run(it)
                        } catch (e: StarscriptError) {
                            error("Command failed: ${e.message}")
                            null
                        }?.also { cmd ->
                            info("Command success: $cmd")
                        }
                    }
                }
            })
        }


    private fun recompile(scripts: List<String>) {
        commandScripts = scripts.map {
            MeteorStarscript.compile(it)
        }
    }

    private fun recompile(script: String) {
        messageScript = MeteorStarscript.compile(script)
    }
}
