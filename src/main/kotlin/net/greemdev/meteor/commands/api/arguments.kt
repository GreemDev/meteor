/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("unused")

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.arguments.*
import meteordevelopment.meteorclient.commands.Commands
import meteordevelopment.meteorclient.commands.arguments.*
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.commands.args.DirectionArgumentType
import net.greemdev.meteor.commands.args.PathArgumentType
import net.greemdev.meteor.util.decapitalize
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.*
import net.minecraft.predicate.NumberRange
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import java.util.function.Predicate

inline infix fun <reified T> MinecraftCommandContext.contextArg(name: String): Lazy<T> = lazy { getArgument(name, T::class.java) }

inline fun <reified T> MinecraftCommandContext.contextArg(type: ArgumentType<T>) = lazy { type.get(this, formatArgType(type)) }
inline fun <reified T> MinecraftCommandContext.contextArg(name: String, type: ArgumentType<T>) = lazy { type.get(this, name) }

inline fun<reified T> MinecraftCommandContext.contextArg(
    name: String,
    noinline parser: MinecraftCommandContext.(String) -> T
) =
    parser(name)

object Arguments {

    @JvmOverloads
    @JvmStatic fun blockPredicate(
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): BlockPredicateArgumentType =
        BlockPredicateArgumentType.blockPredicate(registryAccess)

    @JvmOverloads
    @JvmStatic fun blockState(
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): BlockStateArgumentType =
        BlockStateArgumentType.blockState(registryAccess)

    @JvmOverloads
    @JvmStatic fun itemPredicate(
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): ItemPredicateArgumentType =
        ItemPredicateArgumentType.itemPredicate(registryAccess)

    @JvmOverloads
    @JvmStatic fun itemStack(
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): ItemStackArgumentType =
        ItemStackArgumentType.itemStack(registryAccess)

    @JvmOverloads
    @JvmStatic fun particleEffect(
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): ParticleEffectArgumentType =
        ParticleEffectArgumentType.particleEffect(registryAccess)

    @JvmOverloads
    @JvmStatic fun<T> registryEntry(
        registryKey: RegistryKey<out Registry<T>>,
        registryAccess: CommandRegistryAccess = Commands.REGISTRY_ACCESS
    ): RegistryEntryArgumentType<T> =
        RegistryEntryArgumentType.registryEntry(registryAccess, registryKey)

    @JvmOverloads
    @JvmStatic fun path(
        allowDirectories: Boolean = true
    ) =
        PathArgumentType(allowDirectories)

    @JvmOverloads
    @JvmStatic fun double(
        min: Double = -Double.MAX_VALUE,
        max: Double = Double.MAX_VALUE
    ): DoubleArgumentType =
        DoubleArgumentType.doubleArg(min, max)

    @JvmOverloads
    @JvmStatic fun float(
        min: Float = -Float.MAX_VALUE,
        max: Float = Float.MAX_VALUE
    ): FloatArgumentType =
        FloatArgumentType.floatArg(min, max)

    @JvmOverloads
    @JvmStatic fun int(
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ): IntegerArgumentType =
        IntegerArgumentType.integer(min, max)

    @JvmOverloads
    @JvmStatic fun long(
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE
    ): LongArgumentType =
        LongArgumentType.longArg(min, max)

    @JvmOverloads
    @JvmStatic fun vec2(
        centerIntegers: Boolean = true
    ): Vec2ArgumentType =
        Vec2ArgumentType.vec2(centerIntegers)

    @JvmOverloads
    @JvmStatic fun vec3(
        centerIntegers: Boolean = true
    ): Vec3ArgumentType =
        Vec3ArgumentType.vec3(centerIntegers)

    @JvmOverloads
    @JvmStatic fun module(
        predicate: Predicate<Module>? = null
    ): ModuleArgumentType =
        ModuleArgumentType.create(predicate)

