/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.PostInit
import meteordevelopment.meteorclient.utils.PreInit
import net.greemdev.meteor.findFirst
import net.greemdev.meteor.invoking
import net.greemdev.meteor.optionalOf
import net.greemdev.meteor.util.HTTP
import net.greemdev.meteor.util.scope
import kotlin.time.Duration.Companion.hours

private const val upstreamGradleProperties = "https://raw.githubusercontent.com/GreemDev/meteor/1.20.1/gradle.properties"

object GVersioning {
    private var revisionChecker: Job? = null
    @JvmStatic
    @PreInit
    fun init() {
        scope.launch { reloadLatestRevision() }
        revisionChecker = scope.launch {
            while (true) {
                reloadLatestRevision()
                delay(1.hours)
            }
        }
    }


    suspend fun reloadLatestRevision() {
        latestRevision = (HTTP GET upstreamGradleProperties)
            .requestLinesAsync().await()
            .orEmpty()
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
