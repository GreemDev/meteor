/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.misc.KMC;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL32C.*;

public class Shader {
    public static Shader BOUND;

    private final int id;
    private final Object2IntMap<String> uniformLocations = new Object2IntOpenHashMap<>();

    public Shader(String shaderFileName) {
        this(shaderFileName + ".vert", shaderFileName + ".frag");
    }

    public Shader(String vertPath, String fragPath) {
        int vert = GL.createShader(GL_VERTEX_SHADER);
        {
            String vertexShader = read(vertPath, "Cannot find vertex shader %s in jar resources");
            GL.shaderSource(vert, vertexShader);

            String vertError = GL.compileShader(vert);
            if (vertError != null) {
                MeteorClient.LOG.error("Failed to compile vertex shader (" + vertPath + "): " + vertError);
                throw new RuntimeException("Failed to compile vertex shader (" + vertPath + "): " + vertError);
            }
        }


        int frag = GL.createShader(GL_FRAGMENT_SHADER);
        {
            String fragmentShader = read(fragPath, "Cannot find fragment shader %s in jar resources");
            GL.shaderSource(frag, fragmentShader);

            String fragError = GL.compileShader(frag);
            if (fragError != null) {
                MeteorClient.LOG.error("Failed to compile fragment shader (" + fragPath + "): " + fragError);
                throw new RuntimeException("Failed to compile fragment shader (" + fragPath + "): " + fragError);
            }
        }

        id = GL.createProgram();

        String programError = GL.linkProgram(id, vert, frag);
        if (programError != null) {
            MeteorClient.LOG.error("Failed to link program: " + programError);
            throw new RuntimeException("Failed to link program: " + programError);
        }

        GL.deleteShader(vert);
        GL.deleteShader(frag);
    }

    private String read(String path, String notFoundError) {
        return KMC.getMeteorResource("shaders/%s".formatted(path))
            .map(rs -> {
                try {
                    return IOUtils.toString(rs.getInputStream(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not read shader '%s'".formatted(path), e);
                }
            })
            .orElseThrow(() -> new RuntimeException(notFoundError.formatted(path)));
    }

    public void bind() {
        GL.useProgram(id);
        BOUND = this;
    }

    private int getLocation(String name) {
        if (uniformLocations.containsKey(name)) return uniformLocations.getInt(name);

        int location = GL.getUniformLocation(id, name);
        uniformLocations.put(name, location);
        return location;
    }

    public void set(String name, boolean v) {
        GL.uniformInt(getLocation(name), v ? GL_TRUE : GL_FALSE);
    }

    public void set(String name, int v) {
        GL.uniformInt(getLocation(name), v);
    }

    public void set(String name, double v) {
        GL.uniformFloat(getLocation(name), (float) v);
    }

    public void set(String name, double v1, double v2) {
        GL.uniformFloat2(getLocation(name), (float) v1, (float) v2);
    }

    public void set(String name, Color color) {
        GL.uniformFloat4(getLocation(name), (float) color.r / 255, (float) color.g / 255, (float) color.b / 255, (float) color.a / 255);
    }

    public void set(String name, Matrix4f mat) {
        GL.uniformMatrix(getLocation(name), mat);
    }

    public void setDefaults() {
        set("u_Proj", RenderSystem.getProjectionMatrix());
        set("u_ModelView", RenderSystem.getModelViewStack().peek().getPositionMatrix());
    }
}
