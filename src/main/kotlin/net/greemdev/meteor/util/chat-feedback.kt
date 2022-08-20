/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("ChatFeedback")

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.systems.config.Config
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.text.TextColor
import net.minecraft.util.Formatting
import java.util.regex.Pattern

private const val prefixRegexBase = "^\\s{0,2}(<[0-9]{1,2}:[0-9]{1,2}>\\s)?{{PREFIXPATTERN}}"

fun prefixRegex(): Regex {
    val prefix = getOrNull { Config.get().meteorPrefix() } ?: "Meteor"
    val brackets = getOrNull { Config.get().meteorPrefixBrackets() } ?: PrefixBrackets.Square

    return Regex(prefixRegexBase.replace("{{PREFIXPATTERN}}", "\\${brackets.left}$prefix\\${brackets.right}"))
}

fun feedbackPrefix(): Text {
    val color = getOrNull { Config.get().meteorPrefixColor() } ?: MeteorClient.ADDON.color.toSetting()
    val prefix = getOrNull { Config.get().meteorPrefix() } ?: "Meteor"
    val brackets = getOrNull { Config.get().meteorPrefixBrackets() } ?: PrefixBrackets.Square

    return Text.literal("")
        .setStyle(Style.EMPTY.withFormatting(Formatting.GRAY))
        .append(brackets.left)
        .append(Text.literal(prefix).setStyle(Style.EMPTY.withColor(TextColor(color.packed))))
        .append("${brackets.right} ")
}

enum class PrefixBrackets(pairing: Pair<String, String>) {
    Square("[" to "]"),
    Curly("{" to "}"),
    Parenthesis("(" to ")"),
    Angled("<" to ">"),
    Hashtag("#" to "#"),
    Separator("|" to "|");

    override fun toString() = "$left $right"

    val left = pairing.first
    val right = pairing.second
}
