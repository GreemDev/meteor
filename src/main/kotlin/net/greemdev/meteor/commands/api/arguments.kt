/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:JvmName("Args")
@file:Suppress("unused")

package net.greemdev.meteor.commands.api

import com.mojang.brigadier.arguments.*
import meteordevelopment.meteorclient.commands.arguments.*
import net.greemdev.meteor.commands.args.DirectionArgumentType
import net.greemdev.meteor.commands.args.PathArgumentType
import net.minecraft.command.CommandRegistryAccess
import net.minecraft.command.argument.*
import net.minecraft.predicate.NumberRange

inline infix fun <reified T> MinecraftCommandContext.argument(name: String): Lazy<T> = lazy { getArgument(name, T::class.java) }

inline fun <reified T> MinecraftCommandContext.argument(type: ArgumentType<T>, name: String = formatArgType(type)) = lazy { type.get(this, name) }

inline fun<reified T> MinecraftCommandContext.argument(
    name: String,
    noinline parser: MinecraftCommandContext.(String) -> T
) =
    parser(name)

object Arguments {
    fun module(): ModuleArgumentType = ModuleArgumentType.create()
    fun friend(): FriendArgumentType = FriendArgumentType.create()
    fun player(): PlayerArgumentType = PlayerArgumentType.create()
    fun playerListEntry(): PlayerListEntryArgumentType = PlayerListEntryArgumentType.create()
    fun profile(): ProfileArgumentType = ProfileArgumentType.create()
    fun setting(): SettingArgumentType = SettingArgumentType.create()
    fun settingValue(): SettingValueArgumentType = SettingValueArgumentType.create()
    fun waypoint(): WaypointArgumentType = WaypointArgumentType.create()
    fun direction() = DirectionArgumentType.create()

    @JvmOverloads fun path(
        baseStringType: ArgumentType<String> = greedyString(),
        allowDirectories: Boolean = true
    ) =
        PathArgumentType.create(baseStringType, allowDirectories)

    @JvmOverloads fun double(
        min: Double = -Double.MAX_VALUE,
        max: Double = Double.MAX_VALUE
    ): DoubleArgumentType =
        DoubleArgumentType.doubleArg(min, max)

    @JvmOverloads fun float(
        min: Float = -Float.MAX_VALUE,
        max: Float = Float.MAX_VALUE
    ): FloatArgumentType =
        FloatArgumentType.floatArg(min, max)

    @JvmOverloads fun int(
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ): IntegerArgumentType =
        IntegerArgumentType.integer(min, max)

    @JvmOverloads fun long(
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE
    ): LongArgumentType =
        LongArgumentType.longArg(min, max)

    @JvmOverloads
    fun vec2(
        centerIntegers: Boolean = true
    ): Vec2ArgumentType =
        Vec2ArgumentType.vec2(centerIntegers)

    @JvmOverloads
    fun vec3(
        centerIntegers: Boolean = true
    ): Vec3ArgumentType =
        Vec3ArgumentType.vec3(centerIntegers)

    fun boolean(): BoolArgumentType = BoolArgumentType.bool()
    fun wordString(): StringArgumentType = StringArgumentType.word()
    fun quotableString(): StringArgumentType = StringArgumentType.string()
    fun greedyString(): StringArgumentType = StringArgumentType.greedyString()
    fun blockPredicate(registryAccess: CommandRegistryAccess): BlockPredicateArgumentType = BlockPredicateArgumentType.blockPredicate(registryAccess)
    fun blockState(registryAccess: CommandRegistryAccess): BlockStateArgumentType = BlockStateArgumentType.blockState(registryAccess)
    fun blockPos(): BlockPosArgumentType = BlockPosArgumentType.blockPos()
    fun columnPos(): ColumnPosArgumentType = ColumnPosArgumentType.columnPos()
    fun rotation(): RotationArgumentType = RotationArgumentType.rotation()
    fun blockRotation(): BlockRotationArgumentType = BlockRotationArgumentType.blockRotation()
    fun swizzle(): SwizzleArgumentType = SwizzleArgumentType.swizzle()
    fun commandFunction(): CommandFunctionArgumentType = CommandFunctionArgumentType.commandFunction()
    fun testFunction(): TestFunctionArgumentType = TestFunctionArgumentType.testFunction()
    fun itemPredicate(registryAccess: CommandRegistryAccess): ItemPredicateArgumentType = ItemPredicateArgumentType.itemPredicate(registryAccess)
    fun itemStack(registryAccess: CommandRegistryAccess): ItemStackArgumentType = ItemStackArgumentType.itemStack(registryAccess)
    fun itemSlot(): ItemSlotArgumentType = ItemSlotArgumentType.itemSlot()
    fun angle(): AngleArgumentType = AngleArgumentType.angle()
    fun color(): ColorArgumentType = ColorArgumentType.color()
    fun text(): TextArgumentType = TextArgumentType.text()
    fun dimension(): DimensionArgumentType = DimensionArgumentType.dimension()
    fun entityAnchor(): EntityAnchorArgumentType = EntityAnchorArgumentType.entityAnchor()
    fun entity(): EntityArgumentType = EntityArgumentType.entity()
    fun entities(): EntityArgumentType = EntityArgumentType.entities()
    fun playerEntity(): EntityArgumentType = EntityArgumentType.player()
    fun playerEntities(): EntityArgumentType = EntityArgumentType.players()
    fun identifier(): IdentifierArgumentType = IdentifierArgumentType.identifier()
    fun gameProfile(): GameProfileArgumentType = GameProfileArgumentType.gameProfile()
    fun message(): MessageArgumentType = MessageArgumentType.message()
    fun nbtTag(): CompoundNbtTagArgumentType = CompoundNbtTagArgumentType.create()
    fun nbtPath(): NbtPathArgumentType = NbtPathArgumentType.nbtPath()
    fun objective(): ScoreboardObjectiveArgumentType = ScoreboardObjectiveArgumentType.scoreboardObjective()
    fun objectiveCriteria(): ScoreboardCriterionArgumentType = ScoreboardCriterionArgumentType.scoreboardCriterion()
    fun operation(): OperationArgumentType = OperationArgumentType.operation()
    fun particleEffect(registryAccess: CommandRegistryAccess): ParticleEffectArgumentType = ParticleEffectArgumentType.particleEffect(registryAccess)
    fun intRange(): NumberRangeArgumentType<NumberRange.IntRange> = NumberRangeArgumentType.intRange()
    fun floatRange(): NumberRangeArgumentType<NumberRange.FloatRange> = NumberRangeArgumentType.floatRange()
    fun scoreboardSlot(): ScoreboardSlotArgumentType = ScoreboardSlotArgumentType.scoreboardSlot()
    fun scoreHolder(): ScoreHolderArgumentType = ScoreHolderArgumentType.scoreHolder()
    fun scoreHolders(): ScoreHolderArgumentType = ScoreHolderArgumentType.scoreHolders()
    fun team(): TeamArgumentType = TeamArgumentType.team()
    fun time(): TimeArgumentType = TimeArgumentType.time()
    fun uuid(): UuidArgumentType = UuidArgumentType.uuid()
}

fun formatArgType(argType: ArgumentType<*>) = buildString {
    val typeName = argType::class.simpleName?.dropLast("ArgumentType".length)
    require(!typeName.isNullOrEmpty()) { "Invalid ArgumentType name." }
    append(typeName.replaceFirstChar(Char::lowercase))
}

inline fun<reified T> ArgumentType<T>.get(ctx: MinecraftCommandContext, name: String = formatArgType(this)): T = ctx.getArgument(name, T::class.java)


