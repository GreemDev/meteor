/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("KMC")
@file:Suppress("FunctionName")

package net.greemdev.meteor.util.misc

import com.mojang.blaze3d.systems.RenderSystem
import meteordevelopment.meteorclient.mixin.accessor.MinecraftClientAccessor
import meteordevelopment.meteorclient.mixininterface.IVec3d
import meteordevelopment.meteorclient.utils.player.ChatUtils
import meteordevelopment.meteorclient.utils.player.PlayerUtils
import meteordevelopment.meteorclient.utils.world.Dimension
import net.greemdev.meteor.*
import net.greemdev.meteor.util.empty
import net.greemdev.meteor.util.meteor.resource
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.text.*
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.PlayerListEntry
import net.minecraft.client.render.BufferBuilder
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.world.ClientWorld
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.resource.Resource
import net.minecraft.text.Text
import net.minecraft.util.crash.CrashException
import net.minecraft.util.crash.CrashReport
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.*
import net.minecraft.world.GameMode
import net.minecraft.world.WorldView
import org.joml.Vector4f
import java.util.Optional

@JvmField
var allowNextChatClear = false

@JvmOverloads
fun clearChat(clearHistory: Boolean = false) {
    allowNextChatClear = true
    minecraft.inGameHud.chatHud.clear(clearHistory)
}

inline fun HitResult?.ifEntity(block: ValueAction<EntityHitResult>) {
    block(this?.tryCast<EntityHitResult>() ?: return)
}

inline fun HitResult?.ifBlock(block: ValueAction<BlockHitResult>) {
    block(this?.tryCast<BlockHitResult>()?.takeUnless { it.type == HitResult.Type.MISS } ?: return)
}

inline fun HitResult?.ifMissed(block: ValueAction<BlockHitResult>) {
    block(this?.tryCast<BlockHitResult>()?.takeIf { it.type == HitResult.Type.MISS } ?: return)
}

@get:JvmName("fps")
val currentFps by invoking(MinecraftClientAccessor::getFps)

fun MinecraftClient.setPlayerPos(x: Double = playerX, y: Double = playerY, z: Double = playerZ) {
    player().setPos(x, y, z)
}

var MinecraftClient.playerX
    get() = player().x
    set(value) {
        setPlayerPos(x = value)
    }

var MinecraftClient.playerY
    get() = player().y
    set(value) {
        setPlayerPos(y = value)
    }

var MinecraftClient.playerZ
    get() = player().z
    set(value) {
        setPlayerPos(z = value)
    }

fun Entity.editPos(x: Double = 0.0, y: Double = 0.0, z: Double = 0.0) {
    setPosition(this.x + x, this.y + y, this.z + z)
}

fun MinecraftClient.getMeteorResource(path: String): Optional<Resource> = resourceManager.getResource(resource(path))
fun getMeteorResource(path: String) = minecraft.getMeteorResource(path)

fun MinecraftClient?.isInGame() = this != null && player != null && world != null

fun MinecraftClient.player() = player ?: error("The client's PlayerEntity is unavailable.")
fun MinecraftClient.currentWorld() = world ?: error("There is no world loaded currently.")
fun MinecraftClient.network() = networkHandler ?: error("There is no network handler available.")

/**
 * Runs [func] on the [MinecraftClient]'s [ClientPlayNetworkHandler] if it is not null.
 */
infix fun MinecraftClient.network(func: Initializer<ClientPlayNetworkHandler>) {
    networkHandler?.func()
}

fun Vector4f.setAsRenderSystemShaderColor() =
    RenderSystem.setShaderColor(x(), y(), z(), w())

fun setRenderSystemShaderColor(components: Vector4f) = components.setAsRenderSystemShaderColor()

fun ClientWorld?.currentDimension() =
    when (this?.registryKey?.value?.path) {
        "the_nether" -> Dimension.Nether
        "the_end" -> Dimension.End
        else -> Dimension.Overworld
    }

operator fun<T> TagKey<T>.contains(entry: RegistryEntry<T>) = entry.isIn(this)
operator fun<T> RegistryEntry<T>.contains(tagKey: TagKey<T>) = isIn(tagKey)

fun PlayerEntity.pingOrNull() =
    minecraft.networkHandler?.getPlayerListEntry(uuid)?.latency

val PlayerEntity.ping
    get() = pingOrNull() ?: -1

val PlayerEntity.currentGameMode
    get() = minecraft.networkHandler?.getPlayerListEntry(uuid)?.gameMode ?: GameMode.SPECTATOR

fun ClientPlayNetworkHandler?.findPlayerListEntries(predicate: Predicate<PlayerListEntry>) =
    this?.playerList.orEmpty().filterNotNull().filter(predicate)

