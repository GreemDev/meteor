/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.greemdev.meteor.utils;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ModuleListSetting extends Setting<List<Module>> {
    private static List<String> suggestions;

    public final Predicate<? super Module> filter;
    private final boolean bypassFilterWhenSavingAndLoading;

    protected ModuleListSetting(String name, String description, Object defaultValue, Consumer<List<Module>> onChanged, Consumer<Setting<List<Module>>> onModuleActivated, Predicate<? super Module> filter, IVisible visible, boolean serialize,  boolean bypassFilterWhenSavingAndLoading) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.filter = filter;
        this.bypassFilterWhenSavingAndLoading = bypassFilterWhenSavingAndLoading;
    }

    protected ModuleListSetting(String name, String description, Object defaultValue, Consumer<List<Module>> onChanged, Consumer<Setting<List<Module>>> onModuleActivated, IVisible visible, boolean serialize) {
        this(name, description, defaultValue, onChanged, onModuleActivated, null, visible, serialize, false);
    }

    @Override
    public void resetImpl() {
        value = new ArrayList<>(getDefaultValue());
    }

    @Override
    protected List<Module> parseImpl(String str) {
        String[] values = str.split(",");
        List<Module> modules = new ArrayList<>(values.length);

        try {
            for (String value : values) {
                Module module = Modules.get().get(value.trim());
                if (module != null && (filter == null || filter.test(module))) modules.add(module);
            }
        } catch (Throwable ignored) {
        }

        return modules;
    }

    @Override
    public List<String> getSuggestions() {
        if (suggestions == null) {
            suggestions = new ArrayList<>(Modules.get().getAll().size());
            for (Module module : Modules.get().getAll()) suggestions.add(module.name);
        }

        return suggestions;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        NbtList modulesTag = new NbtList();
        for (Module module : get()) {
            if (bypassFilterWhenSavingAndLoading || (filter == null || filter.test(module)))
                modulesTag.add(NbtString.of(module.name));
        }
        tag.put("modules", modulesTag);

        return tag;
    }

    @Override
    public List<Module> load(NbtCompound tag) {
        get().clear();

        NbtList valueTag = tag.getList("modules", 8);
        for (NbtElement tagI : valueTag) {
            Module module = Modules.get().get(tagI.asString());
            if (module != null && (bypassFilterWhenSavingAndLoading || (filter == null || filter.test(module)))) get().add(module);
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, List<Module>, ModuleListSetting> {

        private Predicate<? super Module> modulePredicate = null;
        private boolean bypassFilterWhenSavingAndLoading = false;

        public Builder() {
            super(new ArrayList<>(0));
        }

        @SafeVarargs
        public final Builder defaultValue(Class<? extends Module>... defaults) {
            List<Module> modules = new ArrayList<>();

            for (Class<? extends Module> klass : defaults) {
                if (Modules.get().get(klass) != null) modules.add(Modules.get().get(klass));
            }

            return defaultValue(modules);
        }

        public final Builder filter(Predicate<? super Module> predicate) {
            modulePredicate = predicate;
            return this;
        }

        public final Builder ignoreFilterInNbt(boolean bypassFilterWhenSavingAndLoading) {
            this.bypassFilterWhenSavingAndLoading = bypassFilterWhenSavingAndLoading;
            return this;
        }

        @Override
        public ModuleListSetting build() {
            return new ModuleListSetting(name, description, defaultValue, onChanged, onModuleActivated, modulePredicate, visible, serialize, bypassFilterWhenSavingAndLoading);
        }
    }
}
