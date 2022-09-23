/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.systems.System
import meteordevelopment.meteorclient.systems.Systems
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.util.getOrNull
import net.greemdev.meteor.util.misc.*
import net.greemdev.meteor.util.tryOrIgnore
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement

class HiddenModules : System<HiddenModules>("hidden-modules") {

    private val hiddenModules = hashSetOf<String>()

    init {
        init()
        load(MeteorClient.FOLDER)
    }

    operator fun contains(module: Module) = hiddenModules.contains(module.name)
    fun hideInCategory(category: Category, mapped: MutableMap<Category, MutableList<Module>>): List<Module> {
        val modules = mapped.computeIfAbsent(category) { mutableListOf() }
        if (hiddenModules.isNotEmpty())
            modules.removeAll { it.name in hiddenModules }

        return modules
    }

    companion object {
        @JvmStatic
        fun get(): HiddenModules = Systems.get(HiddenModules::class.java)

        @JvmStatic
        fun getOrNull(): HiddenModules? = getOrNull { get() }

        @JvmStatic
        fun getModules() = getOrNull {
            get().hiddenModules.mapNotNull { Meteor.modules().get(it) }
        } ?: emptyList()
    }

    fun set(modules: Collection<Module?>) {
        if (modules.size < hiddenModules.size)
            tryOrIgnore {
                showConfirm("hidden-modules-restart") {
                    title("Unhidden Modules")
                    message("In order to see the module you've unhidden, you will need to restart Minecraft.")
                }
            }

        hiddenModules.clear()
        modules.filterNotNull().forEach {
            if (it.isActive)
                it.toggle()
            hiddenModules.add(it.name)
        }
    }


    override fun toTag() = Nbt compound {
        put("hiddenModules", hiddenModules.toNBT())
    }

    override fun fromTag(tag: NbtCompound): HiddenModules {
        hiddenModules.clear()
        hiddenModules.addAll(tag.getList("hiddenModules", NbtDataType.String).map(NbtElement::asString))
        return this
    }

}
