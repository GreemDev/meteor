/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WWindow;
import meteordevelopment.meteorclient.systems.config.Config;

public abstract class WindowTabScreen extends TabScreen {
    protected final WWindow window;

    public WindowTabScreen(GuiTheme theme, Tab tab) {
        super(theme, tab);

        window = (tab.icon != null
            ? super.add(theme.window(theme.guiTexture(tab.icon, theme.textColor()), tab.name))
            : super.add(theme.window(tab.name))
        ).center().widget();
    }

    @Override
    public <W extends WWidget> Cell<W> add(W widget) {
        return window.add(widget);
    }

    protected void reloadTopBar() {
        root.cells.stream()
            .filter(c -> c.widget() instanceof WTopBar)
            .findFirst()
            .map(c ->
                (WTopBar)c
                    .alignY(Config.getTopBarAlignmentY())
                    .alignX(Config.getTopBarAlignmentX())
                    .widget())
            .ifPresent(WTopBar::init);
    }

    @Override
    public void clear() {
        window.clear();
    }
}
