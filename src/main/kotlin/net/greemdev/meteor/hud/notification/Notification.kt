/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.notification

import meteordevelopment.meteorclient.systems.macros.Macro
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.*
import net.greemdev.meteor.hud.element.NotificationSource
import net.greemdev.meteor.meteor
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.text.ChatColor
import net.greemdev.meteor.util.text.buildText
import net.greemdev.meteor.util.text.textOf
import net.minecraft.text.Text
import java.util.function.Consumer

@Suppress("ObjectPropertyName")
private val _defaultTextMapper: Mapper<Notification, Text> = { n ->
    buildText {
        addText {
            addString(n.title.replace(colorCodeRegex, ""))
            colored(n.color)
        }
        n.description?.let { addString(": $it") }
    }
}

fun notification(builder: Initializer<NotificationBuilder>) = NotificationBuilder(builder.java).build()

class NotificationBuilder() {

    constructor(javaInitializer: Consumer<NotificationBuilder>) : this() {
        javaInitializer.kotlin(this)
    }

    val onlyDescription: Mapper<Notification, Text> = { textOf(it.description) }
    val onlyTitle: Mapper<Notification, Text> = { textOf(it.title) }
    val defaultTextMapper: Mapper<Notification, Text> = _defaultTextMapper

    var description: String? = null
    var event: NotificationEvent? = null
    var source: NotificationSource = NotificationSource.Default
    private var asText: Mapper<Notification, Text> = defaultTextMapper
    private var fallbackPredicate: Getter<Boolean>? = null
    private var fallback: ValueAction<Text>? = null

    private var _t: String? = null
    var title: String
        get() = _t ?: error("No title given")
        set(value) {
            _t = value
        }

    private var _c: AwtColor? = null
    var color: MeteorColor
        get() = _c?.meteor() ?: error("No color given")
        set(value) {
            _c = value.awt()
        }

    @JvmField
    val presets = Presets(this)

    class Presets(val b: NotificationBuilder) {
        fun module(module: Module, isNowOn: Boolean) {
            b._t = "&z${module.title}&r: ${if (isNowOn) "&aON" else "&4OFF"}"
            b.color = if (isNowOn) MeteorColor.GREEN else MeteorColor.RED
            b.event = NotificationEvent.ModuleToggled(module)
            b.source = NotificationSource.Module
        }

        fun packet(typeName: String) {
            b._t = "${if (typeName.contains("C2S")) "C2S " else if (typeName.contains("S2C")) "S2C " else ""}packet cancelled"
            b.description = typeName.removeSuffix("Packet").replace("C2S", "").replace("S2C", "")
            b._c = ChatColor.darkRed.asAwt()
        }

        fun macro(macro: Macro, color: AwtColor) {
            b._t = "&zMacro Triggered"
            b.description = macro.name()
            b._c = color
            b.source = NotificationSource.Notifier
        }
    }

    fun textMapper(mapper: Mapper<Notification, Text>) {
        asText = mapper
    }

    fun fallbackPredicate(predicate: Getter<Boolean>) {
        fallbackPredicate = predicate
    }

    @JvmName("ktFallback")
    fun fallback(action: ValueAction<Text>) {
        fallback = action
    }

    @JvmName("fallback")
    fun `java-fallback`(action: Consumer<Text>) {
        fallback = action.kotlin
    }

    fun build() = Notification(title, description, _c, event, source, asText)

    fun send() = build().send()
    fun sendOrFallback() = build().sendOrFallback()
}

class Notification @JvmOverloads constructor(
    val title: String,
    val description: String? = null,
    color: AwtColor? = null,
    val event: NotificationEvent? = null,
    val source: NotificationSource? = null,
    private val _asText: Mapper<Notification, Text> = _defaultTextMapper,
    val fallbackPredicate: Getter<Boolean>? = null,
    val fallback: ValueAction<Text>? = null
) {

    fun asText() = _asText(this)

    val color: MeteorColor

    var startTime: Long = -1

    init {
        this.color = color?.meteor() ?: MeteorColor.ORANGE
    }

    override fun toString() = stringHelper()
        .omitNullValues()
        .add("title", title)
        .add("description", description)
        .add("color", color.packed)
        .toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Notification
        return title == other.title && description eq other.description && color == other.color && source == other.source
    }

    override fun hashCode() = hashOf(title, description, color, source)

    fun send() = notifications.send(this)
    fun sendOrRun(altMessage: String, func: Notification.(String) -> Unit) = notifications.sendOrRun(this, altMessage) { this.func(it) }
    fun sendOrRun(func: Notification.() -> Unit) = notifications.sendOrRun(this, "") { this.func() }
    fun sendOrElse(func: Consumer<Notification>) = sendOrRun(func::accept)

    fun sendOrFallback() = notifications.sendOrFallback(this)
}
