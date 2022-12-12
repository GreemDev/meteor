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
            .map { it.substringAfterLast('=') }
            .findFirst()
            .map(String::toInt)
            .orElseThrow()
    }

    @JvmStatic
    var latestRevision: Int = -1
        private set

    @JvmStatic
    fun revisionsBehind() =
        if (latestRevision == -1)
            0
        else
            latestRevision - MeteorClient.REVISION


    @JvmStatic
    fun isOutdated() = revisionsBehind() > 0

    @JvmStatic
    fun isUpToDate() = revisionsBehind() == 0
}
