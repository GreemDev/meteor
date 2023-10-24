/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("unused") // api

package net.greemdev.meteor.util.meteor

import it.unimi.dsi.fastutil.objects.Object2BooleanMap
import it.unimi.dsi.fastutil.objects.Object2IntMap
import meteordevelopment.meteorclient.renderer.text.FontFace
import meteordevelopment.meteorclient.settings.*
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.utils.misc.Keybind
import meteordevelopment.meteorclient.utils.misc.PotionTypes
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.Initializer
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.EntityType
import net.minecraft.entity.effect.StatusEffect
import net.minecraft.item.Item
import net.minecraft.network.Packet
import net.minecraft.particle.ParticleType
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

operator fun <T> Supplier<T>.invoke(): T = get()
infix fun Settings.group(name: String) = group(name, true)

fun Settings.group(name: String? = null, expanded: Boolean = true): SettingGroup =
    name?.let { getGroup(it) ?: createGroup(it, expanded) } ?: group("General", expanded)

inline fun <B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>> SettingGroup.new(
    builder: B,
    crossinline func: Initializer<B>
) =
    SettingDelegate(this, builder.apply(func))

class SettingDelegate<B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>>(
    group: SettingGroup,
    builder: B
) : ReadOnlyProperty<Any?, S> {

    @Suppress("UNCHECKED_CAST")
    val setting = group.add(builder) as S

    override fun getValue(thisRef: Any?, property: KProperty<*>) = setting
}

//collection settings
inline infix fun SettingGroup.stringList(
    crossinline func: StringListSetting.Builder.() -> Unit
): SettingDelegate<StringListSetting.Builder, List<String>, StringListSetting> =
    new(StringListSetting.Builder(), func)

inline infix fun SettingGroup.stringMap(
    crossinline func: StringMapSetting.Builder.() -> Unit
): SettingDelegate<StringMapSetting.Builder, Map<String, String>, StringMapSetting> =
    new(StringMapSetting.Builder(), func)

inline infix fun SettingGroup.blockList(
    crossinline func: BlockListSetting.Builder.() -> Unit
): SettingDelegate<BlockListSetting.Builder, List<Block>, BlockListSetting> =
    new(BlockListSetting.Builder(), func)

inline infix fun SettingGroup.colorList(
    crossinline func: ColorListSetting.Builder.() -> Unit
): SettingDelegate<ColorListSetting.Builder, List<SettingColor>, ColorListSetting> =
    new(ColorListSetting.Builder(), func)

inline infix fun SettingGroup.enchantList(
    crossinline func: EnchantmentListSetting.Builder.() -> Unit
): SettingDelegate<EnchantmentListSetting.Builder, List<Enchantment>, EnchantmentListSetting> =
    new(EnchantmentListSetting.Builder(), func)

inline infix fun SettingGroup.entityTypeList(
    crossinline func: EntityTypeListSetting.Builder.() -> Unit
): SettingDelegate<EntityTypeListSetting.Builder, Object2BooleanMap<EntityType<*>>, EntityTypeListSetting> =
    new(EntityTypeListSetting.Builder(), func)

inline infix fun SettingGroup.itemList(
    crossinline func: ItemListSetting.Builder.() -> Unit
): SettingDelegate<ItemListSetting.Builder, List<Item>, ItemListSetting> =
    new(ItemListSetting.Builder(), func)

inline infix fun SettingGroup.moduleList(
    crossinline func: ModuleListSetting.Builder.() -> Unit
): SettingDelegate<ModuleListSetting.Builder, List<Module>, ModuleListSetting> =
    new(ModuleListSetting.Builder(), func)

inline infix fun SettingGroup.packetList(
    crossinline func: PacketListSetting.Builder.() -> Unit
): SettingDelegate<PacketListSetting.Builder, Set<Class<out Packet<*>>>, PacketListSetting> =
    new(PacketListSetting.Builder(), func)

inline infix fun SettingGroup.particleTypeList(
    crossinline func: ParticleTypeListSetting.Builder.() -> Unit
): SettingDelegate<ParticleTypeListSetting.Builder, List<ParticleType<*>>, ParticleTypeListSetting> =
    new(ParticleTypeListSetting.Builder(), func)

inline infix fun SettingGroup.soundList(
    crossinline func: SoundEventListSetting.Builder.() -> Unit
): SettingDelegate<SoundEventListSetting.Builder, List<SoundEvent>, SoundEventListSetting> =
    new(SoundEventListSetting.Builder(), func)

