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
import net.minecraft.client.util.math.MatrixStack

object TitleScreenInfo {
    val updateChecker by invoking {
        if (GVersioning.latestRevision == -1)
            null
        else {
            infoLine {
                if (GVersioning.isUpToDate())
                    newSection("Up to date!", green)
                else if (GVersioning.isOutdated()) {
                    newSection(GVersioning.revisionsBehind(), red)
                    newSection(grey) {
                        append(" revision".pluralize(GVersioning.revisionsBehind(), prefixQuantity = false))
                        append(" behind!")
                    }
                } else
                    newSection("dev build", lightPurple)

                if (SharedConstants.isDevelopment)
                    newSection(" - *dev mode*", darkRed)
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

fun infoLine(autoCalc: Boolean = true, builder: ColoredInitializer<InfoLine>) = InfoLine(mutableListOf()).apply {
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

    fun newSection(color: ChatColor, textBuilder: Initializer<StringBuilder>) =
        newSection(color.rgb ?: error("Cannot color text with a formatting option."), textBuilder)
    fun newSection(color: AwtColor, textBuilder: Initializer<StringBuilder>) =
        newSection(color.rgb, textBuilder)
    fun newSection(color: MeteorColor, textBuilder: Initializer<StringBuilder>) =
        newSection(color.packed, textBuilder)

    fun newSection(color: Int, textBuilder: Initializer<StringBuilder>): InfoLine {
        sections.add(Section(buildString(textBuilder), color))
        return this
    }

    fun sections() = sections.toList()
}
