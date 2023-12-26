/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

public enum AlignmentX {
    Left, Center, Right;

    public boolean right() {
        return this == Right;
    }

    public boolean center() {
        return this == Center;
    }

    public boolean left() {
        return this == Left;
    }

    public double align(double x, double w, double titleWidth) {
        return switch (this) {
            case Left -> x;
            case Center -> x + w / 2 - titleWidth / 2;
            case Right -> x + w - titleWidth;
        };
    }
}