inline infix fun SettingGroup.statusEffectList(
    crossinline func: StatusEffectListSetting.Builder.() -> Unit
): SettingDelegate<StatusEffectListSetting.Builder, List<StatusEffect>, StatusEffectListSetting> =
    new(StatusEffectListSetting.Builder(), func)

inline infix fun SettingGroup.statusEffectAmpMap(
    crossinline func: StatusEffectAmplifierMapSetting.Builder.() -> Unit
): SettingDelegate<StatusEffectAmplifierMapSetting.Builder, Object2IntMap<StatusEffect>, StatusEffectAmplifierMapSetting> =
    new(StatusEffectAmplifierMapSetting.Builder(), func)

inline infix fun SettingGroup.storageBlockList(
    crossinline func: StorageBlockListSetting.Builder.() -> Unit
): SettingDelegate<StorageBlockListSetting.Builder, List<BlockEntityType<*>>, StorageBlockListSetting> =
    new(StorageBlockListSetting.Builder(), func)

//single settings
inline infix fun SettingGroup.blockPos(
    crossinline func: BlockPosSetting.Builder.() -> Unit
): SettingDelegate<BlockPosSetting.Builder, BlockPos, BlockPosSetting> =
    new(BlockPosSetting.Builder(), func)

inline infix fun SettingGroup.block(
    crossinline func: BlockSetting.Builder.() -> Unit
): SettingDelegate<BlockSetting.Builder, Block, BlockSetting> =
    new(BlockSetting.Builder(), func)

inline infix fun SettingGroup.bool(
    crossinline func: BoolSetting.Builder.() -> Unit
): SettingDelegate<BoolSetting.Builder, Boolean, BoolSetting> =
    new(BoolSetting.Builder(), func)

inline infix fun SettingGroup.color(
    crossinline func: ColorSetting.Builder.() -> Unit
): SettingDelegate<ColorSetting.Builder, SettingColor, ColorSetting> =
    new(ColorSetting.Builder(), func)

inline infix fun SettingGroup.double(
    crossinline func: DoubleSetting.Builder.() -> Unit
): SettingDelegate<DoubleSetting.Builder, Double, DoubleSetting> =
    new(DoubleSetting.Builder(), func)

inline infix fun <T : Enum<T>> SettingGroup.enum(
    crossinline func: EnumSetting.Builder<T>.() -> Unit
): SettingDelegate<EnumSetting.Builder<T>, T, EnumSetting<T>> =
    new(EnumSetting.Builder(), func)

inline infix fun SettingGroup.font(
    crossinline func: FontFaceSetting.Builder.() -> Unit
): SettingDelegate<FontFaceSetting.Builder, FontFace, FontFaceSetting> =
    new(FontFaceSetting.Builder(), func)

inline infix fun SettingGroup.int(
    crossinline func: IntSetting.Builder.() -> Unit
): SettingDelegate<IntSetting.Builder, Int, IntSetting> =
    new(IntSetting.Builder(), func)

inline infix fun SettingGroup.item(
    crossinline func: ItemSetting.Builder.() -> Unit
): SettingDelegate<ItemSetting.Builder, Item, ItemSetting> =
    new(ItemSetting.Builder(), func)

inline infix fun SettingGroup.keybind(
    crossinline func: KeybindSetting.Builder.() -> Unit
): SettingDelegate<KeybindSetting.Builder, Keybind, KeybindSetting> =
    new(KeybindSetting.Builder(), func)

inline infix fun SettingGroup.potion(
    crossinline func: EnumSetting.Builder<PotionTypes>.() -> Unit
): SettingDelegate<EnumSetting.Builder<PotionTypes>, PotionTypes, EnumSetting<PotionTypes>> =
    new(PotionSetting.Builder(), func)

inline infix fun SettingGroup.string(
    crossinline func: StringSetting.Builder.() -> Unit
): SettingDelegate<StringSetting.Builder, String, StringSetting> =
    new(StringSetting.Builder(), func)

inline infix fun SettingGroup.providedString(
    crossinline func: ProvidedStringSetting.Builder.() -> Unit
): SettingDelegate<ProvidedStringSetting.Builder, String, ProvidedStringSetting> =
    new(ProvidedStringSetting.Builder(), func)
