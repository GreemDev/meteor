/*
* This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
* Copyright (c) Meteor Development.
*/
@file:JvmName("LegacyText")

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.renderer.text.TextRenderer
import meteordevelopment.meteorclient.utils.render.color.RainbowColor
import net.greemdev.meteor.util.*

val colorCodeRegex = Regex("[§|&][0123456789abcdefklmnorz]")

private val COLOR_CODES = arrayOf(
    0xFF000000,
    0xFF0000AA,
    0xFF00AA00,
    0xFF00AAAA,
    0xFFAA0000,
    0xFFAA00AA,
    0xFFFFAA00,
    0xFFAAAAAA,
    0xFF555555,
    0xFF5555FF,
    0xFF55FF55,
    0xFF55FFFF,
    0xFFFF5555,
    0xFFFF55FF,
    0xFFFFFF55,
    0xFFFFFFFF
)

@JvmName("render")
fun TextRenderer.legacyRender(text: String, startX: Double, startY: Double, color: AwtColor, shadow: Boolean = false) {
    var x = startX
    var y = startY

    var currentColor = color.rgb
    val characters = text.toCharArray()

    val parts = colorCodeRegex.split(text)
    var index = 0
    parts.forEach { p ->
        p.split("\n").forEach { l ->
            l.split("\r").forEach { sec ->
                render(sec, x, y, MeteorColor(currentColor))
                x += getWidth(sec)

                index += sec.length
                if (index < characters.size && characters[index] == '\r') {
                    x = startX
                    index++
                }
            }
            if (index < characters.size && characters[index] == '\n') {
                x = startX
                y += getHeight(shadow) * 2
                index++
            }
        }
        if (index < characters.size) {
            val ch = characters[index]
            if (ch == '§' || ch == '&') {
                val colorCode = characters[index + 1]
                val colorIndex = "0123456789abcdef".indexOf(colorCode)

                currentColor = when (colorCode) {
                    'r' -> color.rgb
                    'z' -> RainbowColor.GLOBAL.packed
                    else -> COLOR_CODES[colorIndex].toInt()
                }

                index += 2
            }
        }
    }
}

@JvmName("getWidth")
fun TextRenderer.getLegacyWidth(text: String) = getWidth(text.replace(colorCodeRegex, ""))
