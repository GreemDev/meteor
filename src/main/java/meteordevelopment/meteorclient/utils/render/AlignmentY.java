/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

public enum AlignmentY {
    Top, Center, Bottom;

    public boolean top() {
        return this == Top;
    }

    public boolean center() {
        return this == Center;
    }

    public boolean bottom() {
        return this == Bottom;
    }

    public double align(double y, double scaledTopMargin, double h, double height) {
        return switch (this) {
            case Top -> y + scaledTopMargin;
            case Center -> y + h / 2 - height / 2;
            case Bottom -> y - h - height;
        };
    }
}
