/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.utils.render.RenderUtils
import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.text.ChatColor
import net.minecraft.SharedConstants
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.math.MatrixStack

object TitleScreenInfo {
    private val updateChecker by invoking {
        if (GVersioning.latestRevision != -1)
            infoLine {
                if (GVersioning.isUpToDate)
                    newSection("Up to date!", green)
                else if (GVersioning.isOutdated) {
                    newSection(GVersioning.revisionsBehind, red)
                    newSection(grey) {
                        +" revision".pluralize(GVersioning.revisionsBehind, prefixQuantity = false)
                        +" behind!"
                    }
                } else
                    newSection("dev build", lightPurple)

                if (SharedConstants.isDevelopment)
                    newSection(" - *dev mode*", darkRed)
            }
        else null
    }

    @JvmStatic
    fun render(drawContext: DrawContext) {
        updateChecker?.run {
            var x = minecraft.currentScreen!!.width - 3 - width
            sections.forEach {
                RenderUtils.drawShadowed(drawContext, it.text, x, 3, it.color)
                x += it.width()
            }
        }
    }
}

fun infoLine(autoCalc: Boolean = true, builder: context(ChatColor.Companion) InfoLine.() -> Unit) =
    InfoLine(mutableListOf(), autoCalc)
        .apply { builder(ChatColor, this) }

class InfoLine(private val _sections: MutableList<Section>, private val autoCalc: Boolean = false) {
    val sections by invoking(_sections::toList)

    data class Section(val text: String, val color: Int) {
        fun width() = minecraft.textRenderer.getWidth(text)
    }

    var width = 0
        private set

    fun updateWidth(): InfoLine {
        width = _sections.sumOf { it.width() }
        return this
    }

    operator fun Section.unaryPlus() {
        addSection(this)
    }

    fun addSection(section: Section): InfoLine {
        _sections.add(section)
        if (autoCalc) updateWidth()

        return this
    }

    fun newSection(text: Any, color: Int): InfoLine {
        return addSection(Section(text.toString(), color))
    }

    fun newSection(text: Any, color: ChatColor) = newSection(text, color.rgb ?: error("Cannot color text with a formatting option."))
    fun newSection(text: Any, color: AwtColor) = newSection(text, color.rgb)
    fun newSection(text: Any, color: MeteorColor) = newSection(text, color.packed)

    fun newSection(color: ChatColor, textBuilder: Initializer<StringScope>) =
        newSection(color.rgb ?: error("Cannot color text with a formatting option."), textBuilder)
    fun newSection(color: AwtColor, textBuilder: Initializer<StringScope>) =
        newSection(color.rgb, textBuilder)
    fun newSection(color: MeteorColor, textBuilder: Initializer<StringScope>) =
        newSection(color.packed, textBuilder)

    fun newSection(color: Int, textBuilder: Initializer<StringScope>) =
        newSection(string(builderScope = textBuilder), color)
}
