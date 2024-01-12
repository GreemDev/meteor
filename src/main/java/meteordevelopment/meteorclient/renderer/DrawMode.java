/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.renderer;

import org.lwjgl.opengl.GL32C;

public enum DrawMode {
    Lines(2, GL32C.GL_LINES),
    Triangles(3, GL32C.GL_TRIANGLES);

    public final int indicesCount;
    public final int gl;

    DrawMode(int indicesCount, int gl) {
        this.indicesCount = indicesCount;
        this.gl = gl;
    }
}
