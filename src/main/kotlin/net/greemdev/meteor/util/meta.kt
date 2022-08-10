/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Util")

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable
import meteordevelopment.meteorclient.settings.*
import meteordevelopment.meteorclient.systems.accounts.*
import meteordevelopment.meteorclient.systems.commands.*
import meteordevelopment.meteorclient.systems.config.Config
import meteordevelopment.meteorclient.systems.friends.*
import meteordevelopment.meteorclient.systems.hud.Hud
import meteordevelopment.meteorclient.systems.macros.*
import meteordevelopment.meteorclient.systems.modules.*
import meteordevelopment.meteorclient.systems.profiles.*
import meteordevelopment.meteorclient.systems.proxies.Proxies
import meteordevelopment.meteorclient.systems.waypoints.*
import net.minecraft.entity.player.PlayerEntity
import java.util.UUID


fun StringSetting.Builder.renderStarscript(): StringSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)
fun StringListSetting.Builder.renderStarscript(): StringListSetting.Builder =
    renderer(StarscriptTextBoxRenderer::class.java)

fun<P1, P2> Collection<Pair<P1, P2>>.asMap() = associate { it.first to it.second }

fun IntSetting.Builder.saneSlider(): IntSetting.Builder = sliderRange(min, max)
infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

object Meteor {

    @JvmStatic
    fun config(): Config = Config.get()

    @JvmStatic
    fun accounts(): Accounts = Accounts.get()

    @JvmStatic
    fun macros(): Macros = Macros.get()

    @JvmStatic
    fun proxies(): Proxies = Proxies.get()

    @JvmStatic
    fun hud(): Hud = Hud.get()

    @JvmStatic
    fun modules(): Modules = Modules.get()

    @JvmStatic
    fun commands(): Commands = Commands.get()

    @JvmStatic
    fun friends(): Friends = Friends.get()

    @JvmStatic
    fun waypoints(): Waypoints = Waypoints.get()

    @JvmStatic
    fun profiles(): Profiles = Profiles.get()

    @JvmStatic
    inline fun <reified T : Module> module(): T = modules().get(T::class.java)

    @JvmStatic
    inline fun <reified T : Command> command(): T = commands().get(T::class.java)

    @JvmStatic
    fun friend(uuid: UUID): Friend? = friends().get(uuid)

    @JvmStatic
    fun friend(player: PlayerEntity): Friend? = friends().get(player)

    @JvmStatic
    fun waypoint(name: String): Waypoint? = waypoints().get(name)

    @JvmStatic
    fun profile(name: String): Profile? = profiles().get(name)
}
