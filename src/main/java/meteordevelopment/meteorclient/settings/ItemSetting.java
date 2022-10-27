/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.settings;

import net.minecraft.item.Item;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemSetting extends Setting<Item> {
    public final Predicate<Item> filter;

    protected ItemSetting(String name, String description, Object defaultValue, Consumer<Item> onChanged, Consumer<Setting<Item>> onModuleActivated, Predicate<Item> filter, IVisible visible, boolean serialize) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible, serialize);

        this.filter = filter;
    }

    @Override
    protected Item parseImpl(String str) {
        return parseId(Registry.ITEM, str);
    }

    @Override
    protected boolean isValueValid(Item value) {
        return filter == null || filter.test(value);
    }

    @Override
    public Iterable<Identifier> getIdentifierSuggestions() {
        return Registry.ITEM.getIds();
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.putString("value", Registry.ITEM.getId(get()).toString());

        return tag;
    }

    @Override
    public Item load(NbtCompound tag) {
        value = Registry.ITEM.get(new Identifier(tag.getString("value")));

        if (filter != null && !filter.test(value)) {
            for (Item item : Registry.ITEM) {
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
            return new ItemSetting(name, description, defaultValue, onChanged, onModuleActivated, filter, visible, serialize);
        }
    }
}
