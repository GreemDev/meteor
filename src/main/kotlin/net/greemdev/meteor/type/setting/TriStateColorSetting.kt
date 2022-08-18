/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type.setting

import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.settings.Setting
import meteordevelopment.meteorclient.settings.SettingGroup
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.util.color

class TriStateColorSetting(private val group: SettingGroup, name: String, normal: SettingColor, hovered: SettingColor, pressed: SettingColor) {

    private val normal: Setting<SettingColor>
    private val hovered: Setting<SettingColor>
    private val pressed: Setting<SettingColor>

    init {
        this.normal = createColorSetting(name, "Color of $name.", normal)
        this.hovered = createColorSetting("hovered-$name", "Color of $name when hovered.", hovered)
        this.pressed = createColorSetting("pressed-$name", "Color of $name when pressed.", pressed)
    }

    fun get(): SettingColor = normal.get()
    fun get(pressed: Boolean, hovered: Boolean, bypassDisableHoverColor: Boolean): SettingColor =
        if (pressed)
            this.pressed.get()
        else if (hovered && (bypassDisableHoverColor || !GuiThemes.get().disableHoverColor))
            this.hovered.get()
        else
            this.normal.get()


    private fun createColorSetting(name: String, description: String, color: SettingColor) = group.color {
        name("$name-color")
        description(description)
        defaultValue(color)
    }.get()


}
