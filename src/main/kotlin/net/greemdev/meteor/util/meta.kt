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
import meteordevelopment.meteorclient.utils.player.ChatUtils
import net.minecraft.entity.player.PlayerEntity
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.message.MessageFactory
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.util.*
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.primaryConstructor


fun<T> getOrNull(func: () -> T): T? = try { func() } catch (t: Throwable) { null }
fun<T> supplyOrNull(func: Supplier<T>): T? = try { func.get() } catch (t: Throwable) { null }

fun<T> tryOrIgnore(func: () -> Unit) = try { func() } catch (ignored: Throwable) { }
fun<T> runOrIgnore(runnable: Runnable) = try { runnable.run() } catch (ignored: Throwable) { }

/**
 * Looks repetitive however each different type we check for has its own special logic in [LogManager]
 */
fun log4j(value: Any): ReadOnlyProperty<Any, Logger> = invoking {
    when (value) {
        is String -> LogManager.getLogger(value)
        is Class<*> -> LogManager.getLogger(value)
        is KClass<*> -> LogManager.getLogger(value.java)
        is MessageFactory -> LogManager.getLogger(value)
        else -> LogManager.getLogger(value)
    }
}

fun<R : Comparable<R>> List<Module>.sortModulesBy(
    sorted: Boolean = true,
    isAscending: Boolean = true,
    sorter: (Module) -> R?
): List<Module> =
    toMutableList().apply {
        if (sorted) {
            if (isAscending)
                sortBy(sorter)
            else
                sortByDescending(sorter)
        }
    }

fun<T : Any> optionalOf(value: T? = null): Optional<T> = if (value == null) Optional.empty() else Optional.of(value)

fun <T> invoking(func: () -> T): FunctionProperty<T> = FunctionProperty(func)
fun <T> invokingOrNull(func: () -> T): FunctionProperty<T?> = FunctionProperty{getOrNull(func)}

class FunctionProperty<T>(private val producer: () -> T): ReadOnlyProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>) = producer()
}

fun StringSetting.Builder.renderStarscript(): StringSetting.Builder = renderer(StarscriptTextBoxRenderer::class.java)
fun StringListSetting.Builder.renderStarscript(): StringListSetting.Builder =
    renderer(StarscriptTextBoxRenderer::class.java)

fun <P1, P2> Collection<Pair<P1, P2>>.asMap() = associate { it.first to it.second }

fun IntSetting.Builder.saneSlider(): IntSetting.Builder = sliderRange(min, max)

inline fun <reified T> javaSubtypesOf(pkg: String): Set<Class<out T>> =
    Reflections(
        ConfigurationBuilder()
            .forPackage(pkg)
            .addScanners(Scanners.SubTypes)
    ).getSubTypesOf(T::class.java)

inline fun <reified T : Any> subtypesOf(pkg: String): List<KClass<out T>> =
    javaSubtypesOf<T>(pkg).map { it.kotlin }

inline fun <reified T : Any> createSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        it.primaryConstructor?.call()
    }

infix fun <T : WPressable> T.action(func: (T) -> Unit): T = action { func(this) }

fun String.ensurePrefix(prefix: String): String {
    return if (startsWith(prefix))
        this
    else "$prefix$this"
}

fun String.ensureSuffix(suffix: String): String {
    return if (endsWith(suffix))
        this
    else "$this$suffix"
}

inline fun <reified T> forceNextChatPrefix() = ChatUtils.forceNextPrefixClass(T::class.java)

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
