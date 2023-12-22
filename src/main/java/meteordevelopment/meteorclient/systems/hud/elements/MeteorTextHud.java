/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import net.greemdev.meteor.hud.element.GreteorTextHud;

public class MeteorTextHud {
    public static final HudElementInfo<TextHud> INFO = new HudElementInfo<>(Hud.GROUP, "text", "Displays arbitrary text with Starscript.", MeteorTextHud::create);

    public static final HudElementInfo<TextHud>.Preset FPS;
    public static final HudElementInfo<TextHud>.Preset TPS;
    public static final HudElementInfo<TextHud>.Preset PING;
    public static final HudElementInfo<TextHud>.Preset SPEED;
    public static final HudElementInfo<TextHud>.Preset DURABILITY;
    public static final HudElementInfo<TextHud>.Preset POSITION;
    public static final HudElementInfo<TextHud>.Preset OPPOSITE_POSITION;
    public static final HudElementInfo<TextHud>.Preset LOOKING_AT;
    public static final HudElementInfo<TextHud>.Preset LOOKING_AT_WITH_POSITION;
    public static final HudElementInfo<TextHud>.Preset BREAKING_PROGRESS;
    public static final HudElementInfo<TextHud>.Preset SERVER;
    public static final HudElementInfo<TextHud>.Preset BIOME;
    public static final HudElementInfo<TextHud>.Preset WORLD_TIME;
    public static final HudElementInfo<TextHud>.Preset REAL_TIME;
    public static final HudElementInfo<TextHud>.Preset ROTATION;
    public static final HudElementInfo<TextHud>.Preset MODULE_ENABLED;
    public static final HudElementInfo<TextHud>.Preset MODULE_ENABLED_WITH_INFO;
    public static final HudElementInfo<TextHud>.Preset WATERMARK;
    public static final HudElementInfo<TextHud>.Preset BARITONE;

    static {
        addPreset("Empty", null);
        FPS = addPreset("FPS", "FPS: #1{fps}", 0);
        TPS = addPreset("TPS", "TPS: #1{round(server.tps, 1)}");
        PING = addPreset("Ping", "Ping: #1{ping}");
        SPEED = addPreset("Speed", "Speed: #1{round(player.speed, 1)}", 0);
        DURABILITY = addPreset("Durability", "Durability: #1{player.handOrOffhand.durability}");
        POSITION = addPreset("Position", "Pos: #1{floor(camera.pos.x)}, {floor(camera.pos.y)}, {floor(camera.pos.z)}", 0);
        OPPOSITE_POSITION = addPreset("Opposite Position", "{player.dimensionOpposite != \"End\" ? player.dimensionOpposite + \":\" : \"\"} #1{player.dimensionOpposite != \"End\" ? \"\" + floor(camera.posOpposite.x) + \", \" + floor(camera.posOpposite.y) + \", \" + floor(camera.posOpposite.z) : \"\"}", 0);
        LOOKING_AT = addPreset("Looking at", "Looking at: #1{crosshairTarget.value}", 0);
        LOOKING_AT_WITH_POSITION = addPreset("Looking at with position", "Looking at: #1{crosshairTarget.value} {crosshairTarget.type != \"miss\" ? \"(\" + \"\" + floor(crosshairTarget.value.pos.x) + \", \" + floor(crosshairTarget.value.pos.y) + \", \" + floor(crosshairTarget.value.pos.z) + \")\" : \"\"}", 0);
        BREAKING_PROGRESS = addPreset("Breaking progress", "Breaking progress: #1{round(player.breakingProgress * 100)}%", 0);
        SERVER = addPreset("Server", "Server: #1{server}");
        BIOME = addPreset("Biome", "Biome: #1{player.biome}", 0);
        WORLD_TIME = addPreset("World time", "Time: #1{server.time}");
        REAL_TIME = addPreset("Real time", "Time: #1{time}");
        ROTATION = addPreset("Rotation", "{camera.direction} #1({round(camera.yaw, 1)}, {round(camera.pitch, 1)})", 0);
        MODULE_ENABLED = addPreset("Module enabled", "Kill Aura: {meteor.isModuleActive(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"}", 0);
        MODULE_ENABLED_WITH_INFO = addPreset("Module enabled with info", "Kill Aura: {meteor.isModuleActive(\"kill-aura\") ? #2 \"ON\" : #3 \"OFF\"} #1{meteor.getModuleInfo(\"kill-aura\")}", 0);
        WATERMARK = addPreset("Watermark", "Meteor Client #1{meteor.version}", Integer.MAX_VALUE);
        BARITONE = addPreset("Baritone", "Baritone: #1{baritone.processName}");
        GreteorTextHud.init();
    }

    private static TextHud create() {
        return new TextHud(INFO);
    }

    public static HudElementInfo<TextHud>.Preset addPreset(String title, String text, int updateDelay, TextHud.Shown shown, String condition) {
        return INFO.addPreset(title, textHud -> {
            if (text != null) textHud.text.set(text);
            if (updateDelay != -1) textHud.updateDelay.set(updateDelay);
            if (!shown.always() && condition != null) {
                textHud.shown.set(shown);
                textHud.condition.set(condition);
            }
        });
    }

    public static HudElementInfo<TextHud>.Preset addPreset(String title, String text, int updateDelay) {
        return addPreset(title, text, updateDelay, TextHud.Shown.Always, null);
    }

    public static HudElementInfo<TextHud>.Preset addPreset(String title, String text, String condition) {
        return addPreset(title, text, condition, -1);
    }

    public static HudElementInfo<TextHud>.Preset addPreset(String title, String text, String condition, int updateDelay) {
        return addPreset(title, text, updateDelay, TextHud.Shown.WhenTrue, condition);
    }

    public static HudElementInfo<TextHud>.Preset addPreset(String title, String text) {
        return addPreset(title, text, -1);
    }
}
