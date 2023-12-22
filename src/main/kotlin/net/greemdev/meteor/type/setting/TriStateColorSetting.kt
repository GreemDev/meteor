/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type.setting

import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.settings.SettingGroup
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.meteor.color

fun SettingGroup.triColor(name: String, normal: SettingColor, hovered: SettingColor, pressed: SettingColor) =
    TriStateColorSetting(this, name, normal, hovered, pressed)

class TriStateColorSetting(
    group: SettingGroup,
    name: String,
    normal: SettingColor,
    hovered: SettingColor,
    pressed: SettingColor
) {
    private val normal by group color {
        name("$name-color")
        description("Color of $name.")
        defaultValue(normal)
    }
    private val hovered by group color {
        name("hovered-$name-color")
        description("Color of $name when hovered.")
        defaultValue(hovered)
    }
    private val pressed by group color {
        name("pressed-$name-color")
        description("Color of $name when pressed.")
        defaultValue(pressed)
    }

    @JvmOverloads
    @JvmName("get")
    operator fun invoke(pressed: Boolean = false, hovered: Boolean = false, bypassDisableHoverColor: Boolean = false): SettingColor =
        if (pressed)
            pressed()
        else if (hovered and (bypassDisableHoverColor or !GuiThemes.get().disableHoverColor))
            hovered()
        else
            normal()
}
