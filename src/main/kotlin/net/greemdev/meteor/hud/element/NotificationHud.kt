/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.hud.element

import meteordevelopment.meteorclient.gui.utils.AlignmentX
import meteordevelopment.meteorclient.renderer.text.TextRenderer
import meteordevelopment.meteorclient.settings.IntSetting
import meteordevelopment.meteorclient.systems.hud.HudElement
import meteordevelopment.meteorclient.systems.hud.HudRenderer
import meteordevelopment.meteorclient.systems.hud.screens.HudElementScreen
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.utils.Utils
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import meteordevelopment.meteorclient.utils.render.prompts.OkPrompt
import net.greemdev.meteor.gui.theme.round.RoundedTheme
import net.greemdev.meteor.gui.theme.round.util.RoundedRenderer2D
import net.greemdev.meteor.hud.HudElementMetadata
import net.greemdev.meteor.hud.notification.notifications
import net.greemdev.meteor.type.ErrorPrompt
import net.greemdev.meteor.util.AwtColor
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.meteor.Prompts.java
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.saneSlider
import kotlin.math.min

private var nInstance: NotificationHud? = null

class NotificationHud : HudElement(info) {
    companion object : HudElementMetadata<NotificationHud>(
        "notifications",
        "Displays various notifications in real-time on your HUD.", {
            if (nInstance != null)
                throw ErrorPrompt {
                    notice("single-notification-hud") {
                        title("Multiple Notification HUDs")
                        message("You are limited to only one Notification HUD element.")
                    }
                }
            else {
                nInstance = NotificationHud()
                nInstance!!
            }
        }
    ) {

        @JvmStatic
        fun getOrNull() = nInstance
        @JvmStatic
        fun get() = nInstance!!
    }

    override fun remove() {
        nInstance = null
        super.remove()
    }

    private val sg = settings.group()
    private val sgS = settings.group("Sources", false)
    private val sgP = settings.group("Proportions", false)

    val displayTime: IntSetting by sg int {
        name("display-time")
        description("The duration that notifications should be displayed for, in milliseconds.")
        onChanged {
            val c = it / 5
            if (animationDuration() > c)
                animationDuration.set(c)

            animationDuration.max = c
            animationDuration.sliderMax = c
            if (minecraft.currentScreen is HudElementScreen)
                (minecraft.currentScreen as HudElementScreen).reload()
        }
        defaultValue(5000)
        min(500)
        max(60000)
        saneSlider()
    }


    val amount by sg int {
        name("max-amount")
        description("The maximum amount of notifications that can be in the UI at any given time.")
        defaultValue(3)
        min(1)
        max(12)
        saneSlider()
    }

    val verticalAlign by sg.enum<VA> {
        name("vertical-align")
        description("The vertical alignment of the notifications.")
        defaultValue(VA.Bottom)
    }

    val titleAlign by sg.enum<AlignmentX> {
        name("title-alignment")
        description("The horizontal alignment of the notification title.")
        defaultValue(AlignmentX.Center)
    }

    val descAlign by sg.enum<AlignmentX> {
        name("description-alignment")
        description("The horizontal alignment of the notification description.")
        defaultValue(AlignmentX.Center)
    }

    val backgroundColor by sg color {
        name("background-color")
        description("The background color of the notifications.")
        defaultValue(SettingColor(35, 35, 35, 150))
    }

    fun getRound(): Double = if (inheritRoundness.isVisible && inheritRoundness())
        (Meteor.currentTheme() as RoundedTheme).round()
    else roundness()

    val inheritRoundness by sg bool {
        name("use-theme-roundness")
        description("When using the Rounded theme, use your global roundness for Notifications.")
        visible { Meteor.currentTheme() is RoundedTheme }
        defaultValue(true)
    }

    val roundness by sg double {
        name("roundness")
        description("How rounded the notification rectangles are. 0.0 forces un-rounded.")
        defaultValue(10.0)
        min(0.0)
        max(10.0)
        visible {
            !inheritRoundness()
        }
        saneSlider()
    }

