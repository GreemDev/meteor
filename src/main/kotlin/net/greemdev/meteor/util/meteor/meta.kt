/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.gui.tabs.Tab
import meteordevelopment.meteorclient.gui.tabs.Tabs
import meteordevelopment.meteorclient.utils.network.Http

//Config is added at the end to ensure it's always at the very right of the top bar
fun List<Tab>.renderable(): Pair<List<Tab>, List<Tab>> =
    filter { !it.displayIcon.get() } to
        (filter { it.displayIcon.get() && it.name != "Config" } + Tabs.config())

@Suppress("EXTENSION_SHADOWED_BY_MEMBER") //kotlin-only type-parameter only inline fun, is not shadowed
inline fun <reified T> Http.Request.sendJson(): T? = sendJson<T>(T::class.java)
