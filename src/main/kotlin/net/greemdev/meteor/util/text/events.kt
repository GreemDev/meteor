/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
//@file:JvmName("KEvents")
package net.greemdev.meteor.util.text

import net.minecraft.text.ClickEvent
import net.minecraft.text.HoverEvent
import net.minecraft.text.HoverEvent.EntityContent
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.text.Text

typealias HoverAction<T> = HoverEvent.Action<T>
typealias ClickAction = ClickEvent.Action


@Suppress("ClassName") //intended for access like a variable, `actions.showText`
object actions {
    @JvmStatic
    val showText: HoverAction<Text> = HoverAction.SHOW_TEXT
    @JvmStatic
    val showItem: HoverAction<ItemStackContent> = HoverAction.SHOW_ITEM
    @JvmStatic
    val showEntity: HoverAction<EntityContent> = HoverAction.SHOW_ENTITY

    @JvmStatic
    val openURL: ClickAction = ClickAction.OPEN_URL
    @JvmStatic
    val openFile: ClickAction = ClickAction.OPEN_FILE
    @JvmStatic
    val runCommand: ClickAction = ClickAction.RUN_COMMAND
    @JvmStatic
    val suggestCommand: ClickAction = ClickAction.SUGGEST_COMMAND
    @JvmStatic
    val changePage: ClickAction = ClickAction.CHANGE_PAGE
    @JvmStatic
    val clipboardCopy: ClickAction = ClickAction.COPY_TO_CLIPBOARD
}

fun FormattedTextBuilder.onClick(action: ClickAction, value: String) = onClick(clicked(action, value))
fun<T> FormattedTextBuilder.onHovered(action: HoverAction<T>, value: T) = onHovered(hovered(action, value))

fun clicked(action: ClickAction, value: String): ClickEvent = ClickEvent(action, value)


fun<T> hovered(action: HoverAction<T>, contents: T): HoverEvent = HoverEvent(action, contents)

