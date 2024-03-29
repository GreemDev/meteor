/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProvidedStringSetting extends StringSetting {
    public static Builder builderProvided() {
        return new Builder();
    }

    public final Supplier<String[]> supplier;

    protected ProvidedStringSetting(String name, String description, Object defaultValue, Consumer<String> onChanged, Consumer<Setting<String>> onModuleActivated, Supplier<Boolean> visible, Class<? extends WTextBox.Renderer> renderer, boolean wide, Supplier<String[]> supplier) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, null, wide);

        this.supplier = supplier;
    }

    public static class Builder extends SettingBuilder<Builder, String, ProvidedStringSetting> {
        private Class<? extends WTextBox.Renderer> renderer;
        private Supplier<String[]> supplier;
        private boolean wide;

        public Builder() {
            super(null);
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder supplier(Supplier<String[]> supplier) {
            this.supplier = supplier;
            return this;
        }

        public Builder wide() {
            wide = true;
            return this;
        }

        @Override
        public ProvidedStringSetting build() {
            return new ProvidedStringSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, renderer, wide, supplier);
        }
    }
}
