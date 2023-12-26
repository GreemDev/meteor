/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.config;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.widgets.WTopBar;
import meteordevelopment.meteorclient.renderer.Fonts;
import meteordevelopment.meteorclient.renderer.text.FontFace;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.utils.render.AlignmentX;
import meteordevelopment.meteorclient.utils.render.AlignmentY;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.greemdev.meteor.type.ChatPrefix;
import net.greemdev.meteor.type.PrefixBrackets;
import net.greemdev.meteor.type.VerticalAlignment;
import net.greemdev.meteor.util.text.ChatColor;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class Config extends System<Config> {

    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";

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
        .visible(customFont)
        .onChanged(Fonts::load)
        .build()
    );

    public final Setting<Double> rainbowSpeed = sgVisual.add(new DoubleSetting.Builder()
        .name("rainbow-speed")
        .description("The global rainbow speed.")
        .defaultValue(0.5)
        .range(0, 10)
        .sliderMax(5)
        .build()
    );

    public final Setting<Boolean> titleScreenVersionInfo = sgVisual.add(new BoolSetting.Builder()
        .name("title-screen-version")
        .description("Show Greteor version info on title screen.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> customWindowTitle = sgVisual.add(new BoolSetting.Builder()
        .name("custom-window-title")
        .description("Show custom text in the window title.")
        .defaultValue(false)
        .onModuleActivated(setting -> mc.updateWindowTitle())
        .onChanged(value -> mc.updateWindowTitle())
        .build()
    );

    public final Setting<String> customWindowTitleText = sgVisual.add(new StringSetting.Builder()
        .name("window-title-text")
        .description("The text it displays in the window title.")
        .visible(customWindowTitle)
        .defaultValue("Minecraft {gameVersion} - {meteor.name} {meteor.version}")
        .renderStarscript()
        .onChanged(value -> mc.updateWindowTitle())
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
        .name("prefix")
        .description("Command prefix.")
        .defaultValue(".")
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
        .visible(chatFeedback)
        .defaultValue(true)
        .build()
    );

    public final Setting<ChatPrefix> chatPrefix = sgChat.add(new EnumSetting.Builder<ChatPrefix>()
        .name("chat-feedback-prefix")
        .description("The icon to appear before Meteor-related chat feedback.")
        .defaultValue(ChatPrefix.Meteor)
        .visible(chatFeedback)
        .build()
    );

    public final Setting<SettingColor> meteorPrefixColor = sgChat.add(new ColorSetting.Builder()
        .name("chat-prefix-color")
        .description("Color of the main prefix text.")
        .defaultValue(MeteorClient.COLOR)
        .build()
    );

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

    public final Setting<Boolean> lastTabMemory = sgMisc.add(new BoolSetting.Builder()
        .name("remember-last-tab")
        .description("Reopen the last tab you were using in Meteor's GUI when pressing the Open GUI key, instead of opening modules.")
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
        .description("Custom splash texts to use on the title screen.\nAmpersands are automatically replaced with section symbols so you can use styling.")
        .defaultValue(
            "github.com/GreemDev/meteor",
            "Star Greteor Client on GitHub!",
            "Based utility mod.",
            "&6%s &fbased god".formatted(MeteorClient.randomAuthor().getName()),
            "&6Greteor on Crack!"
        )
        .visible(useCustomSplashes)
        .build()
    );

    public final Setting<Integer> rotationHoldTicks = sgMisc.add(new IntSetting.Builder()
        .name("rotation-hold")
        .description("Hold long to hold server side rotation when not sending any packets.")
        .defaultValue(4)
        .build()
    );

    public final Setting<Boolean> useTeamColor = sgMisc.add(new BoolSetting.Builder()
        .name("use-team-color")
        .description("Uses player's team color for rendering things like esp and tracers.")
        .defaultValue(true)
        .build()
    );

    public final Setting<Integer> moduleSearchCount = sgMisc.add(new IntSetting.Builder()
        .name("module-search-count")
        .description("Amount of modules and settings to be shown in the module search bar.")
        .defaultValue(8)
        .range(1, 12)
        .build()
    );

    private final Setting<String> httpUserAgent = sgMisc.add(new StringSetting.Builder()
        .name("HTTP-user-agent")
        .description("The User Agent applied to all outgoing HTTP requests.")
        .defaultValue(DEFAULT_USER_AGENT)
        .build()
    );

    public final Setting<AlignmentX> topBarHorizontalAlignment = sgTopBar.add(new EnumSetting.Builder<AlignmentX>()
        .name("horizontal-alignment")
        .description("Where the top bar should be placed horizontally.")
        .defaultValue(AlignmentX.Center)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<VerticalAlignment> topBarVerticalAlignment = sgTopBar.add(new EnumSetting.Builder<VerticalAlignment>()
        .name("vertical-alignment")
        .description("Where the top bar should be placed vertically.")
        .defaultValue(VerticalAlignment.Top) //not using AlignmentY because you shouldn't be able to put the top bar on the vertical center of your screen
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    @NotNull
    public static String getUserAgent() {
        return orDefault(c -> c.httpUserAgent.get(), DEFAULT_USER_AGENT);
    }

    @NotNull
    public static AlignmentX getTopBarAlignmentX() {
        return orDefault(c -> c.topBarHorizontalAlignment.get(), AlignmentX.Center);
    }

    @NotNull
    public static AlignmentY getTopBarAlignmentY() {
        return orDefault(c -> c.topBarVerticalAlignment.get().asMeteor(), AlignmentY.Top);
    }


    @NotNull
    private static <T> T orDefault(Function<@NotNull Config, T> valueGetter, @NotNull T defaultValue) {
        return Optional.ofNullable(get()).map(valueGetter).orElse(defaultValue);
    }


    public final Setting<Boolean> baritoneIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("baritone-icon")
        .description("Replace Baritone in top bar with the bass clef icon.")
        .defaultValue(true)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<Boolean> friendsIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("friends-icon")
        .description("Replace Friends in top bar with the friends icon.")
        .defaultValue(false)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<Boolean> profilesIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("profiles-icon")
        .description("Replace Profiles in top bar with a profile icon.")
        .defaultValue(false)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<Boolean> guiIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("GUI-icon")
        .description("Replace GUI in top bar with a GUI icon.")
        .defaultValue(false)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<Boolean> waypointsIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("waypoints-icon")
        .description("Replace Waypoints in top bar with a location pin icon.")
        .defaultValue(true)
        .onChanged(WTopBar::onBarPropertyChanged)
        .build()
    );

    public final Setting<Boolean> macrosIcon = sgTopBar.add(new BoolSetting.Builder()
        .name("macros-icon")
        .description("Replace Macros in top bar with a generic M icon.")
        .defaultValue(true)
        .onChanged(WTopBar::onBarPropertyChanged)
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

        return tag;
    }

    @Override
    public Config fromTag(NbtCompound tag) {
        if (tag.contains("settings")) settings.fromTag(tag.getCompound("settings"));
        if (tag.contains("dontShowAgainPrompts")) dontShowAgainPrompts = listFromTag(tag, "dontShowAgainPrompts");

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
