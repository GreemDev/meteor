/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("unused") // api

package net.greemdev.meteor.util.meteor

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
import net.minecraft.network.packet.Packet
import net.minecraft.particle.ParticleType
import net.minecraft.sound.SoundEvent
import net.minecraft.util.math.BlockPos
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

//collection settings
inline infix fun SettingGroup.stringList(
    crossinline func: Initializer<StringListSetting.Builder>
): SettingDelegate<StringListSetting.Builder, List<String>, StringListSetting> =
    StringListSetting.builder() configureBy func

inline infix fun SettingGroup.stringMap(
    crossinline func: Initializer<StringMapSetting.Builder>
): SettingDelegate<StringMapSetting.Builder, Map<String, String>, StringMapSetting> =
   StringMapSetting.builder() configureBy func

inline infix fun SettingGroup.blockList(
    crossinline func: Initializer<BlockListSetting.Builder>
): SettingDelegate<BlockListSetting.Builder, List<Block>, BlockListSetting> =
   BlockListSetting.builder() configureBy func

inline infix fun SettingGroup.colorList(
    crossinline func: Initializer<ColorListSetting.Builder>
): SettingDelegate<ColorListSetting.Builder, List<SettingColor>, ColorListSetting> =
   ColorListSetting.builder() configureBy func

inline infix fun SettingGroup.enchantList(
    crossinline func: Initializer<EnchantmentListSetting.Builder>
): SettingDelegate<EnchantmentListSetting.Builder, List<Enchantment>, EnchantmentListSetting> =
   EnchantmentListSetting.builder() configureBy func

inline infix fun SettingGroup.entityTypeList(
    crossinline func: Initializer<EntityTypeListSetting.Builder>
): SettingDelegate<EntityTypeListSetting.Builder, Set<EntityType<*>>, EntityTypeListSetting> =
   EntityTypeListSetting.builder() configureBy func

inline infix fun SettingGroup.itemList(
    crossinline func: Initializer<ItemListSetting.Builder>
): SettingDelegate<ItemListSetting.Builder, List<Item>, ItemListSetting> =
   ItemListSetting.builder() configureBy func

inline infix fun SettingGroup.moduleList(
    crossinline func: Initializer<ModuleListSetting.Builder>
): SettingDelegate<ModuleListSetting.Builder, List<Module>, ModuleListSetting> =
   ModuleListSetting.builder() configureBy func

inline infix fun SettingGroup.packetList(
    crossinline func: Initializer<PacketListSetting.Builder>
): SettingDelegate<PacketListSetting.Builder, Set<Class<out Packet<*>>>, PacketListSetting> =
   PacketListSetting.builder() configureBy func

inline infix fun SettingGroup.particleTypeList(
    crossinline func: Initializer<ParticleTypeListSetting.Builder>
): SettingDelegate<ParticleTypeListSetting.Builder, List<ParticleType<*>>, ParticleTypeListSetting> =
   ParticleTypeListSetting.builder() configureBy func

inline infix fun SettingGroup.soundEventList(
    crossinline func: Initializer<SoundEventListSetting.Builder>
): SettingDelegate<SoundEventListSetting.Builder, List<SoundEvent>, SoundEventListSetting> =
   SoundEventListSetting.builder() configureBy func

inline infix fun SettingGroup.statusEffectList(
    crossinline func: Initializer<StatusEffectListSetting.Builder>
): SettingDelegate<StatusEffectListSetting.Builder, List<StatusEffect>, StatusEffectListSetting> =
   StatusEffectListSetting.builder() configureBy func

inline infix fun SettingGroup.statusEffectAmpMap(
    crossinline func: Initializer<StatusEffectAmplifierMapSetting.Builder>
): SettingDelegate<StatusEffectAmplifierMapSetting.Builder, Object2IntMap<StatusEffect>, StatusEffectAmplifierMapSetting> =
   StatusEffectAmplifierMapSetting.builder() configureBy func

inline infix fun SettingGroup.storageBlockList(
    crossinline func: Initializer<StorageBlockListSetting.Builder>
): SettingDelegate<StorageBlockListSetting.Builder, List<BlockEntityType<*>>, StorageBlockListSetting> =
   StorageBlockListSetting.builder() configureBy func

