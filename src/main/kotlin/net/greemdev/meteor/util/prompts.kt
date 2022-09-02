/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */
@file:JvmName("Prompts")

package net.greemdev.meteor.util

import meteordevelopment.meteorclient.utils.render.prompts.*

fun resetConfirmation(message: String, onConfirm: Runnable): YesNoPrompt.() -> Unit = {
    message("$message. Are you sure you want to do this?")
    onYes(onConfirm)
}

fun confirm(id: String, builder: YesNoPrompt.() -> Unit): YesNoPrompt = YesNoPrompt.create().id(id).apply(builder)

fun notice(id: String, builder: OkPrompt.() -> Unit): OkPrompt = OkPrompt.create().id(id).apply(builder)
