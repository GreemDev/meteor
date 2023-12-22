/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.element

import meteordevelopment.meteorclient.systems.hud.Alignment
import meteordevelopment.meteorclient.systems.hud.HudElement
import meteordevelopment.meteorclient.systems.hud.HudRenderer
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.hud.HudElementDescriptor
import net.greemdev.meteor.*
import net.greemdev.meteor.util.meteor.*
import kotlin.math.max

class ModuleKeybindHud : HudElement(info) {
    companion object : HudElementDescriptor<ModuleKeybindHud>(
        "module-keybinds",
        "Displays selected modules with valid keybinds.",
        ::ModuleKeybindHud
    )

    private val sg = settings.group()

    val modules by sg moduleList {
        name("modules")
        description("The modules to display the keybinds of.")
        defaultValue(emptyList())
    }

    val sortModules by sg bool {
        name("sort-modules")
        description("Sort the modules on the HUD.")
        defaultValue(true)
    }

    val ascendingOrder by sg bool {
        name("ascending-order")
        description("Sort the modules on the HUD in ascending order. Turn off for descending order.")
        defaultValue(false)
        visible(sortModules)
    }

    val textShadow by sg bool {
        name("text-shadow")
        description("Renders shadow behind text.")
        defaultValue(true)
    }

    val moduleColor by sg color {
        name("module-color")
        description("The module name color to display.")
        defaultValue(MeteorColor.WHITE)
    }

    val keybindColor by sg color {
        name("keybind-color")
        description("The module keybind color to display.")
        defaultValue(SettingColor(25, 25, 255))
    }

    val alignment by sg.enum<Alignment> {
        name("alignment")
        description("Horizontal alignment.")
        defaultValue(Alignment.Auto)
    }

    override fun render(renderer: HudRenderer) {
        if (modules().isEmpty()) {
            renderer.text("Module Keybinds", x.toDouble(), y.toDouble(), moduleColor(), textShadow())
            setSize(renderer.textWidth("Module Keybinds"), renderer.textHeight())
            return
        }

        var y = y.toDouble()

        var width = 0.0
        var height = 0.0
        modules().sorted(sortModules(), ascendingOrder()) {
            it.title.length + it.keybind.toString().length
        }.forEachIndexed { i, module ->
            val keybindName = module.keybind.toString()
            val moduleWidth = renderer.textWidth("${module.title} $keybindName")

            var x = this.x + alignX(moduleWidth, alignment())
            x = renderer.text(module.title, x, y, moduleColor(), textShadow())
            renderer.text(keybindName, x + renderer.textWidth(" "), y, keybindColor(), textShadow())
            y += renderer.textHeight() + 2

            width = max(width, moduleWidth)
            height += renderer.textHeight()
            if (i > 0)
                height += 2
        }

        setSize(width, height)
    }
}

