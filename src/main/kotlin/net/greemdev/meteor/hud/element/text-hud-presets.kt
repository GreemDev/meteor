/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.element

import meteordevelopment.meteorclient.systems.hud.HudElementInfo
import meteordevelopment.meteorclient.systems.hud.elements.MeteorTextHud
import meteordevelopment.meteorclient.systems.hud.elements.TextHud

object GreteorTextHud {
    //order: Hud title, Text, condition for display

    @JvmStatic
    fun init() {
        mainhandDurability = MeteorTextHud.addPreset(
            "Mainhand Durability",
            "#5{player.hand.durability}#4/#2{player.hand.durabilityMax}",
            "{player.hand.count == 1 and player.hand.durabilityMax > 0}")
        offhandDurability = MeteorTextHud.addPreset(
            "Offhand Durability",
            "#5{player.offhand.durability}#4/#2{player.offhand.durabilityMax}",
            "{player.offhand.count == 1 and player.offhand.durabilityMax > 0}")
        oppositePosWhenCryingObsidianHeld = MeteorTextHud.addPreset(
            "Opposite Pos when holding Crying Obsidian",
            "#3{player.dimensionOpposite != \"End\" ? player.dimensionOpposite + \":\" : \"\"} #1{player.dimensionOpposite != \"End\" ? #5\"\" + floor(camera.posOpposite.x) + \", \" + floor(camera.posOpposite.y) + \", \" + floor(camera.posOpposite.z) : \"\"}",
            "{player.isItemHeld(\"Crying Obsidian\")}"
        )
    }

    lateinit var mainhandDurability: HudElementInfo<TextHud>.Preset
    lateinit var offhandDurability: HudElementInfo<TextHud>.Preset
    lateinit var oppositePosWhenCryingObsidianHeld: HudElementInfo<TextHud>.Preset
}
