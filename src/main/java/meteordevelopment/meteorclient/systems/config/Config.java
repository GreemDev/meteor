/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.utils.StarscriptTextBoxRenderer;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.commands.Command;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.greemdev.meteor.type.ChatLogo;
import net.greemdev.meteor.util.meteor.HiddenModules;
import net.greemdev.meteor.type.PrefixBrackets;
import net.greemdev.meteor.util.text.ChatColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Config extends System<Config> {
    public final Settings settings = new Settings();

    private final SettingGroup sgVisual = settings.createGroup("Visual");
    private final SettingGroup sgChat = settings.createGroup("Chat");
    private final SettingGroup sgMisc = settings.createGroup("Misc");

    private final SettingGroup sgTopBar = settings.createGroup("Top Bar", false);

    // Visual
    public final Setting<Boolean> customFont = sgVisual.add(new BoolSetting.Builder()
        .name("custom-font")
        .description("Use a custom font.")
        .defaultValue(true)
        .build()
    );

    public final Setting<FontFace> font = sgVisual.add(new FontFaceSetting.Builder()
        .name("font")
        .description("Custom font to use.")
        .visible(customFont::get)
        .onChanged(Fonts::load)
        .build()
    );

    public final Setting<Double> rainbowSpeed = sgVisual.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("The global rainbow speed.")
        .defaultValue(0.5)
        .range(0, 10)
        .build()
    );

    public final Setting<Boolean> titleScreenVersionInfo = sgVisual.add(new BoolSetting.Builder()
        .name("title-screen-version")
        .description("Show Greteor version info on title screen")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> useCustomWindowTitle = sgVisual.add(new BoolSetting.Builder()
        .name("custom-window-title")
        .description("Show custom text in the window title.")
        .defaultValue(false)
        .onModuleActivated(setting -> mc.updateWindowTitle())
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    public final Setting<String> customWindowTitle = sgVisual.add(new StringSetting.Builder()
        .name("window-title-text")
        .description("The text it displays in the window title.")
        .visible(useCustomWindowTitle::get)
        .defaultValue("Minecraft {gameVersion} - Meteor Client {meteor.version}")
        .onChanged(value -> mc.updateWindowTitle())
        .renderer(StarscriptTextBoxRenderer.class)
        .build()
    );

    public final Setting<SettingColor> friendColor = sgVisual.add(new ColorSetting.Builder()
        .name("friend-color")
        .description("The color used to show friends.")
        .defaultValue(new SettingColor(0, 255, 180))
        .build()
    );

    // Chat
    public final Setting<String> prefix = sgChat.add(new StringSetting.Builder()
        .name("command-prefix")
        .description("Meteor command prefix.")
        .defaultValue(Command.DEFAULT_PREFIX)
        .build()
    );

    public final Setting<Boolean> chatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("chat-feedback")
        .description("Sends chat feedback when meteor performs certain actions.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> deleteChatFeedback = sgChat.add(new BoolSetting.Builder()
        .name("delete-chat-feedback")
        .description("Delete previous matching chat feedback to keep chat clear.")
        .visible(chatFeedback::get)
        .defaultValue(true)
        .build()
    );

    public final Setting<ChatLogo> chatLogo = sgChat.add(new EnumSetting.Builder<ChatLogo>()
        .name("chat-feedback-icon")
        .description("The icon to appear before Meteor-related chat feedback.")
        .defaultValue(ChatLogo.Meteor)
        .visible(chatFeedback::get)
        .build()
    );

    public final Setting<String> meteorPrefix = sgChat.add(new StringSetting.Builder()
        .name("chat-prefix")
        .description("The prefix to appear before all Meteor chat feedback messages.")
        .defaultValue("Meteor")
        .onChanged(s -> {
            if (s.equalsIgnoreCase("Baritone")) {
                ChatUtils.sendMsg(Text.of("You are not allowed to use the Baritone prefix."));
                resetPrefix();
            }
        })
        .build()
    );

    public final Setting<SettingColor> meteorPrefixColor = sgChat.add(new ColorSetting.Builder()
        .name("chat-prefix-color")
        .description("Color of the main prefix text.")
        .defaultValue(MeteorClient.COLOR)
        .build()
    );

    private void resetPrefix() {
        meteorPrefix.reset();
    }

    public final Setting<PrefixBrackets> meteorPrefixBrackets = sgChat.add(new EnumSetting.Builder<PrefixBrackets>()
        .name("chat-prefix-brackets")
        .description("The brackets to be placed before and after the Meteor chat feedback prefix.")
        .defaultValue(PrefixBrackets.Square)
        .build()
    );

    public final Setting<SettingColor> meteorPrefixBracketsColor = sgChat.add(new ColorSetting.Builder()
        .name("chat-prefix-brackets-color")
        .description("Color of the brackets around the prefix text.")
        .defaultValue(ChatColor.grey.asMeteor())
        .build()
    );

    // Misc
    public final Setting<List<Module>> hiddenModules = sgMisc.add(new ModuleListSetting.Builder()
        .name("hidden-modules")
        .description("Modules to hide from Meteor's Modules screen.")
        .defaultValue(HiddenModules::getModules)
        .serialize(false)
        .onChanged(HiddenModules::set)
        .build()
    );

    public static List<String> hiddenModuleNames = new ArrayList<>();

    public final Setting<Boolean> hiddenModulesAffectsSearch = sgMisc.add(new BoolSetting.Builder()
        .name("hide-modules-in-search")
        .description("Also hide your hidden modules from the Meteor search.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> lastTabMemory = sgMisc.add(new BoolSetting.Builder()
        .name("remember-last-tab")
        .description("Reopen the last tab you were using in Meteor's GUI.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> useCustomSplashes = sgMisc.add(new BoolSetting.Builder()
        .name("use-custom-splashes")
        .description("Show custom splash texts on title screen")
        .defaultValue(false)
        .build()
    );

    public final Setting<List<String>> customSplashes = sgMisc.add(new StringListSetting.Builder()
        .name("custom-splashes")
        .description("Custom splash texts to use on the title screen")
        .defaultValue("Meteor on Crack!",
            "Star Meteor Client on GitHub!",
            "Based utility mod.",
            "&6MineGame159 &fbased god",
            "&4meteorclient.com",
            "&4Meteor on Crack!",
            "&6Meteor on Crack!")
        .visible(useCustomSplashes::get)
        .build()
    );

    public final Setting<Integer> rotationHoldTicks = sgMisc.add(new IntSetting.Builder()
        .name("rotation-hold")
        .description("How long to hold server side rotation when not sending any packets.")
        .defaultValue(4)
        .build()
    );

    public final Setting<Integer> moduleSearchCount = sgMisc.add(new IntSetting.Builder()
        .name("module-search-count")
        .description("Amount of modules and settings to be shown in the module search bar.")
        .defaultValue(10)
        .range(1, 25)
        .build()
    );

    public final Setting<Boolean> useTeamColor = sgMisc.add(new BoolSetting.Builder()
        .name("use-team-color")
        .description("Uses player's team color for rendering things like esp and tracers.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> baritoneIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("baritone-icon")
        .description("Replace Baritone in top bar with the Baritone bass clef icon.")
        .defaultValue(true)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public final Setting<Boolean> friendsIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("friends-icon")
        .description("Replace Friends in top bar with the friends icon.")
        .defaultValue(false)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public final Setting<Boolean> profilesIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("profiles-icon")
        .description("Replace Profiles in top bar with a profile icon.")
        .defaultValue(false)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public final Setting<Boolean> guiIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("GUI-icon")
        .description("Replace GUI in top bar with a GUI icon.")
        .defaultValue(false)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public final Setting<Boolean> waypointsIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("waypoints-icon")
        .description("Replace Waypoints in top bar with a location pin icon.")
        .defaultValue(true)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public final Setting<Boolean> macrosIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("macros-icon")
        .description("Replace Macros in top bar with an M icon.")
        .defaultValue(true)
        .onChanged(b -> WTopBar.NEEDS_REFRESH = true)
        .build()
    );

    public List<String> dontShowAgainPrompts = new ArrayList<>();

    public Config() {
        super("config");
    }

    public static Config get() {
        return Systems.get(Config.class);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("version", MeteorClient.VERSION.toString());
        tag.put("settings", settings.toTag());
        tag.put("dontShowAgainPrompts", listToTag(dontShowAgainPrompts));
        tag.put("hiddenModules", listToTag(hiddenModuleNames));

        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("dontShowAgainPrompts")) dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");
        if (tag.contains("hiddenModules")) hiddenModuleNames = listFromTag(tag, "hiddenModules");

        return this;
    }

    private NbtList listToTag(List<String> list) {
        NbtList nbt = new NbtList();
        for (String item : list) nbt.add(NbtString.of(item));
        return nbt;
    }

    private List<String> listFromTag(NbtCompound tag, String key) {
        List<String> list = new ArrayList<>();
        for (NbtElement item : tag.getList(key, 8)) list.add(item.asString());
        return list;
    }
}
