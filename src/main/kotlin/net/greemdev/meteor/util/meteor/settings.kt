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

infix fun Settings.group(name: String) = group(name, true)

fun Settings.group(name: String? = null, expanded: Boolean = true): SettingGroup =
    name?.let { getGroup(it) ?: createGroup(it, expanded) } ?: group("General", expanded)

context(SettingGroup)
inline infix fun <B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>> B.configureBy(
    crossinline func: Initializer<B>
) = createDelegate(this.apply(func))

fun<B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>> SettingGroup.createDelegate(builder: B) =
    SettingDelegate(this, builder)

class SettingDelegate<B : Setting.SettingBuilder<B, V, S>, V : Any, S : Setting<V>>(
    group: SettingGroup,
    builder: B,
    val setting: S = group.add(builder)
) : ReadOnlyProperty<Any?, S> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = setting
}

//collection settings
inline infix fun SettingGroup.stringList(
    crossinline func: StringListSetting.Builder.() -> Unit
): SettingDelegate<StringListSetting.Builder, List<String>, StringListSetting> =
    StringListSetting.builder() configureBy func

inline infix fun SettingGroup.stringMap(
    crossinline func: StringMapSetting.Builder.() -> Unit
): SettingDelegate<StringMapSetting.Builder, Map<String, String>, StringMapSetting> =
   StringMapSetting.builder() configureBy func

inline infix fun SettingGroup.blockList(
    crossinline func: BlockListSetting.Builder.() -> Unit
): SettingDelegate<BlockListSetting.Builder, List<Block>, BlockListSetting> =
   BlockListSetting.builder() configureBy func

inline infix fun SettingGroup.colorList(
    crossinline func: ColorListSetting.Builder.() -> Unit
): SettingDelegate<ColorListSetting.Builder, List<SettingColor>, ColorListSetting> =
   ColorListSetting.builder() configureBy func

inline infix fun SettingGroup.enchantList(
    crossinline func: EnchantmentListSetting.Builder.() -> Unit
): SettingDelegate<EnchantmentListSetting.Builder, List<Enchantment>, EnchantmentListSetting> =
   EnchantmentListSetting.builder() configureBy func

inline infix fun SettingGroup.entityTypeList(
    crossinline func: EntityTypeListSetting.Builder.() -> Unit
): SettingDelegate<EntityTypeListSetting.Builder, Set<EntityType<*>>, EntityTypeListSetting> =
   EntityTypeListSetting.builder() configureBy func

inline infix fun SettingGroup.itemList(
    crossinline func: ItemListSetting.Builder.() -> Unit
): SettingDelegate<ItemListSetting.Builder, List<Item>, ItemListSetting> =
   ItemListSetting.builder() configureBy func

inline infix fun SettingGroup.moduleList(
    crossinline func: ModuleListSetting.Builder.() -> Unit
): SettingDelegate<ModuleListSetting.Builder, List<Module>, ModuleListSetting> =
   ModuleListSetting.builder() configureBy func

inline infix fun SettingGroup.packetList(
    crossinline func: PacketListSetting.Builder.() -> Unit
): SettingDelegate<PacketListSetting.Builder, Set<Class<out Packet<*>>>, PacketListSetting> =
   PacketListSetting.builder() configureBy func

inline infix fun SettingGroup.particleTypeList(
    crossinline func: ParticleTypeListSetting.Builder.() -> Unit
): SettingDelegate<ParticleTypeListSetting.Builder, List<ParticleType<*>>, ParticleTypeListSetting> =
   ParticleTypeListSetting.builder() configureBy func

inline infix fun SettingGroup.soundList(
    crossinline func: SoundEventListSetting.Builder.() -> Unit
): SettingDelegate<SoundEventListSetting.Builder, List<SoundEvent>, SoundEventListSetting> =
   SoundEventListSetting.builder() configureBy func

inline infix fun SettingGroup.statusEffectList(
    crossinline func: StatusEffectListSetting.Builder.() -> Unit
): SettingDelegate<StatusEffectListSetting.Builder, List<StatusEffect>, StatusEffectListSetting> =
   StatusEffectListSetting.builder() configureBy func

