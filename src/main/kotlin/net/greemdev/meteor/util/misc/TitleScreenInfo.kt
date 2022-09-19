/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.network.Http
import meteordevelopment.meteorclient.utils.render.RenderUtils
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.text.ChatColor
import net.minecraft.client.util.math.MatrixStack

object TitleScreenInfo {

    @JvmStatic
    fun loadLatestRevision() {
        val response = Http.get("https://raw.githubusercontent.com/GreemDev/meteor/main/gradle.properties").sendLines()
        latestRevision = response
            .filter { it.startsWith("revision") }
            .map { it.split("=").last() }
            .findFirst()
            .map(String::toInt)
            .orElseThrow()
    }

    @JvmStatic
    var latestRevision: Int = -1
        private set

    @JvmStatic
    fun howManyBehind() =
        if (latestRevision == -1)
            0
        else
            latestRevision - MeteorClient.REVISION

    @JvmStatic
    fun isOutdated() = latestRevision != -1 && MeteorClient.REVISION < latestRevision

    @JvmStatic
    fun isUpToDate() = latestRevision != -1 && MeteorClient.REVISION == latestRevision

    val updateChecker by invoking {
        if (latestRevision == -1)
            null
        else {
            infoLine {
                if (isUpToDate())
                    newSection("Up to date!", green)
                else if (isOutdated()) {
                    newSection(howManyBehind(), red)
                    newSection(grey) {
                        append(" revision")
                        if (howManyBehind() != 1)
                            append('s')
                        append(" behind!")
                    }
                } else {
                    newSection("dev build", lightPurple)
                }
            }
        }
    }

    @JvmStatic
    fun render(matrices: MatrixStack) {
        updateChecker?.run {
            var x = minecraft.currentScreen!!.width - 3 - width
            sections().forEach {
                RenderUtils.drawShadowed(matrices, it.text, x, 3, it.color)
                x += it.width
            }
        }
    }
}

fun infoLine(autoCalc: Boolean = true, builder: context(ChatColor.Companion) InfoLine.() -> Unit) = InfoLine(mutableListOf()).apply {
    builder(ChatColor, this)
    if (autoCalc) updateWidth()
}

data class Section(val text: String, val color: Int) {
    val width: Int = minecraft.textRenderer.getWidth(text)
}

class InfoLine(private val sections: MutableList<Section>) {
    var width = 0
        private set

    fun updateWidth(): InfoLine {
        width = sections.sumOf { it.width }
        return this
    }

    operator fun Section.unaryPlus() {
        sections.add(this)
    }

    fun newSection(text: Any?, color: ChatColor) = newSection(text.toString(), color.rgb ?: error("Cannot color text with a formatting option."))
    fun newSection(text: Any?, color: AwtColor) = newSection(text.toString(), color.rgb)
    fun newSection(text: Any?, color: MeteorColor) = newSection(text.toString(), color.packed)

    fun newSection(text: Any?, color: Int): InfoLine {
        sections.add(Section(text.toString(), color))
        return this
    }

    fun newSection(color: ChatColor, textBuilder: StringBuilder.() -> Unit) =
        newSection(color.rgb ?: error("Cannot color text with a formatting option."), textBuilder)
    fun newSection(color: AwtColor, textBuilder: StringBuilder.() -> Unit) =
        newSection(color.rgb, textBuilder)
    fun newSection(color: MeteorColor, textBuilder: StringBuilder.() -> Unit) =
        newSection(color.packed, textBuilder)

    fun newSection(color: Int, textBuilder: StringBuilder.() -> Unit): InfoLine {
        sections.add(Section(buildString(textBuilder), color))
        return this
    }

    fun sections() = sections.toList()
}
