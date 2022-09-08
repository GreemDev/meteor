/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.text

import net.greemdev.meteor.util.invoking
import net.minecraft.util.Formatting

data class ChatColor private constructor(private val fmt: Formatting) {
    companion object {
        val black = ChatColor(Formatting.BLACK)
        val darkBlue = ChatColor(Formatting.DARK_BLUE)
        val darkGreen = ChatColor(Formatting.DARK_GREEN)
        val darkAqua = ChatColor(Formatting.DARK_AQUA)
        val darkRed = ChatColor(Formatting.DARK_RED)
        val darkPurple = ChatColor(Formatting.DARK_PURPLE)
        val darkGrey = ChatColor(Formatting.DARK_GRAY)
        val gold = ChatColor(Formatting.GOLD)
        val grey = ChatColor(Formatting.GRAY)
        val blue = ChatColor(Formatting.BLUE)
        val green = ChatColor(Formatting.GREEN)
        val aqua = ChatColor(Formatting.AQUA)
        val red = ChatColor(Formatting.RED)
        val lightPurple = ChatColor(Formatting.LIGHT_PURPLE)
        val yellow = ChatColor(Formatting.YELLOW)
        val white = ChatColor(Formatting.WHITE)
        val reset = ChatColor(Formatting.RESET)

        val obfuscated = ChatColor(Formatting.OBFUSCATED)
        val bold = ChatColor(Formatting.BOLD)
        val strikethrough = ChatColor(Formatting.STRIKETHROUGH)
        val underline = ChatColor(Formatting.UNDERLINE)
        val italic = ChatColor(Formatting.ITALIC)
    }

    val name by invoking(fmt::getName)
    val code by invoking(fmt::getCode)
    val isModifier by invoking(fmt::isModifier)
    val isColor by invoking(fmt::isColor)
    val packedRgb by invoking(fmt::getColorValue)
    val mc = fmt

    override fun toString(): String = fmt.toString()
}
