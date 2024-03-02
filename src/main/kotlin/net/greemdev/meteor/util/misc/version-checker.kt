/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.misc

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.PreInit
import net.greemdev.meteor.*
import net.greemdev.meteor.util.*
import net.greemdev.meteor.util.text.ChatColor
import kotlin.time.Duration.Companion.hours

private const val UpstreamGradleProperties = "https://raw.githubusercontent.com/GreemDev/meteor/1.20.1/gradle.properties"

object GVersioning {

    @get:JvmStatic
    @get:JvmName("revisionChecker")
    lateinit var revisionChecker: DisposableCoroutine<Job>
        private set

    @JvmStatic
    @PreInit
    fun init() {
        revisionChecker = scope.newJobBuilder()
            .whenError { Greteor.logger.error("Error in revision checking coroutine", it) }
            .executing {
                updateRevision()

                while (isActive) {
                    delay(1.hours)
                    updateRevision()
                }
            }
    }


    private suspend fun updateRevision() {
        fun versioning(message: String) = "[Versioning] $message"
        (HTTP GET UpstreamGradleProperties)
            .requestLinesAsync() thenAccept  { resp ->
            if (resp == null) {
                Greteor debug versioning("Received no response from '$UpstreamGradleProperties'.")
                return
            }

            val revisionLine = resp.find("revision"::isStartOf)
            if (revisionLine.isEmpty) {
                Greteor debug versioning("Could not find revision line in response.")
                return
            }

            val revision = revisionLine.map { it.substringAfterLast('=').toIntOrNull() }

            if (revision.isEmpty) {
                Greteor debug versioning("Could not parse detected revision string.")
                return
            }

            latestRevision = revision.get()
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
                else   -> ChatColor.red.meteor
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
