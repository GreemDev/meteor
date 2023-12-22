/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.asm;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.greemdev.meteor.utils;

public class Descriptor {
    private final String[] components;

    public Descriptor(String... components) {
        this.components = components;
    }

    public String toString(boolean method, boolean map) {
        StringBuilder sb = new StringBuilder();

        if (method) sb.append('(');
        for (int i = 0; i < components.length; i++) {
            if (method && i == utils.lastIndex(components)) sb.append(')');

            String component = components[i];

            if (map && component.startsWith("L") && component.endsWith(";")) {
                sb.append('L')
                    .append(FabricLoader.getInstance().getMappingResolver()
                        .mapClassName("intermediary", component.substring(1, component.length() - 1).replace('/', '.')).replace('.', '/')
                    )
                    .append(';');
            }
            else sb.append(component);
        }

        return sb.toString();
    }
}
