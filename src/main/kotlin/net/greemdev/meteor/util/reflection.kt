/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util

import net.greemdev.meteor.Initializer
import net.greemdev.meteor.findInstance
import net.greemdev.meteor.getOrNull
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

inline fun <reified T> javaSubtypesOf(pkg: String): Set<Class<out T>> =
    buildReflections {
        forPackage(pkg)
        addScanners(Scanners.SubTypes)
    }.getSubTypesOf(T::class.java)

inline fun <reified T : Any> subtypesOf(pkg: String): List<KClass<out T>> =
    javaSubtypesOf<T>(pkg).map { it.kotlin }

inline fun <reified T : Any> createSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        getOrNull { it.primaryConstructor?.call() }
    }

inline fun <reified T : Any> findInstancesOfSubtypesOf(pkg: String): List<T> =
    subtypesOf<T>(pkg).mapNotNull {
        getOrNull { it.findInstance() }
    }

inline fun buildReflections(crossinline cb: Initializer<ConfigurationBuilder>) = Reflections(ConfigurationBuilder().apply(cb))

inline fun<reified T> className() = T::class.simpleName
inline fun<reified T> qualifiedName() = T::class.qualifiedName
