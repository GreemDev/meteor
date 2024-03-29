/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.packer.GuiTexture;
import meteordevelopment.meteorclient.systems.config.Config;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public abstract class Tab {
    public final String name;
    @Nullable
    public final GuiTexture icon;
    public final Supplier<Boolean> displayIcon;

    public Tab(String name, @NotNull GuiTexture icon, Supplier<Boolean> displayIcon) {
        this.name = name;
        this.icon = Objects.requireNonNull(icon);
        this.displayIcon = displayIcon;
    }

    public Tab(String name, @NotNull GuiTexture icon) {
        this.name = name;
        this.icon = Objects.requireNonNull(icon);
        this.displayIcon = () -> true;
    }

    public Tab(String name) {
        this.name = name;
        this.icon = null;
        this.displayIcon = () -> false;
    }

    public void openScreen(GuiTheme theme) {
        TabScreen screen = this.createScreen(theme);
        screen.addDirect(theme.topBar())
            .alignY(Config.getTopBarAlignmentY()) //custom alignment type used for vertical because i don't want to allow placing the top bar in the vertical center of the screen
            .alignX(Config.getTopBarAlignmentX());
        mc.setScreen(screen);
        Tabs.setLastTab(this);
    }

    public abstract TabScreen createScreen(GuiTheme theme);

    public abstract boolean isScreen(Screen screen);

    @Override
    public boolean equals(@NotNull Object obj) {
        return obj instanceof Tab tab && name.equals(tab.name);
    }
}
