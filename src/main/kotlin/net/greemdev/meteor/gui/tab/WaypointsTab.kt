/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.tab

import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.gui.GuiTheme
import meteordevelopment.meteorclient.gui.WindowScreen
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer
import meteordevelopment.meteorclient.gui.tabs.Tab
import meteordevelopment.meteorclient.gui.tabs.TabScreen
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen
import meteordevelopment.meteorclient.gui.widgets.containers.WTable
import meteordevelopment.meteorclient.systems.waypoints.Waypoints
import meteordevelopment.meteorclient.utils.Utils
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.*
import net.greemdev.meteor.util.minecraft
import net.greemdev.meteor.util.misc.readNbt
import net.greemdev.meteor.util.misc.write
import net.minecraft.client.gui.screen.Screen
import java.io.File
import kotlin.math.round

class WaypointsTab : Tab(NAME, GuiRenderer.WAYPOINTS, Meteor.config().waypointsIcon) {
    override fun createScreen(theme: GuiTheme): TabScreen = WorldListScreen(theme, this)
    override fun isScreen(screen: Screen) = screen is WorldListScreen

    companion object {
        const val NAME = "Waypoints"
    }
}

private class WorldListScreen(theme: GuiTheme, tab: Tab) : WindowTabScreen(theme, tab) {
    override fun initWidgets() {
        add(theme.table { table ->
            val folder = MeteorClient.FOLDER / "waypoints"
            val files = folder.filter {
                it.isFile && it.name.endsWith(".nbt") && it != Meteor.waypoints().file
            }

            //File#filter returns null if the file isn't a directory, so this is basically an existence, isDirectory, and files isNotEmpty check all in one
            if (!files.isNullOrEmpty()) {
                files.forEach {
                    val nameLabel = table.add(theme.label(it.name.removeSuffix(".nbt"))).expandX().widget()
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    table.add(theme.button("View") {
                        runCatching {
                            minecraft.setScreen(WaypointListScreen(this, theme, it))
                        }.onFailure { err ->
                            nameLabel.color(MeteorColor.RED).append(" ERR")
                            Greteor.logger.catching(err)
                        }
                    })
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    table.add(theme.minus {
                        it.delete()
                        reload()
                    }).widget()
                        .tooltip = "Delete the waypoints file for this world."
                    table.row()
                }
            } else
                table.add(theme.label("No waypoint files."))
        }).expandX().minWidth(
            Utils.getWindowWidth() / (GuiTheme.getWidthDivisor() * 2.818182)
        ) /* default width divisor is 2.2 and 2.2 * 2.818182 is roughly equivalent to the previous hardcoded 6.2 divisor here*/
    }
}

private class WaypointListScreen(
    parent: WorldListScreen,
    theme: GuiTheme,
    private val file: File,
    private val wp: Waypoints = Waypoints().fromTag(file.readNbt())
) : WindowScreen(theme, file.name.removeSuffix(".nbt")) {

    init {
        this.parent = parent
    }

    override fun onClosed() {
        reloadParent()
    }

    override fun initWidgets() {
        if (!wp.isEmpty) {
            add(theme.table { it.fill() })
            add(theme.horizontalSeparator()).expandX()
            add(theme.horizontalList { hl ->
                hl.add(theme.button("Save") {
                    file.write(wp.toTag())?.also {
                        Greteor.logger.error("Error occurred writing waypoint data to file ${file.absolutePath}", it)
                    }
                }).expandX()
                hl.add(theme.button("Delete All") {
                    showConfirm("delete-all-waypoints") {
                        title("Are you sure?")
                        message("This is a destructive and irreversible action. Are you sure you want to proceed?")
                        displayRequired(true)
                        onYes {
                            file.delete()
                            close()
                        }
                    }
                }).expandX()
            }).expandX()
        } else
            add(theme.label("This world has no waypoints."))
    }

    private fun WTable.fill() {
        clear()
        wp.forEach {
            add(theme.waypointIcon(it))
            add(theme.label(it.name()))
            add(theme.verticalSeparator()).expandWidgetY()
            add(theme.label(it.dimension().name))
            add(theme.verticalSeparator()).expandWidgetY()
            it.xyz().also { (x, y, z) ->
                add(theme.label("($x, $y, $z)"))
            }
            add(theme.verticalSeparator()).expandWidgetY()
            add(theme.minus {
                wp.remove(it)
                fill()
            })

            row()
        }
    }
}