//single settings
inline infix fun SettingGroup.blockPos(
    crossinline func: Initializer<BlockPosSetting.Builder>
): SettingDelegate<BlockPosSetting.Builder, BlockPos, BlockPosSetting> =
   BlockPosSetting.builder() configureBy func

inline infix fun SettingGroup.block(
    crossinline func: Initializer<BlockSetting.Builder>
): SettingDelegate<BlockSetting.Builder, Block, BlockSetting> =
   BlockSetting.builder() configureBy func

inline infix fun SettingGroup.bool(
    crossinline func: Initializer<BoolSetting.Builder>
): SettingDelegate<BoolSetting.Builder, Boolean, BoolSetting> =
   BoolSetting.builder() configureBy func

inline infix fun SettingGroup.color(
    crossinline func: Initializer<ColorSetting.Builder>
): SettingDelegate<ColorSetting.Builder, SettingColor, ColorSetting> =
   ColorSetting.builder() configureBy func

inline infix fun SettingGroup.double(
    crossinline func: Initializer<DoubleSetting.Builder>
): SettingDelegate<DoubleSetting.Builder, Double, DoubleSetting> =
   DoubleSetting.builder() configureBy func

inline infix fun <T : Enum<T>> SettingGroup.enum(
    crossinline func: Initializer<EnumSetting.Builder<T>>
): SettingDelegate<EnumSetting.Builder<T>, T, EnumSetting<T>> =
   EnumSetting.builder<T>() configureBy func

inline infix fun SettingGroup.fontFace(
    crossinline func: Initializer<FontFaceSetting.Builder>
): SettingDelegate<FontFaceSetting.Builder, FontFace, FontFaceSetting> =
   FontFaceSetting.builder() configureBy func

inline infix fun SettingGroup.int(
    crossinline func: Initializer<IntSetting.Builder>
): SettingDelegate<IntSetting.Builder, Int, IntSetting> =
   IntSetting.builder() configureBy func

inline infix fun SettingGroup.item(
    crossinline func: Initializer<ItemSetting.Builder>
): SettingDelegate<ItemSetting.Builder, Item, ItemSetting> =
   ItemSetting.builder() configureBy func

inline infix fun SettingGroup.keybind(
    crossinline func: Initializer<KeybindSetting.Builder>
): SettingDelegate<KeybindSetting.Builder, Keybind, KeybindSetting> =
   KeybindSetting.builder() configureBy func

inline infix fun SettingGroup.potion(
    crossinline func: Initializer<EnumSetting.Builder<PotionTypes>>
): SettingDelegate<EnumSetting.Builder<PotionTypes>, PotionTypes, EnumSetting<PotionTypes>> =
   PotionSetting.builder() configureBy func

inline infix fun SettingGroup.string(
    crossinline func: Initializer<StringSetting.Builder>
): SettingDelegate<StringSetting.Builder, String, StringSetting> =
   StringSetting.builder() configureBy func

inline infix fun SettingGroup.providedString(
    crossinline func: Initializer<ProvidedStringSetting.Builder>
): SettingDelegate<ProvidedStringSetting.Builder, String, ProvidedStringSetting> =
   ProvidedStringSetting.builderProvided() configureBy func



infix fun Settings.group(name: String) = group(name, true)

fun Settings.group(name: String? = null, expanded: Boolean = true): SettingGroup =
    name?.let { getGroup(it) ?: createGroup(it, expanded) } ?: group("General", expanded)

// utilities for the above Setting functions

// B = SettingBuilder type
// V = value type contained in Setting S
// S = concrete Setting type of type Setting<V>

context(SettingGroup)
inline infix fun <B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>> B.configureBy(
    crossinline func: Initializer<B>
) = createDelegate(this.apply(func))

fun<B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>> SettingGroup.createDelegate(
    builder: B
) = SettingDelegate(this, builder)

class SettingDelegate<
    B : Setting.SettingBuilder<B, V, S>,
    V : Any,
    S : Setting<V>
>(
    group: SettingGroup,
    builder: B,
    val setting: S = group.add(builder)
) : ReadOnlyProperty<Any?, S> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = setting
}
