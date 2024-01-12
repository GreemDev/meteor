/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.screens

import com.google.gson.GsonBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
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
import net.greemdev.meteor.onFailureOf
import net.greemdev.meteor.util.empty
import net.greemdev.meteor.util.meteor.Meteor
import net.greemdev.meteor.util.scope
import java.nio.file.InvalidPathException
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

private val gson = GsonBuilder().setPrettyPrinting().create()!!

private const val STATUS_LABEL_DEFAULT =
    "Input a file directory. Relative paths are relative to your .minecraft folder."
private const val SCREEN_TITLE = "Import from JourneyMap"
private const val IMPORT_BTN = "Import"

private const val DIR_BOX_PLACEHOLDER = "Insert a directory"
private const val NOT_DIRECTORY = "Path is a file, not a folder/directory"
private const val INVALID_PATH_F = "Path resolution failed: %s"
private const val DATA_FILE_EXTENSION = ".json"

private const val WAYPOINTS_IMPORTED_F = "Successfully imported %s new waypoints from JourneyMap data."
private const val NO_WAYPOINTS_FOUND = "No waypoints found or imported."


class JourneyMapWaypointsImportScreen(theme: GuiTheme) : WindowScreen(theme, SCREEN_TITLE) {

    private lateinit var statusLabel: WLabel
    private var labelResetJob: Job? = null

    override fun initWidgets() {
        statusLabel = add(theme.label(STATUS_LABEL_DEFAULT)).expandX().widget()
        add(theme.horizontalSeparator()).expandX()
        val textBox = add(theme.textBox(String.empty, DIR_BOX_PLACEHOLDER)).expandX().widget()
        add(theme.horizontalSeparator()).expandX()
        add(theme.button(IMPORT_BTN) {
            val dir = runCatching {
                Path(textBox.get()).toFile()
            }.onFailureOf(InvalidPathException::class) {
                statusLabel.set(theme.badColor(), INVALID_PATH_F.format(it.reason))
                return@button
            }.getOrThrow()

            if (dir.isFile) {
                statusLabel.set(theme.badColor(), NOT_DIRECTORY)
                return@button
            }

            val newWaypoints = dir.filter {
                it.isFile and it.name.endsWith(DATA_FILE_EXTENSION)
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
            }

            Meteor.waypoints().addAll(newWaypoints).also { imported ->
                if (imported > 0)
                    statusLabel.set(Color.GREEN, WAYPOINTS_IMPORTED_F.format(imported))
                else
                    statusLabel.set(Color.ORANGE, NO_WAYPOINTS_FOUND)
            }

            labelResetJob = scope.launch {
                delay(6.5.seconds)
                if (statusLabel() != STATUS_LABEL_DEFAULT)
                    statusLabel.set(theme.textColor(), STATUS_LABEL_DEFAULT)
                labelResetJob = null
            }
        })
    }

    override fun onClosed() {
        labelResetJob?.cancel("Screen closed.")
        labelResetJob = null
    }

    @Suppress("unused") //JSON object
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
