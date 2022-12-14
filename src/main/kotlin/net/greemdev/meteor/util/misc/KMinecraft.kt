/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("KMC")

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.mixin.ChatHudAccessor
import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.world.Dimension
import net.greemdev.meteor.Initializer
import net.greemdev.meteor.Mapper
import net.greemdev.meteor.Predicate
import net.greemdev.meteor.getOrNull
import net.greemdev.meteor.util.meteor.resource
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.text.textOf
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.hud.ChatHud
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.resource.Resource
import net.minecraft.text.Text
import net.minecraft.util.math.*
import net.minecraft.world.GameMode
import java.util.Optional

fun MinecraftClient.setPlayerPos(x: Double = player!!.x, y: Double = player!!.y, z: Double = player!!.z) {
    player!!.setPos(x, y, z)
}

fun Entity.editPos(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
    setPosition(this.x + x, this.y + y, this.z + z)
}

fun MinecraftClient.getMeteorResource(path: String): Optional<Resource> = resourceManager.getResource(resource(path))

operator fun Entity.plus(vec3d: Vec3d): Entity {
    addVelocity(vec3d.x, vec3d.y, vec3d.z)
    return this
}

fun MinecraftClient?.isInGame() = this != null && player != null && world != null

fun MinecraftClient.player() = player ?: error("The client's PlayerEntity is unavailable.")
fun MinecraftClient.currentWorld() = world ?: error("There is no world loaded currently.")
fun MinecraftClient.network() = networkHandler ?: error("There is no network handler available.")
infix fun MinecraftClient.network(func: Initializer<ClientPlayNetworkHandler>) {
    networkHandler?.func()
}

fun ClientWorld?.currentDimension() =
    when (this?.registryKey?.value?.path) {
        "the_nether" -> Dimension.Nether
        "the_end" -> Dimension.End
        else -> Dimension.Overworld
    }

fun ChatHud.clearChat(includeHistory: Boolean = false) {
    val access = Utils.cast<ChatHudAccessor>(this) //direct java cast usage, not sure how kotlin handles accessor mixin casts
    access.messageQueue.clear()
    access.visibleMessages.clear()
    access.messages.clear()
    if (includeHistory)
        access.messageHistory.clear()
}

infix fun ItemStack.eq(other: ItemStack) = ItemStack.areEqual(this, other)
infix fun ItemStack.neq(other: ItemStack) = !(this eq other)

fun PlayerEntity.ping(): Int =
    minecraft.networkHandler?.getPlayerListEntry(uuid)?.latency ?: -1

fun PlayerEntity.currentGameMode() =
    minecraft.networkHandler?.getPlayerListEntry(uuid)?.gameMode ?: GameMode.SPECTATOR

fun ClientPlayNetworkHandler?.findPlayerListEntries(predicate: Predicate<PlayerListEntry>) =
    this?.playerList.orEmpty().filter(predicate).filterNotNull()

fun ClientPlayNetworkHandler?.findFirstPlayerListEntry(predicate: Predicate<PlayerListEntry>) = this?.playerList.orEmpty().firstOrNull(predicate)

fun PlayerEntity.usableItemStack(): ItemStack? =
    if (mainHandStack neq ItemStack.EMPTY)
        mainHandStack
    else if (offHandStack neq ItemStack.EMPTY)
        offHandStack
    else null

fun MinecraftClient.clientRotationVec(): Vec3d = player().rotationVecClient

/** Operator overload for [Vec3d.multiply]. */
operator fun Vec3d.times(value: Double): Vec3d = multiply(value)

/** Unary operator overload for [Vec3d.negate]. */
operator fun Vec3d.unaryMinus(): Vec3d = negate()

/** Operator overload for [Vec3d.add]. */
operator fun Vec3d.plus(value: Vec3d): Vec3d = add(value)

/** Operator overload for [Vec3d.add]. */
operator fun Vec3d.plus(xyz: Triple<Double, Double, Double>): Vec3d = add(xyz.first, xyz.second, xyz.third)

/** Operator overload for [Vec3d.subtract]. */
operator fun Vec3d.minus(value: Vec3d): Vec3d = subtract(value)

/** Operator overload for [Vec3d.subtract]. */
operator fun Vec3d.minus(xyz: Triple<Double, Double, Double>): Vec3d = subtract(xyz.first, xyz.second, xyz.third)

/** Operator overload for [Vec3d.distanceTo]. */
operator fun Vec3d.rangeTo(value: Vec3d) = distanceTo(value)

/** Operator overload for [Vec3d.relativize]. */
operator fun Vec3d.rem(value: Vec3d): Vec3d = relativize(value)

/** Operator overload for [Vec3d.crossProduct]. */
operator fun Vec3d.div(value: Vec3d): Vec3d = crossProduct(value)

fun MinecraftClient.rotation(): Vec2f = player().rotationClient
fun MinecraftClient.rotationVec(): Vec3d = player().rotationVector

fun MinecraftClient.sendCommand(command: String, preview: Text? = null) = player().sendCommand(command.removePrefix("/"), preview)
fun MinecraftClient.showMessage(text: Text) = player().sendMessage(text)
fun MinecraftClient.showActionBar(text: Text) = player().sendMessage(text, true)
fun MinecraftClient.showActionBar(message: String) = showActionBar(textOf(message))
fun MinecraftClient.sendChatMessage(message: String, preview: Text? = null) = player().sendChatMessage(message, preview)
fun MinecraftClient.sendAsPlayer(message: String, preview: Text? = null) {
    inGameHud.chatHud.addToMessageHistory(message)

    if (message.startsWith('/'))
        sendCommand(message, preview)
    else
        sendChatMessage(message, preview)
}
