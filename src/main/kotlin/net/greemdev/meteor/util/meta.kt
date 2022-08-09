/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Util")
package net.greemdev.meteor.util

import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.settings.*


fun StringSetting.Builder.renderStarscript(): StringSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)
fun StringListSetting.Builder.renderStarscript(): StringListSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)

fun IntSetting.Builder.saneSlider(): IntSetting.Builder = sliderRange(this.min, this.max)

fun<T : WPressable> T.action(func: () -> Unit): T = action(Runnable(func))
fun<T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }
