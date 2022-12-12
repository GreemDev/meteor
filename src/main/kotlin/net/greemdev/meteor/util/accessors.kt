/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

@file:JvmName("accessors")
package net.greemdev.meteor.util

import meteordevelopment.meteorclient.MeteorClient
import net.fabricmc.loader.api.FabricLoader
import net.greemdev.meteor.invoking
import net.greemdev.meteor.util.text.FormattedText
import net.greemdev.meteor.util.text.FormattedTextBuilder
import net.minecraft.client.MinecraftClient
import java.util.function.Consumer

@get:JvmName("minecraft")
val minecraft: MinecraftClient = MeteorClient.mc
@get:JvmName("meteor")
val meteor: MeteorClient = MeteorClient.INSTANCE
@get:JvmName("modLoader")
val modLoader: FabricLoader = FabricLoader.getInstance()
@get:JvmName("textBuilder")
val textBuilder by invoking(FormattedText::builder)
fun text(consumer: Consumer<FormattedTextBuilder>) = FormattedText.build(consumer)
