/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.friends;

import com.mojang.util.UUIDTypeAdapter;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Friends extends System<Friends> implements Iterable<Friend> {
    private final List<Friend> friends = new ArrayList<>();

    public Friends() {
        super("friends");
    }

    public static Friends get() {
        return Systems.get(Friends.class);
    }

    public boolean add(Friend friend) {
        if (friend.getName().isEmpty() || friend.getName().contains(" ")) return false;

        if (!friends.contains(friend)) {
            friends.add(friend);
            save();

            return true;
        }

        return false;
    }

    public boolean remove(Friend friend) {
        if (friends.remove(friend)) {
            save();
            return true;
        }

        return false;
    }

    public Friend get(String name) {
        return friends.stream()
            .filter(f -> f.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public Friend getIgnoreCase(String name) {
        return friends.stream()
            .filter(f -> f.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElse(null);
    }


    public Friend get(PlayerEntity player) {
        return get(player.getEntityName());
    }

    public Friend get(PlayerListEntry player) {
        return get(player.getProfile().getName());
    }

    public boolean isFriend(PlayerEntity player) {
        return get(player) != null;
    }

    public boolean isFriend(PlayerListEntry player) {
        return get(player) != null;
    }

    public boolean shouldAttack(PlayerEntity player) {
        return !isFriend(player);
    }

    public int count() {
        return friends.size();
    }

    public boolean isEmpty() {
        return friends.isEmpty();
    }

    @Override
    public @NotNull Iterator<Friend> iterator() {
        return friends.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.put("friends", NbtUtils.listToTag(friends));

        return tag;
    }

    @Override
    public Friends fromTag(NbtCompound tag) {
        friends.clear();

        tag.getList("friends", 10).forEach(it -> {
            var friendTag = (NbtCompound)it;
            if (!friendTag.contains("name")) return;

            var name = friendTag.getString("name");
            if (get(name) != null) return;

            var uuid = friendTag.getString("id");
            friends.add(!uuid.isBlank()
                ? new Friend(name, UUIDTypeAdapter.fromString(uuid))
                : new Friend(name));
        });

        Collections.sort(friends);

        return this;
    }
}
