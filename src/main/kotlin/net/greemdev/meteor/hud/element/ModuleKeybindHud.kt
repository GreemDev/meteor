/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.element

import meteordevelopment.meteorclient.systems.hud.Alignment
import meteordevelopment.meteorclient.systems.hud.HudElement
import meteordevelopment.meteorclient.systems.hud.HudElementInfo
import meteordevelopment.meteorclient.systems.hud.HudRenderer
import meteordevelopment.meteorclient.systems.modules.Modules
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.Greteor
import net.greemdev.meteor.hud.HudElementMetadata
import net.greemdev.meteor.util.*
import kotlin.math.max

class ModuleKeybindHud : HudElement(elementInfo) {
    companion object : HudElementMetadata<ModuleKeybindHud> {
        override val elementInfo = HudElementInfo(Greteor.hudElementGroup(),
        "module-keybinds", "Displays selected modules with valid keybinds."
        ) {
            ModuleKeybindHud()
        }
    }

    private val sg = settings.group()

    val modules by sg moduleList {
        name("modules")
        description("The modules to display the keybinds of.")
        onlyMatching { it.keybind.isValid }
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
        if (Modules.get() == null || modules.get().isEmpty()) {
            renderer.text("Module Keybinds", x.toDouble(), y.toDouble(), moduleColor.get(), textShadow.get())
            setSize(renderer.textWidth("Module Keybinds"), renderer.textHeight())
            return
        }

        var y = this.y.toDouble()

        var width = 0.0
        var height = 0.0
        modules.get().forEachIndexed { i, module ->
            var moduleWidth = renderer.textWidth(module.title) + renderer.textWidth(" ")
            val keybindStr = module.keybind.toString()
            moduleWidth += renderer.textWidth(keybindStr)

            var x = this.x + alignX(moduleWidth, alignment.get());
            x = renderer.text(module.title, x, y, moduleColor.get(), textShadow.get())
            renderer.text(keybindStr, x + renderer.textWidth(" "), y, keybindColor.get(), textShadow.get())
            y += renderer.textHeight() + 2

            width = max(width, moduleWidth)
            height += renderer.textHeight()
            if (i > 0)
                height += 2
        }

        setSize(width, height)
    }

}
