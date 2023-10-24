/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.greemdev.meteor.utils;

public class NameProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> protectName = sgGeneral.add(new BoolSetting.Builder()
        .name("protect-name")
        .description("Hides your name client-side.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Name to be replaced with.")
        .visible(protectName::get)
        .defaultValue(() -> MeteorClient.randomAuthor().getName())
        .build()
    );

    private final Setting<Boolean> protectSkins = sgGeneral.add(new BoolSetting.Builder()
        .name("skin-protect")
        .description("Hide your skin by Detroit: Become Steve.")
        .defaultValue(true)
        .build()
    );

    private String username = "If you see this, something is wrong.";

    public NameProtect() {
        super(Categories.Player, "name-protect", "Hides your name client-side and other players' skins.");
    }

    @Override
    public void onActivate() {
        username = mc.getSession().getUsername();
    }

    public String replaceName(String string) {
        if (string != null && isActive()) {
            return string.replace(username, name.get());
        }

        return string;
    }

    public String getName(String original) {
        if (name.get().length() > 0 && isActive()) {
            return name.get();
        }

        return original;
    }

    public boolean protectName() {
        return isActive() && protectName.get();
    }

    public boolean protectSkins() {
        return isActive() && protectSkins.get();
    }
}
