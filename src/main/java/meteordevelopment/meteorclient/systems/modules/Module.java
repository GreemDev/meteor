/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Module implements ISerializable<Module>, Comparable<Module> {
    protected final MinecraftClient mc;

    public final Category category;
    public final String name;
    public final String title;
    public final String description;
    public final Color color;

    public final Settings settings = new Settings();

    private boolean active;
    private boolean hidden = false;

    public boolean canBind = true;
    public boolean allowChatFeedback = true;
    public boolean forceDisplayChatFeedbackCheckbox = false;
    public boolean canActivate = true;

    public boolean serialize = true;
    public boolean runInMainMenu = false;
    public boolean autoSubscribe = true;

    public final Keybind keybind = Keybind.none();

    public boolean hasKeybind() {
        return keybind.isSet();
    }

    public boolean chatFeedback = true;
    public boolean favorite = false;

    protected boolean autoRegister = true;

    public boolean shouldAutoRegister() {
        return autoRegister;
    }

    public Module(Category category, String name, String description) {
        this.mc = MinecraftClient.getInstance();
        this.category = category;
        this.name = name;
        this.title = Utils.nameToTitle(name);
        this.description = description;
        this.color = Color.random();
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    public void onActivate() {}
    public void onDeactivate() {}

    public void enable() {
        if (!isActive()) toggle();
    }

    public void disable() {
        if (isActive()) toggle();
    }

    public void toggle() {
        if (!canActivate) return;

        if (!active) {
            active = true;
            Modules.get().addActive(this);

            settings.onActivated();

            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) MeteorClient.EVENT_BUS.subscribe(this);
                onActivate();
            }
        }
        else {
            if (runInMainMenu || Utils.canUpdate()) {
                if (autoSubscribe) MeteorClient.EVENT_BUS.unsubscribe(this);
                onDeactivate();
            }

            active = false;
            Modules.get().removeActive(this);
        }
    }

    public void sendToggledMsg() {
        if (Config.get().chatFeedback.get() && chatFeedback) {
            ChatUtils.forceNextPrefixClass(getClass());
            ChatUtils.sendMsg(this.hashCode(), Formatting.GRAY, "Toggled (highlight)%s(default) %s(default).", title, isActive() ? Formatting.GREEN + "on" : Formatting.RED + "off");
        }
    }

    public void sendVisibilityMsg() {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(this.hashCode(), Formatting.GRAY, "(highlight)%s(default) is now %s(default).", title, isActive() ? Formatting.GREEN + "visible" : Formatting.RED + "hidden");
    }

    public void info(Text message) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.sendMsg(title, message);
    }

    public void info(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.infoPrefix(title, message, args);
    }

    public void warning(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.warningPrefix(title, message, args);
    }

    public void error(String message, Object... args) {
        ChatUtils.forceNextPrefixClass(getClass());
        ChatUtils.errorPrefix(title, message, args);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isHidden() { return hidden; }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void toggleHidden() {
        this.hidden = !hidden;
    }

    public String getInfoString() {
        return null;
    }

    @Override
    public NbtCompound toTag() {
        if (!serialize) return null;
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        tag.put("keybind", keybind.toTag());
        tag.putBoolean("chatFeedback", chatFeedback);
        tag.putBoolean("favorite", favorite);
        tag.put("settings", settings.toTag());

        tag.putBoolean("active", active);
        tag.putBoolean("hidden", hidden);

        return tag;
    }

    @Override
    public Module fromTag(NbtCompound tag) {
        // General
        if (tag.contains("key")) keybind.set(true, tag.getInt("key"));
        else keybind.fromTag(tag.getCompound("keybind"));

        chatFeedback = !tag.contains("chatFeedback") || tag.getBoolean("chatFeedback");
        favorite = tag.getBoolean("favorite");

        // Settings
        NbtElement settingsTag = tag.get("settings");
        if (settingsTag instanceof NbtCompound compound)
            settings.fromTag(compound);

        if (tag.contains("hidden"))
            hidden = tag.getBoolean("hidden");

        boolean active = tag.getBoolean("active");
        if (active != isActive()) toggle();

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Module module = (Module) o;
        return Objects.equals(name, module.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(@NotNull Module o) {
        return name.compareTo(o.name);
    }
}
