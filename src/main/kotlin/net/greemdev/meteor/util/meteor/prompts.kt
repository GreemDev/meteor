/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.utils.render.prompts.*
import net.greemdev.meteor.Initializer
import net.greemdev.meteor.kotlin
import net.greemdev.meteor.util.minecraft
import net.minecraft.client.gui.screen.Screen
import java.util.function.Consumer

fun confirm(id: String, builder: Initializer<YesNoPrompt>): YesNoPrompt = YesNoPrompt.create().id(id).apply(builder)
fun showConfirm(id: String, builder: Initializer<YesNoPrompt>) = confirm(id, builder).show()

fun notice(id: String, builder: Initializer<OkPrompt>): OkPrompt = OkPrompt.create().id(id).apply(builder)
fun showNotice(id: String, builder: Initializer<OkPrompt>) = notice(id, builder).show()

object Prompts {
    @JvmStatic
    @JvmOverloads
    fun java(theme: GuiTheme = GuiThemes.get(), screen: Screen? = minecraft.currentScreen) = object : JavaInterop(theme, screen) {}

    abstract class JavaInterop(private val theme: GuiTheme, private val screen: Screen?) {
        fun showNotice(id: String, builder: Consumer<OkPrompt>) = notice(id, builder).show()
        fun showConfirm(id: String, builder: Consumer<YesNoPrompt>) = confirm(id, builder).show()
        fun notice(id: String, builder: Consumer<OkPrompt>): OkPrompt = OkPrompt.create(theme, screen).id(id).apply(builder.kotlin)
        fun confirm(id: String, builder: Consumer<YesNoPrompt>): YesNoPrompt = YesNoPrompt.create(theme, screen).id(id).apply(builder.kotlin)
    }
}
