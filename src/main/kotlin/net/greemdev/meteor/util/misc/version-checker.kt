/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.PreInit
import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.text.ChatColor
import java.util.Optional
import kotlin.time.Duration.Companion.hours

private const val UpstreamGradleProperties = "https://raw.githubusercontent.com/GreemDev/meteor/1.20.1/gradle.properties"

object GVersioning {
    private lateinit var revisionChecker: Job

    @JvmStatic
    @PreInit
    fun init() {
        revisionChecker = scope.launch {
            updateRevision()

            while (isActive) {
                delay(1.hours)
                updateRevision()
            }
        }
    }


    private suspend fun updateRevision() {
        latestRevision = (HTTP GET UpstreamGradleProperties)
            .requestLinesAsync() thenMap { resp ->
                resp.orEmpty()
                    .find { it.startsWith("revision") }
                    .mapNullable { it.substringAfterLast('=').toIntOrNull() }
                    .orElse(-1)
            }
    }

    @JvmStatic
    @get:JvmName("latestRevision")
    var latestRevision: Int = -1
        private set

    @JvmStatic
    @get:JvmName("revisionsBehind")
    val revisionsBehind by invoking {
        if (latestRevision == -1)
            0 //if revision can't be determined, this will make the title screen info say "up to date"
        else
            latestRevision - MeteorClient.REVISION
    }

    fun getRevisionsBehindAndColor(): Pair<Int, MeteorColor> = revisionsBehind.let {
        it to it.let {
            when {
                it >= 15 -> ChatColor.darkRed.meteor.darker().darker()
                it >= 10 -> ChatColor.darkRed.meteor.darker()
                it >=  5 -> ChatColor.darkRed.meteor
                else     -> ChatColor.red.meteor
            }
        }
    }



    @JvmStatic
    @get:JvmName("isOutdated")
    val isOutdated by invoking { revisionsBehind > 0 }

    @JvmStatic
    @get:JvmName("isUpToDate")
    val isUpToDate by invoking { revisionsBehind == 0 }
}