inline infix fun SettingGroup.statusEffectAmpMap(
    crossinline func: StatusEffectAmplifierMapSetting.Builder.() -> Unit
): SettingDelegate<StatusEffectAmplifierMapSetting.Builder, Object2IntMap<StatusEffect>, StatusEffectAmplifierMapSetting> =
   StatusEffectAmplifierMapSetting.builder() configureBy func

inline infix fun SettingGroup.storageBlockList(
    crossinline func: StorageBlockListSetting.Builder.() -> Unit
): SettingDelegate<StorageBlockListSetting.Builder, List<BlockEntityType<*>>, StorageBlockListSetting> =
   StorageBlockListSetting.builder() configureBy func

//single settings
inline infix fun SettingGroup.blockPos(
    crossinline func: BlockPosSetting.Builder.() -> Unit
): SettingDelegate<BlockPosSetting.Builder, BlockPos, BlockPosSetting> =
   BlockPosSetting.builder() configureBy func

inline infix fun SettingGroup.block(
    crossinline func: BlockSetting.Builder.() -> Unit
): SettingDelegate<BlockSetting.Builder, Block, BlockSetting> =
   BlockSetting.builder() configureBy func

inline infix fun SettingGroup.bool(
    crossinline func: BoolSetting.Builder.() -> Unit
): SettingDelegate<BoolSetting.Builder, Boolean, BoolSetting> =
   BoolSetting.builder() configureBy func

inline infix fun SettingGroup.color(
    crossinline func: ColorSetting.Builder.() -> Unit
): SettingDelegate<ColorSetting.Builder, SettingColor, ColorSetting> =
   ColorSetting.builder() configureBy func

inline infix fun SettingGroup.double(
    crossinline func: DoubleSetting.Builder.() -> Unit
): SettingDelegate<DoubleSetting.Builder, Double, DoubleSetting> =
   DoubleSetting.builder() configureBy func

inline infix fun <T : Enum<T>> SettingGroup.enum(
    crossinline func: EnumSetting.Builder<T>.() -> Unit
): SettingDelegate<EnumSetting.Builder<T>, T, EnumSetting<T>> =
   EnumSetting.builder<T>() configureBy func

inline infix fun SettingGroup.font(
    crossinline func: FontFaceSetting.Builder.() -> Unit
): SettingDelegate<FontFaceSetting.Builder, FontFace, FontFaceSetting> =
   FontFaceSetting.builder() configureBy func

inline infix fun SettingGroup.int(
    crossinline func: IntSetting.Builder.() -> Unit
): SettingDelegate<IntSetting.Builder, Int, IntSetting> =
   IntSetting.builder() configureBy func

inline infix fun SettingGroup.item(
    crossinline func: ItemSetting.Builder.() -> Unit
): SettingDelegate<ItemSetting.Builder, Item, ItemSetting> =
   ItemSetting.builder() configureBy func

inline infix fun SettingGroup.keybind(
    crossinline func: KeybindSetting.Builder.() -> Unit
): SettingDelegate<KeybindSetting.Builder, Keybind, KeybindSetting> =
   KeybindSetting.builder() configureBy func

inline infix fun SettingGroup.potion(
    crossinline func: EnumSetting.Builder<PotionTypes>.() -> Unit
): SettingDelegate<EnumSetting.Builder<PotionTypes>, PotionTypes, EnumSetting<PotionTypes>> =
   PotionSetting.builder() configureBy func

inline infix fun SettingGroup.string(
    crossinline func: StringSetting.Builder.() -> Unit
): SettingDelegate<StringSetting.Builder, String, StringSetting> =
   StringSetting.builder() configureBy func

inline infix fun SettingGroup.providedString(
    crossinline func: ProvidedStringSetting.Builder.() -> Unit
): SettingDelegate<ProvidedStringSetting.Builder, String, ProvidedStringSetting> =
   ProvidedStringSetting.builderProvided() configureBy func
