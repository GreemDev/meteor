/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Notifications")

package net.greemdev.meteor.hud.notification

import net.greemdev.meteor.*
import net.greemdev.meteor.hud.element.NotificationHud
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.text.ChatColor
import java.awt.Color
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Consumer

fun send(notification: Notification) = notifications.send(notification)

@JvmOverloads
fun sendOrRun(n: Notification, altMessage: String? = null, func: Consumer<String>? = null) =
    notifications.sendOrRun(n, altMessage, func?.let { c -> { c.accept(it) } })

fun overrideChatFeedback() = NotificationHud.getOrNull()?.overrideChatFeedback() ?: false

val notifications = object : NotificationManager() {}

abstract class NotificationManager {
    fun sendOrRun(n: Notification, altMessage: String? = null, func: ValueAction<String>? = null) {
        if (NotificationHud.getOrNull()?.allowsSource(n.source) == true &&
            (n.event !is NotificationEvent.ModuleToggled || NotificationHud.get().allowsModule(n.event.module))
        ) {
            if (n.send() and overrideChatFeedback())
                return
        }

        func?.invoke(altMessage ?: return)
    }

    fun sendOrFallback(n: Notification) {
        if (NotificationHud.getOrNull()?.allowsSource(n.source) == true &&
            (n.event !is NotificationEvent.ModuleToggled || NotificationHud.get().allowsModule(n.event.module))
        ) {
            if (n.send() and overrideChatFeedback())
                return
        }

        if (n.fallbackPredicate != null && n.fallback != null) {
            if (n.fallbackPredicate.invoke()) {
                n.fallback.invoke(n.asText())
            }
        }
    }

    private object Queue : LinkedBlockingQueue<Notification>() {
        override infix fun add(element: Notification) =
            if (notifications.isActive && NotificationHud.get().allowsSource(element.source)) {
                super.add(element)
            } else false

        fun requeue(notif: Notification) = remove(notif) && add(notif.apply { startTime = -1 })

        fun clearNotifications() = size.also { clear() }

        fun find(n: Notification) = findOrNull(n)!!
        fun findOrNull(n: Notification) = firstOrNull(n::equals)
    }

    private val dummies = listOf(
        Notification("hey there!", "this is just an example", Color.RED),
        Notification("§zRainbow §rReset &aGreen &fWhite", "another example!", ChatColor.white.asAwt()),
        Notification("3"),
        Notification("hmmmmm", "random shit", MeteorColor.PINK.awt()),
        Notification("default color"),
        Notification("meteor good", color = Color(0x9A03FF)),
        Notification(
            "excessively long title because this is a stress test",
            "and this part too because, as i\nsaid before, this is a stress test!!!!!!!!!!!!",
        ),
        Notification("notifications are cool", color = colorOf("#7000FB").awt())
    ).onEach { it.startTime = 0 }.shuffled()

    fun send(notif: Notification) =
        Queue.findOrNull(notif)
            ?.let {
                Queue.requeue(it)
            } ?: Queue.add(notif)


    val isActive: Boolean
        get() = NotificationHud.getOrNull() != null

    fun toListOrNull(inEditor: Boolean = false): List<Notification>? {
        val hud = NotificationHud.getOrNull() ?: return null

        return if (inEditor)
            dummies.take(hud.amount())
        else
            buildList {
                Queue.forEach {
                    if (size >= hud.amount()) return this

                    if (it.startTime == -1L)
                        it.startTime = System.currentTimeMillis()

                    if ((it.startTime + hud.displayTime() >= System.currentTimeMillis() && it.event.canDisplay(hud))) {
                        add(it)
                        return@forEach
                    }

                    Queue.remove(it)
                }
            }
    }
}
