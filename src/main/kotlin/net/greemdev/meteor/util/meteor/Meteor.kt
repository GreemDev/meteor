/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.GuiThemes
import meteordevelopment.meteorclient.systems.accounts.Accounts
import meteordevelopment.meteorclient.systems.commands.Command
import meteordevelopment.meteorclient.systems.commands.Commands
import meteordevelopment.meteorclient.systems.config.Config
import meteordevelopment.meteorclient.systems.friends.Friend
import meteordevelopment.meteorclient.systems.friends.Friends
import meteordevelopment.meteorclient.systems.hud.Hud
import meteordevelopment.meteorclient.systems.macros.Macros
import meteordevelopment.meteorclient.systems.modules.Module
import meteordevelopment.meteorclient.systems.modules.Modules
import meteordevelopment.meteorclient.systems.profiles.Profile
import meteordevelopment.meteorclient.systems.profiles.Profiles
import meteordevelopment.meteorclient.systems.proxies.Proxies
import meteordevelopment.meteorclient.systems.waypoints.Waypoint
import meteordevelopment.meteorclient.systems.waypoints.Waypoints
import net.greemdev.meteor.kotlin
import net.minecraft.entity.player.PlayerEntity
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.KClass

object Meteor {

    @JvmStatic
    fun currentTheme(): GuiTheme = GuiThemes.get() ?: error("Themes system not yet available.")

    @JvmStatic
    fun config(): Config = Config.get() ?: error("Config system not yet available.")

    @JvmStatic
    fun accounts(): Accounts = Accounts.get() ?: error("Accounts system not yet available.")

    @JvmStatic
    fun macros(): Macros = Macros.get() ?: error("Macros system not yet available.")

    @JvmStatic
    fun proxies(): Proxies = Proxies.get() ?: error("Proxies system not yet available.")

    @JvmStatic
    fun hud(): Hud = Hud.get() ?: error("HUD system not yet available.")

    @JvmStatic
    fun modules(): Modules = Modules.get() ?: error("Modules system not yet available.")

    @JvmStatic
    fun commands(): Commands = Commands.get() ?: error("Commands system not yet available.")

    @JvmStatic
    fun friends(): Friends = Friends.get() ?: error("Friends system not yet available.")

    @JvmStatic
    fun waypoints(): Waypoints = Waypoints.get() ?: error("Waypoints system not yet available.")

    @JvmStatic
    fun profiles(): Profiles = Profiles.get() ?: error("Profiles system not yet available.")

    @JvmStatic
    fun module(name: String): Module = modules().get(name)
    inline fun <reified T : Module> module(): T = modules().get(T::class.java)
    inline fun <reified T : Module> module(func: T.() -> Unit) = modules().get(T::class.java).func()

    @JvmStatic
    fun <T : Module> module(moduleClass: Class<T>, func: Consumer<T>) = func.kotlin(module(moduleClass))
    @JvmStatic
    fun <T : Module> module(moduleClass: Class<T>): T = modules().get(moduleClass)

    inline fun <reified T : Command> command(): T = commands().get(T::class.java)

    @JvmStatic
    fun friend(name: String, ignoreCase: Boolean = false): Friend? = friends().get(name, ignoreCase)

    @JvmStatic
    fun friend(player: PlayerEntity): Friend? = friends().get(player)

    @JvmStatic
    fun waypoint(name: String): Waypoint? = waypoints().get(name)

    @JvmStatic
    fun profile(name: String): Profile? = profiles().get(name)
}
