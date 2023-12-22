/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("accessors")
package net.greemdev.meteor.util

import baritone.api.BaritoneAPI
import baritone.api.IBaritone
import baritone.api.IBaritoneProvider
import meteordevelopment.meteorclient.MeteorClient
import meteordevelopment.meteorclient.utils.misc.MeteorStarscript
import meteordevelopment.starscript.Starscript
import net.fabricmc.loader.api.FabricLoader
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.meteor.Meteor
import net.greemdev.meteor.util.text.FormattedText
import net.greemdev.meteor.util.text.emptyText
import net.minecraft.client.MinecraftClient
import java.util.function.Consumer

@get:JvmName("minecraft")
val minecraft: MinecraftClient = MeteorClient.mc
@get:JvmName("meteor")
val meteor: MeteorClient = Meteor.client
@get:JvmName("modLoader")
val modLoader: FabricLoader = FabricLoader.getInstance()
@get:JvmName("baritoneProvider")
val baritoneProvider: IBaritoneProvider by invoking(BaritoneAPI::getProvider)
@get:JvmName("baritone")
val baritone: IBaritone by invoking(baritoneProvider::getPrimaryBaritone)
@get:JvmName("starscript")
val meteorStarscript: Starscript = MeteorStarscript.ss
