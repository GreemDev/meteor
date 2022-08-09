/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.events.world.TickEvent
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.settings.IntSetting
import meteordevelopment.meteorclient.settings.Setting
import meteordevelopment.meteorclient.settings.StringListSetting
import meteordevelopment.meteorclient.settings.StringSetting
import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.meteorclient.utils.player.ChatUtils
import meteordevelopment.orbit.EventHandler
import meteordevelopment.starscript.Script
import meteordevelopment.starscript.Starscript
import meteordevelopment.starscript.utils.StarscriptError
import net.greemdev.meteor.GModule
import net.greemdev.meteor.util.*
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util

class AutoMessage : GModule(
    "auto-message", "Sends a configurable Starscript message every so often."
) {
    private val sgMessage = settings.createGroup("Message", true)
    private val sgCommands = settings.createGroup("Commands", false)

    private var elapsedTicks: Int = 0
    private var elapsedTicksCommands: Int = 0
    private var messageScript: Script? = null
    private val commandScripts = mutableListOf<Script>()

    val message: Setting<String> = sgMessage.add(StringSetting.Builder()
        .name("message")
        .description("The message to send. Supports Starscript.")
        .defaultValue("I {baritone.is_pathing ? \"do\" : \"dont\"} like hackers!")
        .onChanged { recompile(it) }
        .renderStarscript()
        .build()
    )

    val messageDelay: Setting<Int> = sgMessage.add(
        IntSetting.Builder()
            .name("delay")
            .description("The delay of sending a message, in game ticks. Can be no lower than 60 seconds. Default is 5 minutes.")
            .defaultValue(6000)
            .min(1200)
            .max(432000)
            .saneSlider()
            .build()
    )

    val commands: Setting<List<String>> = sgCommands.add(StringListSetting.Builder()
        .name("commands")
        .description("The commands to automatically send. Supports Starscript.")
        .defaultValue("msg GreemDev hi!")
        .onChanged { recompile(it) }
        .renderStarscript()
        .build()
    )

    val commandsDelay: Setting<Int> = sgCommands.add(
        IntSetting.Builder()
            .name("delay")
            .description("The delay of sending the commands, in game ticks. Can be no lower 600 seconds/10 minutes. Default is 1 hour.")
            .defaultValue(72000)
            .min(12000)
            .max(432000)
            .saneSlider()
            .build()
    )

    @OptIn(DelicateCoroutinesApi::class)
    @EventHandler
    private fun postTick(unused: TickEvent.Post) {
        if (messageScript == null) elapsedTicks = 0
        if (commandScripts.isEmpty()) elapsedTicksCommands = 0

        if (Utils.canUpdate()) {
            if (elapsedTicks >= messageDelay.get()) {
                MeteorStarscript.run(messageScript)?.also {
                    mc.player!!.sendChatMessage(it)
                }
                elapsedTicks = 0
            }
            if (elapsedTicksCommands >= commandsDelay.get()) {
                GlobalScope.launch {
                    commandScripts.forEach {
                        delay(250)
                        MeteorStarscript.run(it)?.also(mc.player!!::sendCommand)
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
            add(theme.button("Starscript Info")
                .action {
                    Util.getOperatingSystem().open("https://github.com/MeteorDevelopment/starscript/wiki")
                })
            add(theme.button("Test Current")
                .action {
                    try {
                        messageScript?.let {
                            MeteorStarscript.run(it)
                        }
                    } catch (e: StarscriptError) {
                        ChatUtils.error("AutoMessage", "Message failed: ${e.message}")
                        null
                    }?.also {
                        ChatUtils.info("AutoMessage", "Message success: $it")
                    }

                    if (commandScripts.isEmpty()) {
                        ChatUtils.warning("AutoMessage", "Not testing any command scripts; there are none.")
                    } else {
                        commandScripts.forEach {
                            try {
                                MeteorStarscript.run(it)
                            } catch (e: StarscriptError) {
                                ChatUtils.error("AutoMessage", "Command failed: ${e.message}")
                                null
                            }?.also { cmd ->
                                ChatUtils.info("AutoMessage", "Command success: $cmd")
                            }
                        }
                    }
                })
        }


    private fun recompile(scripts: List<String>) {
        commandScripts.clear()
        scripts.forEach { src ->
            MeteorStarscript.compile(src)?.also {
                commandScripts.add(it)
            }
        }
    }

    private fun recompile(script: String) {
        MeteorStarscript.compile(script)?.also {
            messageScript = it
        }
    }
}
