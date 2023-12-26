/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Category {
    public final String name;
    private final Optional<ItemStack> icon;
    private final int nameHash;

    public ItemStack itemIcon() {
        return icon.orElse(Items.AIR.getDefaultStack());
    }

    public Category(String name, @Nullable ItemStack icon) {
        this.name = name;
        this.nameHash = name.hashCode();
        this.icon = Optional.ofNullable(icon);
    }
    public Category(String name) {
        this(name, null);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return nameHash == category.nameHash;
    }

    @Override
    public int hashCode() {
        return nameHash;
    }
}
