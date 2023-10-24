/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import meteordevelopment.meteorclient.MeteorClient
import net.greemdev.meteor.findFirst
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.Http

object GVersioning {

    const val upstreamGradleProperties = "https://raw.githubusercontent.com/GreemDev/meteor/main/gradle.properties"

    @JvmStatic
    fun loadLatestRevision() {
        latestRevision = Http.get(upstreamGradleProperties).requestLines().orEmpty()
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
    @get:JvmName("revisionsBehind")
    val revisionsBehind by invoking {
        if (latestRevision == -1)
            0
        else
            latestRevision - MeteorClient.REVISION
    }



    @JvmStatic
    @get:JvmName("isOutdated")
    val isOutdated by invoking { revisionsBehind > 0 }

    @JvmStatic
    @get:JvmName("isUpToDate")
    val isUpToDate by invoking { revisionsBehind == 0 }
}
