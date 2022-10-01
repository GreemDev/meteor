/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.util.*
import net.minecraft.util.Formatting

data class ChatColor private constructor(private val mc: Formatting) {

    companion object {
        @JvmField
        val black = ChatColor(Formatting.BLACK)

        @JvmField
        val darkBlue = ChatColor(Formatting.DARK_BLUE)

        @JvmField
        val darkGreen = ChatColor(Formatting.DARK_GREEN)

        @JvmField
        val darkAqua = ChatColor(Formatting.DARK_AQUA)

        @JvmField
        val darkRed = ChatColor(Formatting.DARK_RED)

        @JvmField
        val darkPurple = ChatColor(Formatting.DARK_PURPLE)

        @JvmField
        val darkGrey = ChatColor(Formatting.DARK_GRAY)

        @JvmField
        val gold = ChatColor(Formatting.GOLD)

        @JvmField
        val grey = ChatColor(Formatting.GRAY)

        @JvmField
        val blue = ChatColor(Formatting.BLUE)

        @JvmField
        val green = ChatColor(Formatting.GREEN)

        @JvmField
        val aqua = ChatColor(Formatting.AQUA)

        @JvmField
        val red = ChatColor(Formatting.RED)

        @JvmField
        val lightPurple = ChatColor(Formatting.LIGHT_PURPLE)

        @JvmField
        val yellow = ChatColor(Formatting.YELLOW)

        @JvmField
        val white = ChatColor(Formatting.WHITE)

        @JvmField
        val reset = ChatColor(Formatting.RESET)

        @JvmField
        val obfuscated = ChatColor(Formatting.OBFUSCATED)

        @JvmField
        val bold = ChatColor(Formatting.BOLD)

        @JvmField
        val strikethrough = ChatColor(Formatting.STRIKETHROUGH)

        @JvmField
        val underline = ChatColor(Formatting.UNDERLINE)

        @JvmField
        val italic = ChatColor(Formatting.ITALIC)
    }

    val name by invoking(mc::getName)
    val code by invoking(mc::getCode)
    val isModifier by invoking(mc::isModifier)
    val isColor by invoking(mc::isColor)
    val rgb by invoking(mc::getColorValue)


    fun asMeteor() = Color(rgb ?: error("Cannot obtain color for a non-color formatting option.")).apply { a = 255 }
    fun asAwt() = asMeteor().awt()


    override fun hashCode() = asMeteor().hashCode()
    override fun toString(): String = mc.toString()
    override fun equals(other: Any?) = when (other) {
        is ChatColor -> code == other.code
        is MeteorColor -> rgb != null && asMeteor() == other
        is AwtColor -> rgb != null && asAwt() == other
        is Formatting -> when {
            (mc.isModifier and other.isModifier) && mc.code == other.code -> true
            (mc.isColor and other.isColor) && mc.colorIndex == other.colorIndex -> true
            else -> false
        }
        is Int -> rgb == other
        is Char -> code == other
        else -> false
    }

}
