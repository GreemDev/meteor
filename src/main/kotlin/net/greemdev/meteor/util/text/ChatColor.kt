/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.util.awt
import net.greemdev.meteor.util.invoking
import net.minecraft.util.Formatting

data class ChatColor private constructor(private val fmt: Formatting) {
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

    val name by invoking(fmt::getName)
    val code by invoking(fmt::getCode)
    val isModifier by invoking(fmt::isModifier)
    val isColor by invoking(fmt::isColor)
    val rgb by invoking(fmt::getColorValue)
    val mc = fmt

    fun asMeteor() = Color(rgb ?: error("Cannot obtain color for a non-color formatting option."))
    fun asAwt() = asMeteor().awt()

    override fun toString(): String = fmt.toString()
}