fun ClientPlayNetworkHandler?.findFirstPlayerListEntry(predicate: Predicate<PlayerListEntry>) =
    this?.playerList.orEmpty().firstOrNull(predicate)

fun PlayerEntity?.getColor(default: MeteorColor = MeteorColor.WHITE): MeteorColor = PlayerUtils.getPlayerColor(this, default)

infix fun ItemStack.eq(other: ItemStack) = ItemStack.areEqual(this, other)
infix fun ItemStack.neq(other: ItemStack) = !(this eq other)

val PlayerEntity.usableItemStack: ItemStack?
    get() = mainHandStack.takeUnless(ItemStack.EMPTY::eq) ?: offHandStack.takeUnless(ItemStack.EMPTY::eq)

val ItemStack.currentDurability
    get() = if (!isEmpty && isDamageable)
        maxDamage - damage
    else 0

fun WorldView.isAirAt(x: Int, y: Int, z: Int) = isAir(BlockPos(x, y ,z))

fun BlockPos.getStateInWorld(): Optional<BlockState> =
    minecraft.currentWorld().getBlockState(this)
        .optionalUnless { it.block eq Blocks.VOID_AIR }

fun BlockPos.getBlockInWorld(): Optional<Block> = getStateInWorld().map(BlockState::getBlock)
fun BlockPos.getBlockEntityInWorld(): Optional<BlockEntity> = optionalOf(minecraft.currentWorld().getBlockEntity(this))
fun<T : BlockEntity> BlockPos.getBlockEntityInWorldOfType(type: BlockEntityType<T>): Optional<T> =
    minecraft.currentWorld().getBlockEntity(this, type)

fun Vec3d.setX(x: Double) = set(x = x)
fun Vec3d.setY(y: Double) = cast<IVec3d>().setY(y)
fun Vec3d.setZ(z: Double) = set(z = z)

fun Vec3d.set(x: Double = this.x, y: Double = this.y, z: Double = this.z) = cast<IVec3d>().set(x, y, z)


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

fun DrawContext(bufferBuilder: BufferBuilder) = bufferBuilder.createDrawContext()
fun<T : VertexConsumerProvider.Immediate> DrawContext(vertexConsumers: T) = vertexConsumers.createDrawContext()

fun BufferBuilder.createDrawContext() = immediateVertexConsumerProvider().createDrawContext()
fun BufferBuilder.immediateVertexConsumerProvider(): VertexConsumerProvider.Immediate = VertexConsumerProvider.immediate(this)
fun <T : VertexConsumerProvider.Immediate> T.createDrawContext() = minecraft.createDrawContext(this)
fun <T : VertexConsumerProvider.Immediate> MinecraftClient.createDrawContext(vertexConsumers: T) = DrawContext(this, vertexConsumers)

fun MinecraftClient.rotationClient(): Vec2f = player().rotationClient
fun MinecraftClient.rotationVec(): Vec3d = player().rotationVector
fun MinecraftClient.clientRotationVec(): Vec3d = player().rotationVecClient

fun MinecraftClient.sendCommand(command: String) = network().sendCommand(command.replaceFirst("/", String.empty))


fun sendMeteorMessage(
    prefix: String? = null,
    prefixColor: ChatColor = ChatColor.lightPurple,
    builder: Initializer<FormattedText>
) =
    ChatUtils.sendMsg(
        0,
        prefix,
        prefixColor.mc,
        buildText(block = builder)
    )

fun MinecraftClient.showMessage(initial: Text = emptyText(), textBuilder: Initializer<FormattedText>) = player().sendMessage(buildText(initial, textBuilder))
fun MinecraftClient.showMessage(text: Text) = player().sendMessage(text)
fun MinecraftClient.showMessage(message: String) = showMessage(textOf(message))
fun MinecraftClient.showActionBar(text: Text) = player().sendMessage(text, true)
fun MinecraftClient.showActionBar(message: String) = showActionBar(textOf(message))
fun MinecraftClient.sendChatMessage(message: String) = network().sendChatMessage(message)
fun MinecraftClient.sendAsPlayer(message: String) {
    inGameHud.chatHud.addToMessageHistory(message)

    if (message.startsWith('/'))
        sendCommand(message)
    else
        sendChatMessage(message)
}

fun crash(message: String, cause: Throwable): Nothing {
    throw CrashException(CrashReport.create(cause, message))
}

val Throwable.hasSpecialHandling
    get() = this is CrashException || this is com.mojang.brigadier.exceptions.CommandSyntaxException

fun Throwable.rethrowSpecial() {
    if (hasSpecialHandling)
        throw this
}
