/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.type

enum class StringComparisonType {
    Equals,
    Contains,
    StartsWith,
    EndsWith;

    fun compare(base: String, to: String, ignoreCase: Boolean = true) = when (this) {
        Equals -> base.equals(to, ignoreCase)
        Contains -> base.contains(to, ignoreCase)
        StartsWith -> base.startsWith(to, ignoreCase)
        EndsWith -> base.endsWith(to, ignoreCase)
    }

    override fun toString() = when (this) {
        Equals -> "Equals"
        Contains -> "Contains"
        StartsWith -> "Starts With"
        EndsWith -> "Ends With"
    }
}
