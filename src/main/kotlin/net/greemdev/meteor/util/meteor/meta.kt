/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.util.meteor

import meteordevelopment.meteorclient.gui.tabs.Tab
import meteordevelopment.meteorclient.gui.tabs.Tabs
import meteordevelopment.meteorclient.gui.tabs.builtin.ConfigTab
import meteordevelopment.meteorclient.utils.misc.MeteorIdentifier

//Config is added at the end to ensure it's always at the very right of the top bar
fun List<Tab>.renderOrder() =
    filter { !it.displayIcon.get() } to (filter { it.displayIcon.get() && it.name != ConfigTab.NAME } + Tabs.config() )


fun resource(path: String) = MeteorIdentifier(path)
