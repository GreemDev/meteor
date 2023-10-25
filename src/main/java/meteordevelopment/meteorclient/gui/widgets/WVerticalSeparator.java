/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

public class WVerticalSeparator extends WWidget {

    public boolean unicolor;

    public WVerticalSeparator() {
        this(false);
    }

    public WVerticalSeparator(boolean unicolor) {
        this.unicolor = unicolor;
    }

    @Override
    protected void onCalculateSize() {
        width = theme.scale(3);
        height = 1;
    }
}
