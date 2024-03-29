/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:Suppress("unused") // api

package net.greemdev.meteor.util.text

import net.greemdev.meteor.*
import net.minecraft.util.Formatting

class ChatColor private constructor(val mc: Formatting) {
    companion object {
        @JvmField val black         = ChatColor(Formatting.BLACK)
        @JvmField val darkBlue      = ChatColor(Formatting.DARK_BLUE)
        @JvmField val darkGreen     = ChatColor(Formatting.DARK_GREEN)
        @JvmField val darkAqua      = ChatColor(Formatting.DARK_AQUA)
        @JvmField val darkRed       = ChatColor(Formatting.DARK_RED)
        @JvmField val darkPurple    = ChatColor(Formatting.DARK_PURPLE)
        @JvmField val darkGrey      = ChatColor(Formatting.DARK_GRAY)
        @JvmField val gold          = ChatColor(Formatting.GOLD)
        @JvmField val grey          = ChatColor(Formatting.GRAY)
        @JvmField val blue          = ChatColor(Formatting.BLUE)
        @JvmField val green         = ChatColor(Formatting.GREEN)
        @JvmField val aqua          = ChatColor(Formatting.AQUA)
        @JvmField val red           = ChatColor(Formatting.RED)
        @JvmField val lightPurple   = ChatColor(Formatting.LIGHT_PURPLE)
        @JvmField val yellow        = ChatColor(Formatting.YELLOW)
        @JvmField val white         = ChatColor(Formatting.WHITE)

        @JvmField val obfuscated    = ChatColor(Formatting.OBFUSCATED)
        @JvmField val bold          = ChatColor(Formatting.BOLD)
        @JvmField val strikethrough = ChatColor(Formatting.STRIKETHROUGH)
        @JvmField val underline     = ChatColor(Formatting.UNDERLINE)
        @JvmField val italic        = ChatColor(Formatting.ITALIC)

        @JvmField val reset         = ChatColor(Formatting.RESET)
    }

    @get:JvmName("name")
    val name: String by invoking(mc::getName)
    @get:JvmName("code")
    val code by invoking(mc::getCode)
    @get:JvmName("isModifier")
    val isModifier by invoking(mc::isModifier)
    @get:JvmName("isColor")
    val isColor by invoking(mc::isColor)
    @get:JvmName("rgbOrNull")
    val rgbOrNull by invoking(mc::getColorValue)

    fun rgb() = rgbOrNull ?: error("Cannot obtain color for a non-color formatting option.")

    @get:JvmName("meteor")
    val meteor by lazy { MeteorColor(rgb(), 255) }
    @get:JvmName("awt")
    val awt by lazy { AwtColor(rgb(), false) }

    fun components() = meteor.let { it.r to it.g then it.b }

    operator fun component1() = meteor.r
    operator fun component2() = meteor.g
    operator fun component3() = meteor.b

    override fun hashCode() = hashOf(name, code)
    override fun toString(): String = mc.toString()
    override fun equals(other: Any?) = when (other) {
        is ChatColor -> code == other.code
        is MeteorColor -> rgbOrNull != null && meteor == other
        is AwtColor -> rgbOrNull != null && meteor == other
        is Formatting -> when {
            (mc.isModifier and other.isModifier) && mc.code == other.code -> true
            (mc.isColor and other.isColor) && mc.colorIndex == other.colorIndex -> true
            else -> false
        }
        is Int -> other == rgbOrNull
        is Char -> code == other
        else -> false
    }
}
