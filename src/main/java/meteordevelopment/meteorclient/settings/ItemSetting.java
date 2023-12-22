/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ItemSetting extends Setting<Item> {
    public static Builder builder() {
        return new Builder();
    }

    public final Predicate<Item> filter;

    protected ItemSetting(String name, String description, Object defaultValue, Consumer<Item> onChanged, Consumer<Setting<Item>> onModuleActivated, Supplier<Boolean> visible, boolean serialize, Predicate<Item> filter) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        this.filter = filter;
    }

    @Override
    protected Item parseImpl(String str) {
        return parseId(Registries.ITEM, str);
    }

    @Override
    protected boolean isValueValid(Item value) {
        return filter == null || filter.test(value);
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registries.ITEM.getIds();
    }

    @Override
    public void save(NbtCompound tag) {
        tag.putString("value", Registries.ITEM.getId(get()).toString());
    }

    @Override
    public Item load(NbtCompound tag) {
        value = Registries.ITEM.get(new Identifier(tag.getString("value")));

        if (filter != null && !filter.test(value)) {
            for (Item item : Registries.ITEM) {
                if (filter.test(item)) {
                    value = item;
                    break;
                }
            }
        }

        return get();
    }

    public static class Builder extends SettingBuilder<Builder, Item, ItemSetting> {
        private Predicate<Item> filter;

        public Builder() {
            super(null);
        }

        public Builder filter(Predicate<Item> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public ItemSetting build() {
            return new ItemSetting(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize, filter);
        }
    }
}
