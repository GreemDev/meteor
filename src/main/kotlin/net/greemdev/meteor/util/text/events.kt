/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("ChatEvents")
package net.greemdev.meteor.util.text

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.Text

typealias HoverAction<T> = HoverEvent.Action<T>
typealias ClickAction = ClickEvent.Action


@Suppress("ClassName") //intended for access similar to a variable, `actions.showText`
object actions {
    @JvmField
    val showText: HoverAction<Text> = HoverAction.SHOW_TEXT
    @JvmField
    val showItem: HoverAction<HoverEvent.ItemStackContent> = HoverAction.SHOW_ITEM
    @JvmField
    val showEntity: HoverAction<HoverEvent.EntityContent> = HoverAction.SHOW_ENTITY
    @JvmField
    val openURL: ClickAction = ClickAction.OPEN_URL
    @JvmField
    val openFile: ClickAction = ClickAction.OPEN_FILE
    @JvmField
    val runCommand: ClickAction = ClickAction.RUN_COMMAND
    @JvmField
    val suggestCommand: ClickAction = ClickAction.SUGGEST_COMMAND
    @JvmField
    val changePage: ClickAction = ClickAction.CHANGE_PAGE
    @JvmField
    val clipboardCopy: ClickAction = ClickAction.COPY_TO_CLIPBOARD
}

@JvmName("click")
fun clicked(action: ClickAction, value: String): ClickEvent = ClickEvent(action, value)

@JvmName("hover")
fun<T> hovered(action: HoverAction<T>, contents: T): HoverEvent = HoverEvent(action, contents)
