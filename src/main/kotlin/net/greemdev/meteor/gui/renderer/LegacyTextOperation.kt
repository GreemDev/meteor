/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.renderer

import meteordevelopment.meteorclient.gui.renderer.GuiRenderOperation
import meteordevelopment.meteorclient.renderer.text.TextRenderer
import net.greemdev.meteor.util.meteor.renderLegacy

class LegacyTextOperation : GuiRenderOperation<LegacyTextOperation>() {
    lateinit var text: String
        private set
    lateinit var renderer: TextRenderer
        private set
    var title = false
        private set
    var shadow = false
        private set

    fun set(text: String, renderer: TextRenderer, title: Boolean, shadow: Boolean): LegacyTextOperation {
        this.text = text
        this.renderer = renderer
        this.title = title
        this.shadow = shadow

        return this
    }


    override fun onRun() {
        renderer.renderLegacy(text, x, y, color.awt(), shadow)
    }
}
