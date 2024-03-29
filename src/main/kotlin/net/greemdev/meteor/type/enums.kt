/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:Suppress("unused")

package net.greemdev.meteor.type

import meteordevelopment.meteorclient.utils.render.AlignmentY

enum class StringComparisonType {
    Equals,
    Contains,
    StartsWith,
    EndsWith;

    fun compare(base: String, to: String, ignoreCase: Boolean = true) = when (this) {
        Equals -> base.equals(to, ignoreCase)
        Contains -> base.contains(to, ignoreCase)
        StartsWith -> base.startsWith(to, ignoreCase)
        EndsWith -> base.endsWith(to, ignoreCase)
    }

    override fun toString() = when (this) {
        Equals -> "Equals"
        Contains -> "Contains"
        StartsWith -> "Starts With"
        EndsWith -> "Ends With"
    }
}

enum class PrefixBrackets(pairing: Pair<String, String>) {
    Square("[" to "]"),
    Curly("{" to "}"),
    Parenthesis("(" to ")"),
    Angled("<" to ">"),
    Hashtag("#"),
    Separator("|"),
    Equals("="),
    Colon(":"),
    Hyphen("-");

    constructor(str: String) : this(str to str)

    override fun toString() = "$left $right"

    fun surround(text: String) = left + text + right

    @get:JvmName("left")
    val left = pairing.first
    @get:JvmName("right")
    val right = pairing.second
}

enum class ItemSelectMode {
    Sequential,
    Random
}

enum class DamageOperatorType(val friendly: String, val prefixFormat: String) {
    OperatorOnly("+/- only", "%s"),
    OperatorWithSpace("+/-, then space", "%s "),
    None("No +/-", "");

    val supportsRainbow = prefixFormat.isNotEmpty() //rainbow numbers shouldn't be allowed if there's no prefixing operator
    fun formatPrefix(operator: Char) = prefixFormat.format(operator)
    fun formatNumber(operator: Char, number: String) = formatPrefix(operator) + number
    override fun toString() = friendly
}

enum class ColorSettingScreenMode {
    RGBAFields,
    HexField,
    All;

    override fun toString(): String = when (this) {
        RGBAFields -> "RGBA fields & sliders"
        HexField -> "Hex field only"
        All -> "All color options"
    }

    fun isHexVisible() = isAll() || this == HexField
    fun isRgbaVisible() = isAll() || this == RGBAFields
    fun isAll() = this == All
}

enum class ChatPrefix {
    Meteor,
    Greteor;

    override fun toString() = when (this) {
        Meteor -> "Meteor"
        Greteor -> "Greteor"
    }
}


enum class VerticalAlignment {
    Top,
    Bottom;

    fun top() = this == Top
    fun bottom() = this == Bottom

    fun meteorAlignment() = when (this) {
        Top -> AlignmentY.Top
        Bottom -> AlignmentY.Bottom
    }
}

