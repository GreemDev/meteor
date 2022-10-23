/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.network.Http

object GVersioning {
    @JvmStatic
    fun loadLatestRevision() {
        val response = Http.get("https://raw.githubusercontent.com/GreemDev/meteor/main/gradle.properties").sendLines()
        latestRevision = response
            .filter { it.startsWith("revision") }
            .map { it.split("=").last() }
            .findFirst()
            .map(String::toInt)
            .orElseThrow()
    }

    @JvmStatic
    var latestRevision: Int = -1
        private set

    @JvmStatic
    fun revisionsBehind(): Int {
        return (latestRevision.takeUnless { it == -1 } ?: return 0) - MeteorClient.REVISION
    }

    @JvmStatic
    fun isOutdated() = revisionsBehind() > 0

    @JvmStatic
    fun isUpToDate() = revisionsBehind() == 0
}
