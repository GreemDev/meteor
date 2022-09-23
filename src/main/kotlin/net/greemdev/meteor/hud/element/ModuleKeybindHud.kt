/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.element

import meteordevelopment.meteorclient.systems.hud.Alignment
import meteordevelopment.meteorclient.systems.hud.HudElement
import meteordevelopment.meteorclient.systems.hud.HudRenderer
import meteordevelopment.meteorclient.systems.modules.Modules
import meteordevelopment.meteorclient.utils.render.color.RainbowColors
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.Greteor
import net.greemdev.meteor.hud.HudElementMetadata
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import kotlin.math.max

class ModuleKeybindHud : HudElement(Companion.info) {
    companion object : HudElementMetadata<ModuleKeybindHud>(
        Greteor.hudGroup(),
        "module-keybinds", "Displays selected modules with valid keybinds.",
        ::ModuleKeybindHud
    )

    private val sg = settings.group()

    val modules by sg moduleList {
        name("modules")
        description("The modules to display the keybinds of.")
        filteredBy { it.keybind.isSet }
    }

    val sorted by sg bool {
        name("sort-modules")
        description("Sort the modules on the HUD.")
        defaultValue(true)
    }

    val sortOrder by sg bool {
        name("ascending-order")
        description("Sort the modules on the HUD in ascending order. Turn off for descending order.")
        defaultValue(false)
        visible(sorted::get)
    }

    val textShadow by sg bool {
        name("text-shadow")
        description("Renders shadow behind text.")
        defaultValue(true)
    }

    val moduleColor by sg color {
        name("module-color")
        description("The module name color to display.")
        defaultValue(SettingColor())
        onChanged(RainbowColors::handle)
    }

    val keybindColor by sg color {
        name("keybind-color")
        description("The module keybind color to display.")
        defaultValue(SettingColor(25, 25, 255))
        onChanged(RainbowColors::handle)
    }

    val alignment by sg.enum<Alignment> {
        name("alignment")
        description("Horizontal alignment.")
        defaultValue(Alignment.Auto)
    }

    override fun render(renderer: HudRenderer) {
        if (Modules.get() == null || modules().isEmpty()) {
            renderer.text("Module Keybinds", x.toDouble(), y.toDouble(), moduleColor(), textShadow())
            setSize(renderer.textWidth("Module Keybinds"), renderer.textHeight())
            return
        }

        var y = y.toDouble()

        var width = 0.0
        var height = 0.0
        modules().sorted(sorted(), sortOrder()) {
            it.title.length + it.keybind.toString().length
        }.forEachIndexed { i, module ->
            var moduleWidth = renderer.textWidth(module.title) + renderer.textWidth(" ")
            val keybindName = module.keybind.toString()
            moduleWidth += renderer.textWidth(keybindName)

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
