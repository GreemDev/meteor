/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("JsonSerialization")
package net.greemdev.meteor.util.misc

import com.google.gson.JsonDeserializer
import net.greemdev.meteor.getOrNull
import java.time.Instant
import java.util.*

@get:JvmName("jsonToDate")
val dateDeserializer = JsonDeserializer { jsonElement, _, _ ->
    getOrNull {

        Date.from(Instant.parse(jsonElement.asString))
    }
}
