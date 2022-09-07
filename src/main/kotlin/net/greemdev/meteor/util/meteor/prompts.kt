/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.utils.render.prompts.*
import net.greemdev.meteor.util.minecraft
import net.minecraft.client.gui.screen.Screen
import java.util.function.Consumer

fun confirm(id: String, builder: YesNoPrompt.() -> Unit): YesNoPrompt = YesNoPrompt.create().id(id).apply(builder)
fun showConfirm(id: String, builder: YesNoPrompt.() -> Unit) = confirm(id, builder).show()

fun notice(id: String, builder: OkPrompt.() -> Unit): OkPrompt = OkPrompt.create().id(id).apply(builder)
fun showNotice(id: String, builder: OkPrompt.() -> Unit) = notice(id, builder).show()

object Prompts {
    @JvmStatic
    fun java() = object : JavaInterop() {}
    @JvmStatic
    fun java(theme: GuiTheme, screen: Screen?) = object : JavaInterop(theme, screen) {}

    abstract class JavaInterop(private val theme: GuiTheme = GuiThemes.get(), private val screen: Screen? = minecraft.currentScreen) {
        fun showNotice(id: String, builder: Consumer<OkPrompt>) = notice(id, builder).show()
        fun showConfirm(id: String, builder: Consumer<YesNoPrompt>) = confirm(id, builder).show()
        fun notice(id: String, builder: Consumer<OkPrompt>): OkPrompt = OkPrompt.create(theme, screen).id(id).apply(builder::accept)
        fun confirm(id: String, builder: Consumer<YesNoPrompt>): YesNoPrompt = YesNoPrompt.create(theme, screen).id(id).apply(builder::accept)
    }
}