    val animationDuration by sg int {
        name("animation-duration")
        description("The length of the animation, in milliseconds.")
        defaultValue(250)
        min(0)
        max(displayTime() / 5)
        saneSlider()
    }

    // Sources

    val overrideChatFeedback by sgS bool {
        name("override-chat-feedback")
        description("Replaces chat feedback with a notification. Untick to see both.")
        defaultValue(true)
    }

    val moduleSource by sgS bool {
        name("from-modules")
        description("Receive notifications when Modules toggle.")
        defaultValue(true)
    }

    val modules by sgS moduleList {
        name("modules-to-display")
        description("The modules to display in notifications.")
        defaultValue(Meteor.modules().list)
        visible(moduleSource::get)
    }

    val notifierSource by sgS bool {
        name("from-notifier")
        description("Receive notifications from the Notifier module.")
        defaultValue(true)
    }

    val macroSource by sgS bool {
        name("from-macro")
        description("Receive notifications when a macro is triggered.")
        defaultValue(true)
    }

    val commandSource by sgS bool {
        name("from-commands")
        description("Receive notifications for certain commands.")
        defaultValue(true)
    }

    // Proportions

    val notifHeight by sgP int {
        name("notification-height")
        description("The height of the individual notifications.")
        defaultValue(60)
        min(40)
        max(150)
        saneSlider()
    }

    val progressBarHeight by sgP int {
        name("progress-bar-height")
        description("The height of the progress bar (0 to disable).")
        defaultValue(5)
        min(0)
        max(15)
        saneSlider()
    }

    val notifPaddingY by sgP int {
        name("notification-padding-y")
        description("The padding between notifications on the vertical axis.")
        defaultValue(10)
        min(0)
        max(35)
        saneSlider()
    }

    val notifTextPaddingX by sgP int {
        name("notification-text-padding-x")
        description("The padding between the border of the notification and the text on the X (horizontal) axis, in %.")
        defaultValue(10)
        min(0)
        max(50)
        saneSlider()
    }

    val titleScaling by sgP bool {
        name("title-scaling")
        description("Scale the Notification title by a custom amount.")
        defaultValue(false)
    }

    val descScaling by sgP bool {
        name("description-scaling")
        description("Scale the Notification description by a custom amount.")
        defaultValue(false)
    }

    val titleScale by sgP double {
        name("title-scale")
        description("Scale the Notification title by this multiplier.")
        visible(titleScaling::get)
        range(0.5, 2.0)
        defaultValue(1.25)
        saneSlider()
    }

    val descScale by sgP double {
        name("description-scale")
        description("Scale the Notification description by this multiplier.")
        visible(descScaling::get)
        range(0.5, 2.5)
        defaultValue(1.5)
        saneSlider()
    }

    val width by sgP int {
        name("width")
        description("The width of the notifications.")
        defaultValue(250)
        min(150)
        max(750)
        saneSlider()
    }


    override fun tick(renderer: HudRenderer) {
        box.setSize(
            width().toDouble(),
            ((notifHeight() + progressBarHeight()) * amount()) + (notifPaddingY() * (amount() - 1)).toDouble()
        )
    }