    @JvmStatic fun friend(): FriendArgumentType = FriendArgumentType.create()
    @JvmStatic fun player(): PlayerArgumentType = PlayerArgumentType.create()
    @JvmStatic fun playerListEntry(): PlayerListEntryArgumentType = PlayerListEntryArgumentType.create()
    @JvmStatic fun profile(): ProfileArgumentType = ProfileArgumentType.create()
    @JvmStatic fun setting(): SettingArgumentType = SettingArgumentType.create()
    @JvmStatic fun settingValue(): SettingValueArgumentType = SettingValueArgumentType.create()
    @JvmStatic fun waypoint(): WaypointArgumentType = WaypointArgumentType.create()
    @JvmStatic fun direction() = DirectionArgumentType()
    @JvmStatic fun boolean(): BoolArgumentType = BoolArgumentType.bool()
    @JvmStatic fun wordString(): StringArgumentType = StringArgumentType.word()
    @JvmStatic fun quotableString(): StringArgumentType = StringArgumentType.string()
    @JvmStatic fun greedyString(): StringArgumentType = StringArgumentType.greedyString()
    @JvmStatic fun blockPos(): BlockPosArgumentType = BlockPosArgumentType.blockPos()
    @JvmStatic fun columnPos(): ColumnPosArgumentType = ColumnPosArgumentType.columnPos()
    @JvmStatic fun rotation(): RotationArgumentType = RotationArgumentType.rotation()
    @JvmStatic fun blockRotation(): BlockRotationArgumentType = BlockRotationArgumentType.blockRotation()
    @JvmStatic fun swizzle(): SwizzleArgumentType = SwizzleArgumentType.swizzle()
    @JvmStatic fun commandFunction(): CommandFunctionArgumentType = CommandFunctionArgumentType.commandFunction()
    @JvmStatic fun itemSlot(): ItemSlotArgumentType = ItemSlotArgumentType.itemSlot()
    @JvmStatic fun angle(): AngleArgumentType = AngleArgumentType.angle()
    @JvmStatic fun color(): ColorArgumentType = ColorArgumentType.color()
    @JvmStatic fun text(): TextArgumentType = TextArgumentType.text()
    @JvmStatic fun dimension(): DimensionArgumentType = DimensionArgumentType.dimension()
    @JvmStatic fun entityAnchor(): EntityAnchorArgumentType = EntityAnchorArgumentType.entityAnchor()
    @JvmStatic fun entity(): EntityArgumentType = EntityArgumentType.entity()
    @JvmStatic fun entities(): EntityArgumentType = EntityArgumentType.entities()
    @JvmStatic fun playerEntity(): EntityArgumentType = EntityArgumentType.player()
    @JvmStatic fun playerEntities(): EntityArgumentType = EntityArgumentType.players()
    @JvmStatic fun identifier(): IdentifierArgumentType = IdentifierArgumentType.identifier()
    @JvmStatic fun gameProfile(): GameProfileArgumentType = GameProfileArgumentType.gameProfile()
    @JvmStatic fun message(): MessageArgumentType = MessageArgumentType.message()
    @JvmStatic fun nbtTag(): CompoundNbtTagArgumentType = CompoundNbtTagArgumentType.create()
    @JvmStatic fun nbtPath(): NbtPathArgumentType = NbtPathArgumentType.nbtPath()
    @JvmStatic fun scoreboardObjective(): ScoreboardObjectiveArgumentType = ScoreboardObjectiveArgumentType.scoreboardObjective()
    @JvmStatic fun scoreboardCriterion(): ScoreboardCriterionArgumentType = ScoreboardCriterionArgumentType.scoreboardCriterion()
    @JvmStatic fun operation(): OperationArgumentType = OperationArgumentType.operation()
    @JvmStatic fun intRange(): NumberRangeArgumentType<NumberRange.IntRange> = NumberRangeArgumentType.intRange()
    @JvmStatic fun doubleRange(): NumberRangeArgumentType<NumberRange.DoubleRange> = NumberRangeArgumentType.floatRange()
    @JvmStatic fun scoreboardSlot(): ScoreboardSlotArgumentType = ScoreboardSlotArgumentType.scoreboardSlot()
    @JvmStatic fun scoreHolder(): ScoreHolderArgumentType = ScoreHolderArgumentType.scoreHolder()
    @JvmStatic fun scoreHolders(): ScoreHolderArgumentType = ScoreHolderArgumentType.scoreHolders()
    @JvmStatic fun team(): TeamArgumentType = TeamArgumentType.team()
    @JvmStatic fun time(): TimeArgumentType = TimeArgumentType.time()
    @JvmStatic fun uuid(): UuidArgumentType = UuidArgumentType.uuid()
}

fun formatArgType(argType: ArgumentType<*>): String {
    val typeName = argType::class.simpleName?.dropLast("ArgumentType".length)
    require(typeName != null) { "Cannot use an anonymous class as an argument type." }
    require(typeName.isNotEmpty()) { "Invalid ArgumentType name." }
    return typeName.decapitalize()
}

inline fun<reified T> ArgumentType<T>.get(
    ctx: MinecraftCommandContext,
    name: String = formatArgType(this)
): T = get(name, ctx)

inline fun<reified T> get(
    name: String,
    ctx: MinecraftCommandContext
): T = ctx.getArgument(name, T::class.java)
