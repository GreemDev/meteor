/*
* This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
* Copyright (c) Meteor Development.
*/
@file:JvmName("LegacyText")

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.renderer.text.TextRenderer
import meteordevelopment.meteorclient.utils.render.color.RainbowColor
import meteordevelopment.meteorclient.utils.render.color.RainbowColors
import net.greemdev.meteor.*
import net.greemdev.meteor.util.*

val colorCodeRegex = Regex("[§|&][0123456789abcdefklmnorz]")

const val betweenLines = 5

private val colorCodes = arrayOf(
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

@JvmOverloads
@JvmName("render")
fun TextRenderer.lr(text: String, startX: Double, startY: Double, color: MeteorColor = MeteorColor.WHITE, shadow: Boolean = false) {
    var x = startX
    var y = startY

    var currentColor = color
    val characters = text.toCharArray()

    val parts = colorCodeRegex.split(text)
    var index = 0
    parts.forEach { p ->
        p.forEachLine { l ->
            render(l, x, y, currentColor, shadow)
            x += getWidth(l)

            index += l.length
            if (index < characters.size && characters[index] == '\n') {
                x = startX
                y += getHeight(shadow) + betweenLines
                index++
            }
        }
        if (index < characters.size) {
            val ch = characters[index]
            if (ch == '§' || ch == '&') {
                val colorCode = characters[index + 1]
                val colorIndex = "0123456789abcdef".indexOf(colorCode)

                currentColor = when (colorCode) {
                    'r' -> color
                    'z' -> RainbowColor.current()
                    else -> MeteorColor(colorCodes[colorIndex].toInt())
                }

                index += 2
            }
        }
    }
}

fun String.onlyVisibleContent() =
    replace(colorCodeRegex, String.empty)
    .replace("\n", String.empty)
    .replace("\r", String.empty)

fun needsSpecialRenderer(content: String) = content.lineCount() > 1 || colorCodeRegex in content

fun TextRenderer.getLegacyWidth(text: String) = Meteor.currentTheme()
    .scale(getWidth(text.onlyVisibleContent()))
