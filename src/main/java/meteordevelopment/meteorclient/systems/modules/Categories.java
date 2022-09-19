/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules;

import net.greemdev.meteor.Greteor;
import net.minecraft.item.Items;

public class Categories {
    public static final Category Combat = new Category("Combat", Items.DIAMOND_SWORD.getDefaultStack());
    public static final Category Player = new Category("Player", Items.PLAYER_HEAD.getDefaultStack());
    public static final Category Movement = new Category("Movement", Items.SCULK_SENSOR.getDefaultStack());
    public static final Category Render = new Category("Render", Items.BLAZE_ROD.getDefaultStack());
    public static final Category World = new Category("World", Items.GRASS_BLOCK.getDefaultStack());
    public static final Category Misc = new Category("Misc", Items.BEDROCK.getDefaultStack());

    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;

        // Meteor
        Modules.registerCategory(Combat);
        Modules.registerCategory(Player);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(World);
        Modules.registerCategory(Misc);

        Greteor.categories();

        REGISTERING = false;
    }
}
