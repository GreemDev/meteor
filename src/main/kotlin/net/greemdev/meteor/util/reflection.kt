/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Reflection")

package net.greemdev.meteor.util

import net.greemdev.meteor.*
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.Field
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

@get:JvmName("cache")
val reflections = mutableMapOf<String, Reflections>()

inline fun <reified T> javaSubtypesOf(pkg: String): Set<Class<out T>> =
    reflectionCached(pkg) {
        addScanners(Scanners.SubTypes)
    }.getSubTypesOf(T::class.java)

@JvmName("streamSubtypes")
fun<T> javaSubtypesOf(clazz: Class<T>, pkg: String): Stream<Class<out T>> =
    reflectionCached(pkg) {
        addScanners(Scanners.SubTypes)
    }.getSubTypesOf(clazz)
        .stream()

inline fun <reified T : Any> subtypesOf(pkg: String): Set<KClass<out T>> =
    javaSubtypesOf<T>(pkg).map { it.kotlin }.toSet()

inline fun <reified T : Any> createSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        getOrNull { it.primaryConstructor?.call() }
    }

inline fun <reified T : Any> findInstancesOfSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull { getOrNull(it::findInstance) }

@JvmName("ktReflection")
inline fun reflection(crossinline cb: Initializer<ConfigurationBuilder>) = Reflections(ConfigurationBuilder().apply(cb))

@JvmName("ktReflectionCached")
inline fun reflectionCached(pkg: String, crossinline cb: Initializer<ConfigurationBuilder>) =
    reflections.getOrPut(pkg) {
        reflection {
            forPackage(pkg)
            cb()
        }
    }

@JvmName("create")
fun `java-reflection`(cb: Consumer<ConfigurationBuilder>) = reflection(cb.kotlin)

@JvmName("createCached")
fun `java-reflectionCached`(pkg: String, cb: Consumer<ConfigurationBuilder>) = reflectionCached(pkg, cb.kotlin)

inline fun<reified T> className() = T::class.simpleName
inline fun<reified T> qualifiedName() = T::class.qualifiedName

fun<T> Class<T>.callNoArgsConstructor(): T? =
    runCatching {
        getConstructor().newInstance()
    }.onFailureOf(NoSuchMethodException::class) { nse ->
        Greteor.logger.error("Specified type '$qualifiedNameOrMappingName' does not have a no-args constructor.", nse)
    }.getOrNull()

fun<T> Class<T>.callConstructor(vararg _args: Any): T? {
    val (argTypes, args) = if (_args.isNotEmpty())
        _args.map {
            it::class.java to it
        }.let { argMap ->
            Pair(argMap.map { it.first }, argMap.map { it.second })
        }
    else
        listOf<Class<out Any>>() to listOf()

    return runCatching {
        getConstructor(*argTypes.toTypedArray()).newInstance(args.toTypedArray())
    }.onFailureOf(NoSuchMethodException::class) { nse ->
        val ctorSignature = argTypes.joinToString(", ") { it.qualifiedNameOrMappingName }

        Greteor.logger.error("Specified type '$qualifiedNameOrMappingName' does not have a constructor matching ($ctorSignature).", nse)
    }.getOrNull()
}

@Suppress("UNCHECKED_CAST")
fun<T> Field.tryGet(instance: Any? = null) = getOrNull { get(instance) as? T }
