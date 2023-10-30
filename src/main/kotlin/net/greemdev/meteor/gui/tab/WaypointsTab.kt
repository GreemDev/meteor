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
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.*
import net.greemdev.meteor.util.minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.nbt.NbtIo
import java.io.File

object WaypointsTab : Tab("Waypoints", GuiRenderer.WAYPOINTS, Meteor.config().waypointsIcon::get) {
    override fun createScreen(theme: GuiTheme): TabScreen = WorldListScreen(theme, this)
    override fun isScreen(screen: Screen) = screen is WorldListScreen
}

private class WorldListScreen(theme: GuiTheme, tab: Tab) : WindowTabScreen(theme, tab) {
    override fun initWidgets() {
        add(theme.table()) { cell, table ->
            cell.expandX().minWidth(300.0)

            val folder = MeteorClient.FOLDER / "waypoints"
            val files = folder.filter {
                it.isFile && it.name.endsWith(".nbt")
            }

            if (folder.exists() && folder.isDirectory && !files.isNullOrEmpty()) {
                files.forEach {
                    if (it == Meteor.waypoints().file) return@forEach
                    val nameLabel = table.add(theme.label(it.name.removeSuffix(".nbt"))).expandX().widget()
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    table.add(theme.button("View") {
                        runCatching {
                            minecraft.setScreen(ListScreen(this, theme, it))
                        }.onFailure {
                            nameLabel.color(MeteorColor.RED).append(" ERR")
                        }
                    })
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    val minus = table.add(theme.minus {
                        it.delete()
                        reload()
                    }).widget()
                    minus.tooltip = "Delete the waypoints file for this world."
                    table.row()
                }
            } else
                table.add(theme.label("No waypoint files."))
        }
    }
}

private class ListScreen(
    parent: WorldListScreen,
    theme: GuiTheme,
    private val file: File,
    private val wp: Waypoints = Waypoints().fromTag(NbtIo.read(file))
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
            add(theme.horizontalList()) { cell, hl ->
                cell.expandX()
                hl.add(theme.button("Save") {
                    NbtIo.write(wp.toTag(), file)
                }).expandX()
                hl.add(theme.button("Delete All") {
                    val prompt = confirm("delete-all-waypoints") {
                        title("Are you sure?")
                        message("This is a destructive and irreversible action. Are you sure you want to proceed?")
                        onYes {
                            file.delete()
                            close()
                        }
                    }

                    if (!prompt.show()) {
                        file.delete()
                        close()
                    }
                }).expandX()
            }
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

