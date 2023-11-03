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
}
