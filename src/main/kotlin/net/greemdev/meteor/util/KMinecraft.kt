/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("KMC")

package net.greemdev.meteor.util

import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.text.Text

fun MinecraftClient.setPlayerPos(x: Double = player!!.x, y: Double = player!!.y, z: Double = player!!.z) {
    player!!.setPos(x, y, z)
}

fun Entity.editPos(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
    setPosition(this.x + x, this.y + y, this.z + z)
}

fun MinecraftClient.isInGame() = player != null && world != null

fun MinecraftClient.player() = player ?: error("The client's PlayerEntity is unavailable.")
fun MinecraftClient.currentWorld() = world ?: error("There is no world loaded currently.")

fun MinecraftClient.sendCommand(command: String, preview: Text? = null) = player().sendCommand(command, preview)
fun MinecraftClient.showMessage(text: Text) = player().sendMessage(text)
fun MinecraftClient.showActionBar(text: Text) = player().sendMessage(text, true)
fun MinecraftClient.showActionBar(message: String) = showActionBar(textOf(message))
fun MinecraftClient.sendChatMessage(message: String, preview: Text? = null) = player().sendChatMessage(message, preview)
fun MinecraftClient.sendAsPlayer(message: String, preview: Text? = null) {
    inGameHud.chatHud.addToMessageHistory(message)

    if (message.startsWith('/'))
        sendCommand(message.substring(1), preview)
    else
        sendChatMessage(message, preview)
}
