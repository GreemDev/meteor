/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.tabs;

import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import meteordevelopment.meteorclient.gui.tabs.builtin.*;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import net.greemdev.meteor.gui.tab.WaypointsTab;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Tabs {
    private static Tab _lastTab;

    public static void setLastTab(Tab tab) {
        if (tab == null) return;
        if (Config.get().lastTabMemory.get()) {
            if (!tab.equals(_lastTab))
                _lastTab = tab;
        } else if (_lastTab != null) {
            _lastTab = null;
        }
    }

    public static Tab getTabToOpen() {
        return Config.get().lastTabMemory.get() && _lastTab != null
            ? _lastTab
            : modules();
    }
    private static final List<Tab> tabs = new ArrayList<>();

    public static <T extends Tab> T get(String name) {
        return Utils.cast(
            tabs.stream()
            .filter(t -> t.name.equals(name))
            .findFirst()
            .orElse(null)
        );
    }

    @NotNull
    public static ModulesTab modules() {
        return get(ModulesTab.NAME);
    }

    @NotNull
    public static GuiTab gui() {
        return get(GuiTab.NAME);
    }

    @NotNull
    public static HudTab hud() {
        return get(HudTab.NAME);
    }

    @NotNull
    public static FriendsTab friends() {
        return get(FriendsTab.NAME);
    }

    @NotNull
    public static MacrosTab macros() {
        return get(MacrosTab.NAME);
    }

    @NotNull
    public static ProfilesTab profiles() {
        return get(ProfilesTab.NAME);
    }

    @NotNull
    public static BaritoneTab baritone() {
        return get(BaritoneTab.NAME);
    }

    @NotNull
    public static WaypointsTab waypoints() {
        return get(WaypointsTab.NAME);
    }

    @NotNull
    public static ConfigTab config() {
        return get(ConfigTab.NAME);
    }

    static {
        add(new ModulesTab());
        add(new ConfigTab());
        add(new GuiTab());
        add(new HudTab());
        add(new FriendsTab());
        add(new MacrosTab());
        add(new ProfilesTab());
        add(new BaritoneTab());
        add(new WaypointsTab());
    }

    public static void add(Tab tab) {
        tabs.add(tab);
    }

    public static List<Tab> get() {
        return new ObjectImmutableList<>(tabs);
    }
}
