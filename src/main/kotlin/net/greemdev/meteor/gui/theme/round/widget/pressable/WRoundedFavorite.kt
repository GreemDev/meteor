/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package net.greemdev.meteor.gui.theme.round.widget.pressable

import meteordevelopment.meteorclient.gui.widgets.pressable.WFavorite
import meteordevelopment.meteorclient.utils.render.color.Color
import net.greemdev.meteor.gui.theme.round.RoundedWidget
import net.greemdev.meteor.util.meteor.invoke

class WRoundedFavorite(checked: Boolean) : WFavorite(checked), RoundedWidget {
    override fun getColor(): Color = theme().favoriteColor()
}
