/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.screens

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.WindowScreen
import meteordevelopment.meteorclient.gui.widgets.WLabel
import meteordevelopment.meteorclient.systems.waypoints.Waypoint
import meteordevelopment.meteorclient.systems.waypoints.Waypoints
import meteordevelopment.meteorclient.utils.render.color.Color
import meteordevelopment.meteorclient.utils.render.color.SettingColor
import meteordevelopment.meteorclient.utils.world.Dimension
import net.greemdev.meteor.filter
import net.greemdev.meteor.getOrNull
import net.greemdev.meteor.invoke
import net.greemdev.meteor.util.scope
import java.nio.file.InvalidPathException
import java.util.concurrent.CancellationException
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

private val gson = GsonBuilder().setPrettyPrinting().create()

private const val STATUS_LABEL_DEFAULT =
    "Input a file directory. Relative paths are relative to your .minecraft folder."

class JourneyMapWaypointsImportScreen(theme: GuiTheme) : WindowScreen(theme, "Import from JourneyMap") {

    private lateinit var statusLabel: WLabel
    private var labelResetJob: Job? = null

    override fun initWidgets() {
        statusLabel = add(theme.label(STATUS_LABEL_DEFAULT)).expandX().widget()
        add(theme.horizontalSeparator()).expandX()
        val textBox = add(theme.textBox("", "Insert a directory")).expandX().widget()
        add(theme.horizontalSeparator()).expandX()
        add(theme.button("Import") {
            val dir = runCatching {
                Path(textBox.get()).toFile()
            }.onFailure {
                if (it is InvalidPathException)
                    statusLabel.color(Color.RED).set("Path resolution failed: ${it.reason}")
                return@button
            }.getOrThrow()

            if (dir.isFile) {
                statusLabel.color(Color.RED).set("Path is a file, not a folder/directory")
                return@button
            }

            dir.filter {
                it.name.endsWith(".json") && it.isFile
            }!!.mapNotNull map@{
                val content = getOrNull { it.readText() } ?: return@map null
                val jmWaypoint = getOrNull { gson.fromJson(content, JourneyMapWaypointData::class.java) }
                    ?: return@map null //ignore malformed data

                Waypoint.Builder()
                    .name(jmWaypoint.name)
                    .dimension(
                        when (jmWaypoint.dimensions.firstOrNull()) {
                            "minecraft:overworld" -> Dimension.Overworld
                            "minecraft:the_nether" -> Dimension.Nether
                            "minecraft:the_end" -> Dimension.End
                            else -> return@map null
                        }
                    )
                    .pos(jmWaypoint.x, jmWaypoint.y, jmWaypoint.z)
                    .build()
                    .apply {
                        opposite.set(false)
                        color.set(SettingColor(jmWaypoint.r, jmWaypoint.g, jmWaypoint.b))
                    }
            }.also {
                statusLabel.apply {
                    if (Waypoints.get().addAll(it) != 0)
                        color(Color.GREEN).set("Successfully imported ${it.size} new waypoints from JourneyMap data.")
                    else
                        color(Color.ORANGE).set("No waypoints found or imported.")
                }
            }


            labelResetJob = scope.launch {
                delay(5.seconds)
                if (statusLabel() != STATUS_LABEL_DEFAULT)
                    statusLabel.color(theme.textColor()).set(STATUS_LABEL_DEFAULT)
                labelResetJob = null
            }
        })
    }

    override fun onClosed() {
        labelResetJob?.cancel(CancellationException("Screen closed."))
    }

    private inner class JourneyMapWaypointData(
        @JvmField
        val id: String,
        @JvmField
        val name: String,
        @JvmField
        val icon: String,
        @JvmField
        val colorizedIcon: String,
        @JvmField
        val x: Int,
        @JvmField
        val y: Int,
        @JvmField
        val z: Int,
        @JvmField
        val r: Int,
        @JvmField
        val g: Int,
        @JvmField
        val b: Int,
        @JvmField
        val enable: Boolean,
        @JvmField
        val type: String,
        @JvmField
        val origin: String,
        @JvmField
        val dimensions: Array<String>,
        @JvmField
        val persistent: Boolean,
        @JvmField
        val showDeviation: Boolean,
        @JvmField
        val iconColor: Int,
        @JvmField
        val customIconColor: Boolean
    )
}
