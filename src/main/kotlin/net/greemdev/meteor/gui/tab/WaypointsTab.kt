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
import meteordevelopment.meteorclient.gui.widgets.WWidget
import meteordevelopment.meteorclient.gui.widgets.containers.WTable
import meteordevelopment.meteorclient.systems.waypoints.Waypoint
import meteordevelopment.meteorclient.systems.waypoints.Waypoints
import net.greemdev.meteor.util.meteor.*
import net.greemdev.meteor.util.*
import net.minecraft.client.gui.screen.Screen
import net.minecraft.nbt.NbtIo
import java.io.File
import java.io.FileFilter

object WaypointsTab : Tab("Waypoints", GuiRenderer.WAYPOINTS, Meteor.config().waypointsIcon::get) {
    override fun createScreen(theme: GuiTheme): TabScreen = WorldListScreen(theme, this)
    override fun isScreen(screen: Screen) = screen is WorldListScreen
}

private class WorldListScreen(theme: GuiTheme, tab: Tab) : WindowTabScreen(theme, tab) {
    override fun initWidgets() {
        within(add(theme.table()).expandX().minWidth(300.0)) { table ->
            val folder = MeteorClient.FOLDER / "waypoints"
            val files = folder.listFiles(FileFilter {
                it.isFile && it.name.endsWith(".nbt")
            })

            if (folder.exists() && folder.isDirectory && !files.isNullOrEmpty()) {
                files.forEach {
                    if (it == Meteor.waypoints().file) return@forEach
                    val nameLabel = table.add(theme.label(it.name.removeSuffix(".nbt"))).expandX().widget()
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    table.add(theme.button("View") {
                        getOrNull {
                            minecraft.setScreen(ListScreen(this, theme, it))
                        } ?: nameLabel.set("${nameLabel.get()} ERR")
                    })
                    table.add(theme.verticalSeparator()).expandWidgetY()
                    table.add(theme.minus {
                        it.delete()
                        reload()
                    })
                    table.row()
                }
            } else {
                table.add(theme.label("No Waypoints"))
            }
        }
    }
}

private class ListScreen(
    parent: WorldListScreen,
    theme: GuiTheme,
    private val file: File,
    private val wp: Waypoints = Waypoints().fromTag(NbtIo.read(file))
) : WindowScreen(theme, file.name.withoutSuffix(".nbt")) {

    init {
        this.parent = parent
    }

    override fun onClosed() {
        reloadParent()
    }

    override fun initWidgets() {
        within(add(theme.table())) { it.fill() }
        add(theme.horizontalSeparator()).expandX()
        within(add(theme.horizontalList()).expandX()) {
            it.add(theme.button("Save") {
                NbtIo.write(wp.toTag(), file)
            }).expandX()
            it.add(theme.button("Delete All") {

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
    }

    private fun WTable.fill() {
        clear()
        wp.forEach {
            add(theme.waypointIcon(it))
            add(theme.label(it.name()))
            add(theme.verticalSeparator()).expandWidgetY()
            add(theme.label(it.dimension().name))
            add(theme.verticalSeparator()).expandWidgetY()
            val (x, y, z) = it.rawPos()
            add(theme.label("($x, $y, $z)"))
            add(theme.verticalSeparator()).expandWidgetY()
            add(theme.minus {
                wp.remove(it)
                fill()
            })

            row()
        }
    }
}

