/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs;

import meteordevelopment.meteorclient.gui.tabs.builtin.*;
import meteordevelopment.meteorclient.utils.PostInit;
import meteordevelopment.meteorclient.utils.Utils;
import net.greemdev.meteor.gui.tab.WaypointsTab;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tabs {
    private static final List<Tab> tabs = new ArrayList<>();

    @Nullable
    public static Tab ofName(String name) {
        return tabs.stream().filter(t -> t.name.equals(name)).findFirst().orElse(null);
    }

    @NotNull
    public static ModulesTab modules() {
        return Utils.cast(Objects.requireNonNull(ofName(ModulesTab.NAME)));
    }

    @NotNull
    public static GuiTab gui() {
        return Utils.cast(Objects.requireNonNull(ofName(GuiTab.NAME)));
    }

    @NotNull
    public static HudTab hud() {
        return Utils.cast(Objects.requireNonNull(ofName(HudTab.NAME)));
    }

    @NotNull
    public static FriendsTab friends() {
        return Utils.cast(Objects.requireNonNull(ofName(FriendsTab.NAME)));
    }

    @NotNull
    public static MacrosTab macros() {
        return Utils.cast(Objects.requireNonNull(ofName(MacrosTab.NAME)));
    }

    @NotNull
    public static ProfilesTab profiles() {
        return Utils.cast(Objects.requireNonNull(ofName(ProfilesTab.NAME)));
    }

    @NotNull
    public static BaritoneTab baritone() {
        return Utils.cast(Objects.requireNonNull(ofName(BaritoneTab.NAME)));
    }

    @NotNull
    public static WaypointsTab waypoints() {
        return WaypointsTab.INSTANCE;
    }

    @NotNull
    public static ConfigTab config() {
        return Utils.cast(Objects.requireNonNull(ofName(ConfigTab.NAME)));
    }


    @PostInit
    public static void init() {
        add(new ModulesTab());
        add(new ConfigTab());
        add(new GuiTab());
        add(new HudTab());
        add(new FriendsTab());
        add(new MacrosTab());
        add(new ProfilesTab());
        add(new BaritoneTab());
        add(WaypointsTab.INSTANCE);
    }

    public static void add(Tab tab) {
        tabs.add(tab);
    }

    public static List<Tab> get() {
        return tabs;
    }
}
