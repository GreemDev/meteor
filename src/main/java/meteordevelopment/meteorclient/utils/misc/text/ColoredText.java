/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc.text;

import meteordevelopment.meteorclient.utils.render.color.Color;

/**
 * Encapsulates a string and the color it should have. See {@link TextUtils}
 */
public record ColoredText(String text, Color color) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColoredText that = (ColoredText) o;
        return text.equals(that.text) && color.equals(that.color);
    }

}
