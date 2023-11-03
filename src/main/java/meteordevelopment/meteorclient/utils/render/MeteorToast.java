/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.greemdev.meteor.util.meteor.Meteor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MeteorToast implements Toast {
    public static SoundInstance DEFAULT_SOUND = PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 1.2f, 1);
    public static Color getTitleColor() {
        return Meteor.currentTheme().titleTextColor();
    }
    public static Color getTextColor() {
        return Meteor.currentTheme().textColor();
    }

    private ItemStack icon;
    private Text title, text;
    private boolean justUpdated = true, playedSound;
    private final SoundInstance sound;
    private long start, duration;

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text, SoundInstance sound, long duration) {
        this.icon = item != null ? item.getDefaultStack() : null;
        this.title = Text.literal(title).setStyle(Style.EMPTY.withColor(getTitleColor().toTextColor()));
        this.text = text != null ? Text.literal(text).setStyle(Style.EMPTY.withColor(getTextColor().toTextColor())) : null;
        this.duration = duration;
        this.sound = sound;
    }

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text, long duration) {
        this(item, title, text, DEFAULT_SOUND, duration);
    }

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text) {
        this(item, title, text, 6000);
    }

    public MeteorToast(@Nullable Item item, @NotNull String title, @Nullable String text, SoundInstance sound) {
        this(item, title, text, sound, 6000);
    }

    @Override
    public Visibility draw(DrawContext context, ToastManager toastManager, long currentTime) {
        if (justUpdated) {
            start = currentTime;
            justUpdated = false;
        }

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        context.drawTexture(TEXTURE, 0, 0, 0, 0, getWidth(), getHeight());

        int x = icon != null ? 28 : 12;
        int titleY = 12;

        if (text != null) {
            context.drawText(mc.textRenderer, text, x, 18, getTextColor().getPacked(), false);
            titleY = 7;
        }

        context.drawText(mc.textRenderer, title, x, titleY, getTitleColor().getPacked(), false);

        if (icon != null) context.drawItem(icon, 8, 8);

        if (!playedSound) {
            mc.getSoundManager().play(getSound());
            playedSound = true;
        }

        return currentTime - start >= duration ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
    }

    public void setIcon(@Nullable Item item) {
        this.icon = item != null ? item.getDefaultStack() : null;
        justUpdated = true;
    }

    public void setTitle(@NotNull String title) {
        this.title = Text.literal(title).setStyle(Style.EMPTY.withColor(getTitleColor().toTextColor()));
        justUpdated = true;
    }

    public void setText(@Nullable String text) {
        this.text = text != null ? Text.literal(text).setStyle(Style.EMPTY.withColor(getTextColor().toTextColor())) : null;
        justUpdated = true;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        justUpdated = true;
    }

    public final SoundInstance getSound() {
        return sound;
    }
}
