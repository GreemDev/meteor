/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Notifications")

package net.greemdev.meteor.hud.notification

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

fun active() = notifications.active()
fun overrideChatFeedback() = NotificationHud.getOrNull()?.overrideChatFeedback() ?: false

val notifications = object : NotificationManager() {}

abstract class NotificationManager {
    fun sendOrRun(n: Notification, altMessage: String? = null, func: ((String) -> Unit)? = null) {
        if (NotificationHud.getOrNull()?.allowsSource(n.source) == true &&
            (n.event !is NotificationEvent.ModuleToggled || NotificationHud.get().allowsModule(n.event.module))
        ) {
            n.send()
            if (overrideChatFeedback())
                return
        }

        func?.invoke(altMessage ?: return)
    }

    private object NotificationQueue : LinkedBlockingQueue<Notification>() {
        override fun add(element: Notification) =
            if (active() && NotificationHud.get().allowsSource(element.source)) {
                super.add(element)
            } else false

        fun find(id: Int) = findOrNull(id)!!
        fun findOrNull(id: Int) = firstOrNull { it.id == id }
        fun find(n: Notification) = findOrNull(n)!!
        fun findOrNull(n: Notification) = firstOrNull { it == n }
    }

    private val dummies = listOf(
        Notification("hey there!", "this is just an example", Color.RED),
        Notification("§zRainbow §rReset &aGreen &fWhite", "another example!", ChatColor.white.asAwt()),
        Notification("3"),
        Notification("hmmmmm", "random shit", MeteorColor.PINK),
        Notification("default color"),
        Notification("meteor good", Color(0x9A03FF)),
        Notification(
            "excessively long title because this is a stress test",
            "and this part too because, as i said before, this is a stress test!!!!!!!!!!!!",
        ),
        Notification("notifications are cool", colorOf("#7000FB"))
    ).onEach { it.startTime = 0 }.shuffled()

    fun send(notif: Notification) =
        NotificationQueue.findOrNull(notif)
            ?.let {
                NotificationQueue.remove(it)
                NotificationQueue.add(it)
            } ?: NotificationQueue.add(notif)


    fun active() = NotificationHud.getOrNull() != null

    fun persist(id: Int) {
        NotificationQueue.findOrNull(id)
            ?.let {
                NotificationQueue.remove(it)
                NotificationQueue.add(it.persist())
            }
    }

    fun clearQueue() = NotificationQueue.size.also { NotificationQueue.clear() }

    fun toListOrNull(inEditor: Boolean = false): List<Notification>? {
        val hud = NotificationHud.getOrNull() ?: return null

        return if (inEditor)
            dummies.take(hud.amount())
        else
            buildList {
                NotificationQueue.forEach {
                    if (size >= hud.amount()) return this

                    if (it.startTime == -1L && !it.persistent)
                        it.startTime = System.currentTimeMillis()

                    if ((it.startTime + hud.displayTime() >= System.currentTimeMillis()) || it.persistent)
                        if (it.event.canDisplay(hud)) {
                            add(it)
                            return@forEach
                        }

                    NotificationQueue.remove(it)
                }
            }
    }
}
