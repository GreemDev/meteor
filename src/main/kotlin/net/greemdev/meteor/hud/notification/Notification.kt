/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.notification

import com.google.common.base.MoreObjects
import meteordevelopment.meteorclient.systems.macros.Macro
import meteordevelopment.meteorclient.systems.modules.Module
import net.greemdev.meteor.hud.element.NotificationSource
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.text.ChatColor
import java.util.Objects
import java.util.function.Consumer


open class Notification(val title: String, val description: String?, color: AwtColor?, val event: NotificationEvent?, val source: NotificationSource?) {
    companion object {
        private var nextId: Int = 0
        fun nextId() = nextId++

        @JvmStatic
        fun module(module: Module, isNowOn: Boolean) = Notification(
            title = "&zModule ${module.title}&r: ${if (isNowOn) "&aON" else "&4OFF"}",
            color = if (isNowOn) MeteorColor.GREEN else MeteorColor.RED,
            event = ModuleToggledNEvent(module),
            NotificationSource.Module
        )
        @JvmStatic
        fun notifier(title: String, description: String, color: MeteorColor) = Notification(title, description, color, NotificationSource.Notifier)
        @JvmStatic
        fun macro(macro: Macro, color: AwtColor) = Notification("&zMacro Triggered", macro.name(), color, NotificationSource.Macro)
        @JvmStatic
        fun command(title: String, description: String, color: MeteorColor) = Notification(title, description, color, NotificationSource.Command)
        @JvmStatic
        fun packet(typeName: String) = Notification(
            "${if (typeName.contains("C2S")) "C2S " else if (typeName.contains("S2C")) "S2C " else ""}packet cancelled",
            typeName.removeSuffix("Packet").replace("C2S", "").replace("S2C", ""),
            ChatColor.darkRed.asMeteor(),
            NotificationSource.Default
        )
    }

    val id = nextId()
    val color: MeteorColor

    var persistent = false

    var startTime: Long = -1

    init {
        this.color = color?.meteor() ?: MeteorColor.ORANGE
    }

    constructor(title: String, source: NotificationSource? = null) : this(title, null, null, null, source)
    constructor(title: String, description: String, source: NotificationSource? = null) : this(title, description, null, null, source)
    constructor(title: String, event: NotificationEvent, source: NotificationSource? = null) : this(title, null, null, event, source)
    constructor(title: String, color: AwtColor, source: NotificationSource? = null) : this(title, null, color, null, source)
    constructor(title: String, color: MeteorColor, source: NotificationSource? = null) : this(title, null, color.awt(), null, source)
    constructor(title: String, description: String, color: AwtColor, source: NotificationSource? = null) : this(title, description, color, null, source)
    constructor(title: String, description: String, color: MeteorColor, source: NotificationSource? = null) : this(title, description, color.awt(), null, source)
    constructor(title: String, color: AwtColor, event: NotificationEvent, source: NotificationSource? = null) : this(title, null, color, event, source)
    constructor(title: String, color: MeteorColor, event: NotificationEvent, source: NotificationSource? = null) : this(title, null, color.awt(), event, source)

    override fun toString() = MoreObjects.toStringHelper(this)
        .omitNullValues()
        .add("title", title)
        .add("description", description)
        .add("color", color.packed)
        .add("persistent", persistent)
        .toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        other as Notification
        return title == other.title && Objects.equals(description, other.description) && color == other.color && source == other.source && persistent == other.persistent
    }

    fun persist(): Notification {
        persistent = !persistent
        return this
    }

    override fun hashCode() = Objects.hash(title, description, color, source, persistent)

    fun send() = notifications.send(this)
    fun sendOrRun(altMessage: String, func: Notification.(String) -> Unit) = notifications.sendOrRun(this, altMessage) { this.func(it) }
    fun sendOrRun(func: Notification.() -> Unit) = notifications.sendOrRun(this, "") { this.func() }
    fun sendOrElse(func: Consumer<Notification>) = sendOrRun(func::accept)
}