    override fun render(renderer: HudRenderer) {
        val notifications = notifications.toListOrNull(isInEditor)
            ?.takeIf { it.isNotEmpty() } ?: return

        val notificationHeight = notifHeight()
        val titleAlignment = titleAlign()
        val descAlignment = descAlign()
        val notifPaddingY = notifPaddingY()
        val innerNotifPadding = notifTextPaddingX()
        val barHeight = progressBarHeight()
        val timeToDisplay = displayTime()
        val verticalAlign = verticalAlign()
        val radius = getRound()
        val animationDuration = animationDuration().toFloat()

        val titlePaddingX = if (titleAlignment == AlignmentX.Center) innerNotifPadding else innerNotifPadding / 2
        val descPaddingX = if (descAlignment == AlignmentX.Center) innerNotifPadding else innerNotifPadding / 2

        renderer.post {
            val currentTime = System.currentTimeMillis()
            val baseX = x.toDouble()
            val baseY = y.toDouble()
            val roundRenderer = RoundedRenderer2D.normal()
            val textRenderer = TextRenderer.get()

            notifications.forEachIndexed { i, n ->
                val startTime = if (n.startTime != 0L)
                    n.startTime
                else
                    currentTime - (notifications.size - i - 1) * timeToDisplay / amount()
                val notifTime = currentTime - n.startTime

                val x = baseX + (
                    if (n.startTime == 0L)
                        0f
                    else if (notifTime <= animationDuration)
                        (animationDuration - notifTime) * box.width / animationDuration
                    else if (notifTime >= timeToDisplay - animationDuration)
                        box.width - ((timeToDisplay - notifTime) * box.width / animationDuration)
                    else
                        0f
                    )

                val y = baseY + (notificationHeight + barHeight + notifPaddingY) * (
                    if (verticalAlign == VA.Top)
                        i
                    else
                        amount() - i - 1
                    )

                roundRenderer.r2d.begin()

                // background
                if (radius > 0)
                    roundRenderer.quad(x, y, box.width, notificationHeight + barHeight, backgroundColor(), radius)
                else
                    roundRenderer.r2d.quad(
                        x,
                        y,
                        box.width.toDouble(),
                        notificationHeight + barHeight.toDouble(),
                        backgroundColor()
                    )


                // progress bar
                val progress = if (n.persistent)
                    box.width
                else
                    (startTime + timeToDisplay - currentTime) * box.width / timeToDisplay

                if (radius > 0)
                    roundRenderer.quad(x, y + notificationHeight, progress, barHeight, n.color, radius, false)
                else
                    roundRenderer.r2d.quad(
                        x,
                        y + notificationHeight,
                        progress.toDouble(),
                        barHeight.toDouble(),
                        n.color
                    )

                roundRenderer.r2d.render(null)

                val desc = n.description
                val titleHeight = if (desc != null && desc.isNotEmpty())
                    notificationHeight * 0.7
                else notificationHeight.toDouble()

                // title scale
                var scale = min(
                    box.width * (1f - titlePaddingX / 100F) / textRenderer.getLegacyWidth(n.title),
                    titleHeight / textRenderer.height
                )
                if (titleScaling())
                    scale *= titleScale()
                textRenderer.begin(scale, false, true)

                // title
                val titleX = when (titleAlignment) {
                    AlignmentX.Center -> x + box.width / 2 - textRenderer.getLegacyWidth(n.title) / 2
                    AlignmentX.Left -> x + titlePaddingX
                    else -> x + box.width - titlePaddingX - textRenderer.getLegacyWidth(n.title)
                }
                textRenderer.legacyRender(
                    n.title,
                    titleX,
                    y + (titleHeight - textRenderer.height) / 2,
                    AwtColor.WHITE,
                    false
                )
                textRenderer.end()

                if (!desc.isNullOrEmpty()) {
                    // description scale
                    scale = min(
                        box.width * (1f - descPaddingX / 100f) / textRenderer.getLegacyWidth(desc),
                        notificationHeight * 0.25 / textRenderer.height
                    )
                    if (descScaling())
                        scale *= descScale()
                    textRenderer.begin(scale, false, true)

                    val descX = when (descAlignment) {
                        AlignmentX.Center -> x + box.width / 2 - textRenderer.getLegacyWidth(desc) / 2
                        AlignmentX.Left -> x + descPaddingX
                        else -> x + box.width - descPaddingX - textRenderer.getLegacyWidth(desc)
                    }
                    textRenderer.legacyRender(desc, descX, y + titleHeight + (barHeight - textRenderer.height) / 2,
                        AwtColor.WHITE, false)
                    textRenderer.end()
                }
            }
        }
    }

    fun allowsSource(src: NotificationSource?): Boolean = src?.predicate?.invoke(this) ?: false
    fun allowsModule(module: Module?) = module != null && module in modules()
}

enum class NotificationSource(val predicate: (NotificationHud) -> Boolean) {
    Module({ it.moduleSource() }),
    Notifier({ it.notifierSource() }),
    Macro({ it.macroSource() }),
    Command({ it.commandSource() }),
    Default({ true })
}

enum class VA { Top, Bottom }
