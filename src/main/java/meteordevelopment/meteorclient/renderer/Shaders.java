/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.renderer;

import meteordevelopment.meteorclient.utils.PreInit;

public class Shaders {

    public static Shader POS_COLOR;
    public static Shader POS_TEX_COLOR;
    public static Shader TEXT;

    @PreInit
    public static void init() {
        POS_COLOR = new Shader("pos_color");
        POS_TEX_COLOR = new Shader("pos_tex_color");
        TEXT = new Shader("text");
    }
}
