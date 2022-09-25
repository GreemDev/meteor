/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.entity.fakeplayer;

import meteordevelopment.meteorclient.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class FakePlayerManager {
    private static final List<FakePlayerEntity> ENTITIES = new ArrayList<>();

    public static FakePlayerEntity get(String name) {
        return stream()
            .filter(fp -> fp.getEntityName().equals(name))
            .findFirst()
            .orElse(null);
    }

    public static void add(String name, float health, boolean copyInv) {
        if (!Utils.canUpdate()) return;

        FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, name, health, copyInv);
        fakePlayer.spawn();
        ENTITIES.add(fakePlayer);
    }

    public static void forEach(Consumer<FakePlayerEntity> consumer) {
        ENTITIES.forEach(consumer);
    }

    public static boolean remove(FakePlayerEntity fakePlayer) {
        if (fakePlayer == null) return false;
        return ENTITIES.removeIf(fp -> {
            if (fp.getEntityName().equals(fakePlayer.getEntityName())) {
                fp.despawn();
                return true;
            }
            return false;
        });
    }

    public static void clear() {
        if (ENTITIES.isEmpty()) return;
        ENTITIES.forEach(FakePlayerEntity::despawn);
        ENTITIES.clear();
    }

    public static Stream<FakePlayerEntity> stream() {
        return ENTITIES.stream();
    }

    public static int count() {
        return ENTITIES.size();
    }

    public static boolean contains(FakePlayerEntity fp) {
        return ENTITIES.contains(fp);
    }
}
