/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.utils.Utils;
import net.greemdev.meteor.type.MeteorPromptException;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class HudElementInfo<T extends HudElement> {
    public final HudGroup group;
    public final String name;
    public final String title;
    public final String description;

    public final HudElement.Factory<T> factory;
    public final List<Preset> presets;

    public HudElementInfo(HudGroup group, String name, String title, String description, HudElement.Factory<T> factory) {
        this.group = group;
        this.name = name;
        this.title = title;
        this.description = description;

        this.factory = factory;
        this.presets = new ArrayList<>();
    }

    public HudElementInfo(HudGroup group, String name, String description, HudElement.Factory<T> factory) {
        this(group, name, Utils.nameToTitle(name), description, factory);
    }

    public Preset addPreset(String title, Consumer<T> callback) {
        Preset preset = new Preset(this, title, callback);

        presets.add(preset);
        presets.sort(Comparator.comparing(p -> p.title));

        return preset;
    }

    public boolean hasPresets() {
        return !presets.isEmpty();
    }

    public HudElement create() throws MeteorPromptException {
        return factory.create();
    }

    public Optional<HudElement> tryCreate(boolean showError) {
        try {
            return Optional.of(factory.create());
        } catch (MeteorPromptException err) {
            if (showError) err.tryShow();
            return Optional.empty();
        }
    }

    public class Preset {
        public final HudElementInfo<?> info;
        public final String title;
        public final Consumer<T> callback;

        public Preset(HudElementInfo<?> info, String title, Consumer<T> callback) {
            this.info = info;
            this.title = title;
            this.callback = callback;
        }
    }
}
