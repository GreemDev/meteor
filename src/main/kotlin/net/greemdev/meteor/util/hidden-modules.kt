/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.systems.modules.Category
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt
import java.util.function.Function

abstract class HiddenModules : HashSet<String>() {

    operator fun contains(module: Module) = contains(module.name)
    companion object : HiddenModules() {

        @JvmStatic
        fun hideInCategory(category: Category, mapped: MutableMap<Category, MutableList<Module>>): List<Module> {
            val modules = mapped.computeIfAbsent(category) { mutableListOf() }
            if (this.isNotEmpty())
                modules.removeAll { it in this }

            return modules
        }

        @JvmStatic
        fun get() = this

        @JvmStatic
        fun set(modules: Collection<Module?>) {
            if (modules.size < size)
                confirm("hidden-modules-restart") {
                    title("Unhidden Modules")
                    message("In order to see the modules you've unhidden, you will need to restart Minecraft.")
                }.show()

            clear()
            modules.filterNotNull().forEach {
                if (it.isActive)
                    it.toggle()
                add(it.name)
            }
        }
    }
}
