/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.systems.config.Config
import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.util.getOrNull
import net.greemdev.meteor.util.tryOrIgnore

object HiddenModules {

    operator fun contains(module: Module) = Config.hiddenModuleNames.contains(module.name)
    @JvmStatic
    fun hideInCategory(category: Category, mapped: MutableMap<Category, MutableList<Module>>): List<Module> {
        val modules = mapped.computeIfAbsent(category) { mutableListOf() }
        return if (Config.hiddenModuleNames.isNotEmpty())
            modules.filter { it !in this }
        else
            modules
    }

    @JvmStatic
    fun getModules() =
        Config.hiddenModuleNames.mapNotNull {
            getOrNull { Meteor.modules().get(it) }
        }


    @JvmStatic
    fun set(modules: Collection<Module?>) {
        Config.hiddenModuleNames.clear()
        modules.filterNotNull().forEach {
            if (it.isActive)
                it.toggle()
            Config.hiddenModuleNames.add(it.name)
        }
    }
}
