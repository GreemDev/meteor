/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.modules.presence

typealias CustomStates = MutableMap<String, String>

fun CustomStates.getStateOrNull(className: String): String? {
    return filter { className.startsWith(it.key) }.firstNotNullOfOrNull { it.value }
}

fun CustomStates.register(packageName: String, state: String) {
    this[packageName] = state
}

fun CustomStates.unregister(packageName: String): Boolean {
    return this.remove(packageName) != null
}
