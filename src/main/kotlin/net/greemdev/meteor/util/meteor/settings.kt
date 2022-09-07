/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.settings.*
import meteordevelopment.meteorclient.utils.misc.MyPotion
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

operator fun<T : Any> Setting<T>.invoke(): T = get()

fun Settings.group(name: String? = null, expanded: Boolean = true): SettingGroup =
    name?.let { getGroup(it) ?: createGroup(it, expanded) } ?: group("General", expanded)

inline fun <ST : Setting<S>, S : Any, B : Setting.SettingBuilder<B, S, ST>>
    SettingGroup.new(builder: B, crossinline func: B.() -> Unit) =
    SettingDelegate(this, builder.apply(func))

class SettingDelegate<ST : Setting<S>, S : Any, B : Setting.SettingBuilder<B, S, ST>>
    (group: SettingGroup, builder: B) : ReadOnlyProperty<Any?, ST> {

    val s: Setting<S> = group.add(builder.build() as Setting<S>)

    // result of builder#build is always of type ST, and ST is a subtype of Setting<S>.
    // the result is upcast to the superclass for storing in the delegate.
    @Suppress("UNCHECKED_CAST")
    fun get()
        = s as? ST ?: error("setting value is of type ${s.javaClass.simpleName}, and not required type")


    override fun getValue(thisRef: Any?, property: KProperty<*>) = get()
}

//collection settings
infix fun SettingGroup.stringList(func: StringListSetting.Builder.() -> Unit) = new(StringListSetting.Builder(), func)
infix fun SettingGroup.blockList(func: BlockListSetting.Builder.() -> Unit) = new(BlockListSetting.Builder(), func)
infix fun SettingGroup.colorList(func: ColorListSetting.Builder.() -> Unit) = new(ColorListSetting.Builder(), func)
infix fun SettingGroup.enchantList(func: EnchantmentListSetting.Builder.() -> Unit) = new(EnchantmentListSetting.Builder(), func)
infix fun SettingGroup.entityTypeList(func: EntityTypeListSetting.Builder.() -> Unit) = new(EntityTypeListSetting.Builder(), func)
infix fun SettingGroup.itemList(func: ItemListSetting.Builder.() -> Unit) = new(ItemListSetting.Builder(), func)
infix fun SettingGroup.moduleList(func: ModuleListSetting.Builder.() -> Unit) = new(ModuleListSetting.Builder(), func)
infix fun SettingGroup.packetList(func: PacketListSetting.Builder.() -> Unit) = new(PacketListSetting.Builder(), func)
infix fun SettingGroup.particleTypeList(func: ParticleTypeListSetting.Builder.() -> Unit) = new(ParticleTypeListSetting.Builder(), func)
infix fun SettingGroup.soundList(func: SoundEventListSetting.Builder.() -> Unit) = new(SoundEventListSetting.Builder(), func)
infix fun SettingGroup.statusEffectList(func: StatusEffectListSetting.Builder.() -> Unit) = new(StatusEffectListSetting.Builder(), func)
infix fun SettingGroup.statusEffectAmpMap(func: StatusEffectAmplifierMapSetting.Builder.() -> Unit) = new(StatusEffectAmplifierMapSetting.Builder(), func)
infix fun SettingGroup.storageBlockList(func: StorageBlockListSetting.Builder.() -> Unit) = new(StorageBlockListSetting.Builder(), func)

//single settings
infix fun SettingGroup.blockPos(func: BlockPosSetting.Builder.() -> Unit) = new(BlockPosSetting.Builder(), func)
infix fun SettingGroup.block(func: BlockSetting.Builder.() -> Unit) = new(BlockSetting.Builder(), func)
infix fun SettingGroup.bool(func: BoolSetting.Builder.() -> Unit) = new(BoolSetting.Builder(), func)
infix fun SettingGroup.color(func: ColorSetting.Builder.() -> Unit) = new(ColorSetting.Builder(), func)
infix fun SettingGroup.double(func: DoubleSetting.Builder.() -> Unit) = new(DoubleSetting.Builder(), func)
infix fun<T : Enum<T>> SettingGroup.enum(func: EnumSetting.Builder<T>.() -> Unit) = new(EnumSetting.Builder(), func)
infix fun SettingGroup.font(func: FontFaceSetting.Builder.() -> Unit) = new(FontFaceSetting.Builder(), func)
infix fun SettingGroup.int(func: IntSetting.Builder.() -> Unit) = new(IntSetting.Builder(), func)
infix fun SettingGroup.item(func: ItemSetting.Builder.() -> Unit) = new(ItemSetting.Builder(), func)
infix fun SettingGroup.keybind(func: KeybindSetting.Builder.() -> Unit) = new(KeybindSetting.Builder(), func)
infix fun SettingGroup.potion(func: EnumSetting.Builder<MyPotion>.() -> Unit) = new(PotionSetting.Builder(), func)
infix fun SettingGroup.string(func: StringSetting.Builder.() -> Unit) = new(StringSetting.Builder(), func)
infix fun SettingGroup.providedString(func: ProvidedStringSetting.Builder.() -> Unit) = new(ProvidedStringSetting.Builder(), func)
