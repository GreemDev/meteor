/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient;

import com.google.common.collect.ImmutableList;
import kotlin.collections.CollectionsKt;
import meteordevelopment.discordipc.DiscordIPC;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.gui.GuiThemes;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.mixin.accessor.KeyBindingAccessor;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.DiscordPresence;
import meteordevelopment.meteorclient.utils.*;
import meteordevelopment.meteorclient.utils.misc.Version;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.network.OnlinePlayers;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventBus;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import meteordevelopment.orbit.IEventBus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import net.greemdev.meteor.Greteor;
import net.greemdev.meteor.modules.MinecraftPresence;
import net.greemdev.meteor.util.meteor.Meteor;
import net.greemdev.meteor.util.misc.GVersioning;
import net.greemdev.meteor.utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static net.greemdev.meteor.util.accessors.modLoader;

public class MeteorClient implements ClientModInitializer {
    public static final String MOD_ID = "meteor-client";
    public static final String NAME;
    public static final ModMetadata MOD_META;

    public static CustomValue getMetaCustomValue(String jsonKey) {
        return MOD_META.getCustomValue(MOD_ID + ':' + jsonKey);
    }
    public static final Version VERSION;

    public static final Color COLOR;
    public static final int REVISION;
    public static final String KOTLIN_VERSION, FABRIC_KOTLIN_VERSION;

    public static MeteorClient INSTANCE;

    public static MinecraftClient mc;
    public static final IEventBus EVENT_BUS = new EventBus();
    public static final File FOLDER = FabricLoader.getInstance().getGameDir().resolve(MOD_ID).toFile();
    public static final Logger LOG;

    public static Person randomAuthor() {
        return utils.getRandomElement(MeteorClient.MOD_META.getAuthors());
    }

    public static ImmutableList<String> authors() {
        return MeteorClient.MOD_META.getAuthors().stream().map(Person::getName)
            .collect(ImmutableList.toImmutableList());
    }

    public static String fullVersion() {
        return "%s-rev%d".formatted(VERSION, REVISION);
    }

    static {
        MOD_META = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow().getMetadata();

        NAME = MOD_META.getName();
        LOG = LoggerFactory.getLogger(NAME);

        String versionString = MOD_META.getVersion().getFriendlyString();
        if (versionString.contains("-")) versionString = versionString.split("-")[0];

        // When building and running through IntelliJ and not Gradle it doesn't replace the version so just use a dummy
        if (versionString.equals("${version}")) versionString = "0.0.0";

        VERSION = new Version(versionString);
        REVISION = Integer.parseInt(getMetaCustomValue("revision").getAsString());
        COLOR = utils.colorOf(getMetaCustomValue("color").getAsString());
        FABRIC_KOTLIN_VERSION = getMetaCustomValue("kotlin").getAsString();
        KOTLIN_VERSION = FABRIC_KOTLIN_VERSION.split("kotlin")[1].substring(1);
    }

    @Override
    public void onInitializeClient() {
        if (INSTANCE == null) {
            INSTANCE = this;
            return;
        }

        LOG.info("Initializing {}", NAME);

        // Global minecraft client accessor
        mc = MinecraftClient.getInstance();

        // Pre-load
        //noinspection ResultOfMethodCallIgnored
        FOLDER.mkdirs();

        // Register event handlers
        registerLambdaFactoriesForPackages(MeteorClient.class.getPackageName(), Greteor.class.getPackageName());

        // Register init classes
        ReflectInit.registerPackages();

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
            if (Modules.get().isActive(DiscordPresence.class) || Modules.get().isActive(MinecraftPresence.class))
                DiscordIPC.stop();

            OnlinePlayers.leave();
            Systems.save();
            GuiThemes.save();
            GVersioning.revisionChecker().destroy();
        }));


        if (GVersioning.isOutdated() && !modLoader().isDevelopmentEnvironment())
            Greteor.logger().warn("Not currently on the latest revision! Running %d revisions behind. Latest is %s.".formatted(GVersioning.revisionsBehind(), GVersioning.latestRevision()));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.currentScreen == null && mc.getOverlay() == null && OPEN_COMMANDS.wasPressed()) {
            mc.setScreen(new ChatScreen(Commands.prefix()));
        }
    }

    @EventHandler
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Press && OPEN_GUI.matchesKey(event.key, 0)) {
            toggleGui();
        }
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && OPEN_GUI.matchesMouse(event.button)) {
            toggleGui();
        }
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
        if (GuiThemes.get().hideHUD()) {
            if (!wasWidgetScreen) wasHudHiddenRoot = mc.options.hudHidden;

            if (event.screen instanceof WidgetScreen) mc.options.hudHidden = true;
            else if (!wasHudHiddenRoot) mc.options.hudHidden = false;
        }

        wasWidgetScreen = event.screen instanceof WidgetScreen;
    }

    public static void registerLambdaFactoriesForPackages(String... packages) {
        Arrays.stream(packages).forEach(pkg ->
            EVENT_BUS.registerLambdaFactory(pkg, (lookupInMethod, klass) ->
                java.cast(lookupInMethod.invoke(null, klass, MethodHandles.lookup()))
            )
        );
    }

    // Key bindings

    public static final String KEYBIND_CATEGORY = Utils.nameToTitle(MOD_ID);

    public static KeyBinding OPEN_GUI = new KeyBinding("key.meteor-client.open-gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, KEYBIND_CATEGORY);
    public static KeyBinding OPEN_COMMANDS = new KeyBinding("key.meteor-client.open-commands", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_PERIOD, KEYBIND_CATEGORY);

    public static KeyBinding[] injectKeybinds(KeyBinding[] baseKeybinds) {
        { // add keybind screen category
            Map<String, Integer> categories = KeyBindingAccessor.getCategoryOrderMap();

            Integer highest = CollectionsKt.maxOrNull(categories.values());
            if (highest == null) highest = 0;

            categories.put(MeteorClient.KEYBIND_CATEGORY, highest + 1);
        }


        //doing the reflection to get all static KeyBinding fields was cleaner to impl in kotlin (no try block), so it's not in this class
        List<KeyBinding> meteorBinds = Greteor.keybinds();

        // Add key binding
        KeyBinding[] newBinds = new KeyBinding[baseKeybinds.length + meteorBinds.size()];

        System.arraycopy(baseKeybinds, 0, newBinds, 0, baseKeybinds.length);

        for (int i = 0; i < meteorBinds.size(); i++)
            newBinds[baseKeybinds.length + i] = meteorBinds.get(i);

        return newBinds;
    }
}
