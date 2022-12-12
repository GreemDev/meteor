/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient;

import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.utils.*;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.misc.input.KeyBinds;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.greemdev.meteor.Greteor;
import net.greemdev.meteor.utils;
import net.greemdev.meteor.util.meteor.Meteor;
import net.greemdev.meteor.util.misc.GVersioning;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MeteorClient implements ClientModInitializer {
    public static final String MOD_ID = "meteor-client";
    public static final ModMetadata MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).get().getMetadata();
    public final static Version VERSION;

    public final static Color COLOR;
    public static int REVISION;
    public final static String KOTLIN, FABRIC_KOTLIN;

    public static MinecraftClient mc;
    public static MeteorClient INSTANCE;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = new File(FabricLoader.getInstance().getGameDir().toString(), MOD_ID);
    public static final Logger LOG = LoggerFactory.getLogger("Meteor");

    public static String fullVersion() {
        return "%s-rev%d".formatted(VERSION, REVISION);
    }

    static {
        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        VERSION = new Version(versionString);
        REVISION = Integer.parseInt(MOD_META.getCustomValue(MOD_ID + ":revision").getAsString());
        COLOR = utils.colorOf(MOD_META.getCustomValue(MOD_ID + ":color").getAsString());
        FABRIC_KOTLIN = MOD_META.getCustomValue(MOD_ID + ":kotlin").getAsString();
        KOTLIN = FABRIC_KOTLIN.split("kotlin")[1].substring(1);
    }

    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }

        // Global minecraft client accessor
        mc = MinecraftClient.getInstance();

        // Pre-load
        if (!FOLDER.exists()) {
            FOLDER.getParentFile().mkdirs();
            FOLDER.mkdir();
        }

        // Register event handlers
        Greteor.lambdaFactoriesFor("meteordevelopment.meteorclient", "net.greemdev.meteor");

        // Pre init
        ReflectInit.init(PreInit.class);

        // Register module categories
        Categories.init();

        // Load systems
        Systems.init();

        // Load Greteor features
        Greteor.init();

        // Subscribe after systems are loaded
        EVENT_BUS.subscribe(this);

        // Load configs
        Systems.load();

        // Post init
        ReflectInit.init(PostInit.class);

        // Save on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
        }));

        SharedConstants.isDevelopment = Boolean.parseBoolean(System.getProperty("meteor.minecraft.dev", "false"));
        if (SharedConstants.isDevelopment)
            LOG.warn("Property 'meteor.minecraft.dev' is 'true'; Now running in development mode.");

        if (GVersioning.isOutdated() && !SharedConstants.isDevelopment)
            LOG.warn("Not currently on the latest revision! Running %d revisions behind. Latest is %s.".formatted(GVersioning.revisionsBehind(), GVersioning.getLatestRevision()));
    }

    @EventHandler
    private void onTick(TickEvent.Post ignored) {
        if (mc.currentScreen == null && mc.getOverlay() == null && KeyBinds.OPEN_COMMANDS.wasPressed())
            mc.setScreen(new ChatScreen(Config.get().prefix.get()));
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesKey(event.key, 0))
            toggleGui();
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && KeyBinds.OPEN_GUI.matchesMouse(event.button))
            toggleGui();
    }

    private void toggleGui() {
        if (mc.currentScreen instanceof TabScreen ts)
            ts.close();
        else if (Utils.canOpenGui())
            Tabs.getTabToOpen().openScreen(Meteor.currentTheme());
    }

    // Hide HUD

    private boolean wasWidgetScreen, wasHudHiddenRoot;

    @EventHandler(priority = EventPriority.LOWEST)
    private void onOpenScreen(OpenScreenEvent event) {
        boolean hideHud = GuiThemes.get().hideHUD();

        if (hideHud) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hudHidden;

            if (event.screen instanceof WidgetScreen) mc.options.hudHidden = true;
            else if (!wasHudHiddenRoot) mc.options.hudHidden = false;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }
}
