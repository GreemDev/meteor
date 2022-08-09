/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor

import meteordevelopment.meteorclient.systems.modules.Module

abstract class GModule(name: String, description: String) : Module(Greteor.moduleCategory(), name, description)
