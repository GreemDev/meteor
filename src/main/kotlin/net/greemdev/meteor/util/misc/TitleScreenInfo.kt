/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.text.ChatColor
import net.minecraft.SharedConstants
import net.minecraft.client.gui.DrawContext

object TitleScreenInfo {
    private val updateChecker by invoking {
        if (GVersioning.latestRevision != -1)
            InfoLine {
                if (GVersioning.isUpToDate)
                    green section "Up to date!"
                else if (GVersioning.isOutdated) {
                    val (behind, color) = GVersioning.getRevisionsBehindAndColor()
                    Section(behind, color)
                    grey section {
                        +" revision".pluralize(behind)
                        +" behind!"
                    }
                } else
                    lightPurple section "dev build"

                if (modLoader.isDevelopmentEnvironment)
                    darkRed section " - *dev mode*"
            }
        else null
    }

    @JvmStatic
    fun DrawContext.render() {
        updateChecker?.run {
            var x = minecraft.currentScreen!!.width - 3 - width
            sections.forEach {
                drawText(minecraft.textRenderer, it.text, x, 3, it.color, true)
                x += it.width
            }
        }
    }
}

fun InfoLine(builder: context(ChatColor.Companion) InfoLine.() -> Unit) = InfoLine().apply {
    builder(ChatColor, this)
}

class InfoLine(private val secs: MutableList<Section> = mutableListOf()) {
    val sections = secs.toList()

    var width = 0
        private set

    private fun addSection(section: Section) {
        secs.add(section)
        width = secs.sumOf(Section::width)
    }

    infix fun ChatColor.section(text: Initializer<StringScope>) = Section(this, text)
    infix fun ChatColor.section(content: Any) = Section(content, this)

    inner class Section(content: Any, val color: Int) {
        val text = content.toString()
        val width = minecraft.textRenderer.getWidth(text)

        init {
            addSection(this)
        }
        operator fun component1() = text
        operator fun component2() = color

        constructor(content: Any, color: ChatColor) : this(content, color.rgb())
        constructor(content: Any, color: AwtColor) : this(content, color.rgb)
        constructor(content: Any, color: MeteorColor) : this(content, color.packed)
        constructor(color: Int, text: Initializer<StringScope>) : this(string(builderScope = text), color)
        constructor(color: ChatColor, text: Initializer<StringScope>) : this(color.rgb(), text)
        constructor(color: AwtColor, text: Initializer<StringScope>) : this(color.rgb, text)
        constructor(color: MeteorColor, text: Initializer<StringScope>) : this(color.packed, text)
    }
}
