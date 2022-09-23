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
import net.minecraft.entity.player.PlayerEntity
import java.util.*

object Meteor {

    @JvmStatic
    fun currentTheme(): GuiTheme = GuiThemes.get()

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
    fun friend(name: String, ignoreCase: Boolean = false): Friend? = friends().get(name, ignoreCase)

    @JvmStatic
    fun friend(player: PlayerEntity): Friend? = friends().get(player)

    @JvmStatic
    fun waypoint(name: String): Waypoint? = waypoints().get(name)

    @JvmStatic
    fun profile(name: String): Profile? = profiles().get(name)
}
