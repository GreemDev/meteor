/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import kotlin.Pair;
import kotlin.collections.MapsKt;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class StringMapSetting extends Setting<Map<String, String>> {
    public static Builder builder() {
        return new Builder();
    }

    public final Class<? extends WTextBox.Renderer> keyRenderer;
    public final Class<? extends WTextBox.Renderer> valueRenderer;
    public final boolean wide;

    protected StringMapSetting(String name, String description, Object defaultValue, Consumer<Map<String, String>> onChanged, Consumer<Setting<Map<String, String>>> onModuleActivated, Supplier<Boolean> visible, boolean serialize, Class<? extends WTextBox.Renderer> keyRenderer, Class<? extends WTextBox.Renderer> valueRenderer, boolean wide) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        this.keyRenderer = keyRenderer;
        this.valueRenderer = valueRenderer;
        this.wide = wide;
    }

    @Override
    protected Map<String, String> parseImpl(String str) {
        String[] values = str.split(",");

        Map<String, String> map = new LinkedHashMap<>(values.length / 2);

        try {
            String left = null;
            for (int i = 0; i < values.length; i++) {
                if (i % 2 == 0) {
                    left = values[i];
                } else {
                    map.put(left, values[i]);
                }
            }
        } catch (Exception ignored) {
        }

        return map;
    }

    @Override
    protected boolean isValueValid(Map<String, String> value) {
        return true;
    }

    @Override
    protected void resetImpl() {
        value = new LinkedHashMap<>(getDefaultValue());
    }

    @Override
    protected void save(NbtCompound tag) {
        NbtCompound valueTag = new NbtCompound();
        for (String key : get().keySet()) {
            valueTag.put(key, NbtString.of(get().get(key)));
        }
        tag.put("map", valueTag);
    }

    @Override
    protected Map<String, String> load(NbtCompound tag) {
        get().clear();

        NbtCompound valueTag = tag.getCompound("map");
        for (String key : valueTag.getKeys()) {
            get().put(key, valueTag.getString(key));
        }

        return get();
    }

    public static void fillTable(GuiTheme theme, WTable table, StringMapSetting setting) {
        table.clear();

        Map<String, String> map = setting.get();

        for (String key : map.keySet()) {
            AtomicReference<String> key2 = new AtomicReference<>(key);

            WTextBox kTextBox = table.add(theme.textBox(key2.get(), setting.keyRenderer)).minWidth(150).expandX().widget();
            kTextBox.actionOnUnfocused = () -> {
                String text = kTextBox.get();
                if (map.containsKey(text)) {
                    kTextBox.set(key2.get());
                    return;
                }
                String value = map.remove(key2.get());
                key2.set(text);
                map.put(text, value);
            };

            WTextBox vTextBox = table.add(theme.textBox(map.get(key2.get()), setting.valueRenderer))
                .minWidth(150)
                .expandX()
                .widget();
            vTextBox.actionOnUnfocused = () -> map.replace(key2.get(), vTextBox.get());

            table.add(theme.minus(() -> {
                map.remove(key2.get());
                fillTable(theme, table, setting);
            }));

            table.row();
        }

        if (!map.isEmpty()) {
            table.add(theme.horizontalSeparator()).expandX();
            table.row();
        }

        table.add(theme.plus(() -> {
            map.put("", "");
            fillTable(theme, table, setting);
        })).expandCellX();

        table.add(theme.resetButton(() -> {
            setting.reset();
            fillTable(theme, table, setting);
        })).right();

        table.row();
    }

    public static class Builder extends SettingBuilder<Builder, Map<String, String>, StringMapSetting> {
        private Class<? extends WTextBox.Renderer> keyRenderer;
        private Class<? extends WTextBox.Renderer> valueRenderer;

        private boolean wide = false;

        public Builder() {
            super(new LinkedHashMap<>(0));
        }

        @SafeVarargs
        public final Builder defaultValue(Pair<String, String>... pairs) {
            this.defaultValue = MapsKt.mapOf(pairs);
            return this;
        }

        public Builder wide() {
            wide = !wide;
            return this;
        }

        public Builder renderer(Class<? extends WTextBox.Renderer> renderer) {
            this.keyRenderer = renderer;
            this.valueRenderer = renderer;
            return this;
        }

        public Builder rendererForKey(Class<? extends WTextBox.Renderer> renderer) {
            this.keyRenderer = renderer;
            return this;
        }

        public Builder rendererForValue(Class<? extends WTextBox.Renderer> renderer) {
            this.valueRenderer = renderer;
            return this;
        }

        public Builder renderStarscript() {
            return renderer(StarscriptTextBoxRenderer.class);
        }

        public Builder renderStarscriptKey() {
            return rendererForKey(StarscriptTextBoxRenderer.class);
        }

        public Builder renderStarscriptValue() {
            return rendererForValue(StarscriptTextBoxRenderer.class);
        }

        @Override
        public StringMapSetting build() {
            return new StringMapSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize, keyRenderer, valueRenderer, wide);
        }
    }
}
