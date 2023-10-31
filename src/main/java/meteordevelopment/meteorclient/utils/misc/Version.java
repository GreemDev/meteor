/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.misc;

import kotlin.Triple;

import java.util.Arrays;

public class Version {
    private final String string;
    private final int[] numbers;

    public Triple<Integer, Integer, Integer> components() {
        return new Triple<>(major(), minor(), patch());
    }

    public int major() {
        return numbers[0];
    }
    public int minor() {
        return numbers[1];
    }

    public int patch() {
        return numbers[2];
    }

    public Version(String string) {
        this.string = string;
        this.numbers = new int[3];

        String[] split = string.trim().split("\\.");
        if (split.length != 3) throw new IllegalArgumentException("Version string needs to have 3 numbers, separated by a dot.");

        for (int i = 0; i < 3; i++) {
            try {
                numbers[i] = Integer.parseInt(split[i]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Failed to parse version string.");
            }
        }
    }

    public boolean isZero() {
        return Arrays.stream(numbers).noneMatch(i -> i == 0);
    }

    public boolean isHigherThan(Version version) {
        for (int i = 0; i < 3; i++) {
            if (numbers[i] > version.numbers[i]) return true;
            if (numbers[i] < version.numbers[i]) return false;
        }

        return false;
    }



    @Override
    public String toString() {
        return string;
    }
}
