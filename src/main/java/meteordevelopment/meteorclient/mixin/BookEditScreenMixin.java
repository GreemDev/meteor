/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import it.unimi.dsi.fastutil.io.FastByteArrayOutputStream;
import meteordevelopment.meteorclient.MeteorClient;
import net.greemdev.meteor.util.Strings;
import net.greemdev.meteor.util.misc.Nbt;
import net.greemdev.meteor.util.misc.NbtUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static meteordevelopment.meteorclient.MeteorClient.mc;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    @Shadow @Final private List<String> pages;
    @Shadow private int currentPage;
    @Shadow private boolean dirty;

    public BookEditScreenMixin(Text title) {
        super(title);
    }

    @Shadow protected abstract void updateButtons();

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        addDrawableChild(
            new ButtonWidget.Builder(Text.literal("Copy"), button -> {
                NbtList pagesNbt = Nbt.newList(listTag ->
                    pages.stream()
                        .map(NbtString::of)
                        .forEach(listTag::add)
                );

                NbtCompound bookNbt = Nbt.newCompound(tag -> {
                    tag.put("pages", pagesNbt);
                    tag.putInt("currentPage", currentPage);
                });

                try (FastByteArrayOutputStream bytes = new FastByteArrayOutputStream()) {
                    try (DataOutputStream out = new DataOutputStream(bytes)) {
                        Throwable err = NbtUtil.write(out, bookNbt);
                        if (err != null) //noinspection CallToPrintStackTrace
                            err.printStackTrace();
                    }

                    GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), Base64.getEncoder().encodeToString(bytes.array));
                } catch (Throwable e) {
                    MeteorClient.LOG.error("Error occurred releasing output stream or copying NBT to clipboard", e);
                    GLFW.glfwSetClipboardString(mc.getWindow().getHandle(), e.toString());
                }
            })
                .position(4, 4)
                .size(120, 20)
                .build()
        );

        addDrawableChild(
                new ButtonWidget.Builder(Text.literal("Paste"), button -> {
                    String clipboard = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
                    if (clipboard == null) return;

                    byte[] bytes;
                    try {
                        bytes = Base64.getDecoder().decode(clipboard);
                    } catch (IllegalArgumentException ignored) {
                        return;
                    }
                    DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes));

                    try {
                        NbtCompound tag = NbtIo.readCompressed(in);

                        NbtList listTag = tag.getList("pages", NbtElement.STRING_TYPE).copy();

                        pages.clear();
                        for(int i = 0; i < listTag.size(); ++i)
                            pages.add(listTag.getString(i));

                        if (pages.isEmpty())
                            pages.add(Strings.empty);


                        currentPage = tag.getInt("currentPage");

                        dirty = true;
                        updateButtons();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                })
                .position(4, 4 + 20 + 2)
                .size(120, 20)
                .build()
        );
    }
}
