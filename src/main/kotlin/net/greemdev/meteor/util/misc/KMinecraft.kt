/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("KMC")

package net.greemdev.meteor.util.misc

import net.greemdev.meteor.util.text.textOf
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.*

fun MinecraftClient.setPlayerPos(x: Double = player!!.x, y: Double = player!!.y, z: Double = player!!.z) {
    player!!.setPos(x, y, z)
}

fun Entity.editPos(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
    setPosition(this.x + x, this.y + y, this.z + z)
}

operator fun Entity.plus(vec3d: Vec3d): Entity {
    addVelocity(vec3d.x, vec3d.y, vec3d.z)
    return this
}

fun MinecraftClient?.isInGame() = this != null && player != null && world != null

fun MinecraftClient.player() = player ?: error("The client's PlayerEntity is unavailable.")
fun MinecraftClient.currentWorld() = world ?: error("There is no world loaded currently.")

fun PlayerEntity.usableItemStack(): ItemStack? =
    if (!ItemStack.areEqual(mainHandStack, ItemStack.EMPTY))
        mainHandStack
    else if (!ItemStack.areEqual(offHandStack, ItemStack.EMPTY))
        offHandStack
    else null

fun MinecraftClient.rotationVecClient(): Vec3d = player().rotationVecClient

operator fun Vec3d.times(value: Double): Vec3d = multiply(value)
fun MinecraftClient.rotation(): Vec2f = player().rotationClient
fun MinecraftClient.rotationVec(): Vec3d = player().rotationVector

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
