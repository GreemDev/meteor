/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type.setting

import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.settings.ColorSetting
import meteordevelopment.meteorclient.settings.SettingGroup
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.util.meteor.color

class TriStateColorSetting(
    private val group: SettingGroup,
    name: String,
    normal: SettingColor,
    hovered: SettingColor,
    pressed: SettingColor
) {

    private val normal  = createColorSetting(name, "Color of $name.", normal)
    private val hovered = createColorSetting("hovered-$name", "Color of $name when hovered.", hovered)
    private val pressed = createColorSetting("pressed-$name", "Color of $name when pressed.", pressed)

    operator fun invoke(): SettingColor = normal.get()
    fun get(pressed: Boolean, hovered: Boolean, bypassDisableHoverColor: Boolean = false): SettingColor =
        if (pressed)
            this.pressed.get()
        else if (hovered and (bypassDisableHoverColor or !GuiThemes.get().disableHoverColor))
            this.hovered.get()
        else
            this.normal.get()


    private fun createColorSetting(name: String, description: String, color: SettingColor): ColorSetting = group.color {
        name("$name-color")
        description(description)
        defaultValue(color)
    }.get()


}
