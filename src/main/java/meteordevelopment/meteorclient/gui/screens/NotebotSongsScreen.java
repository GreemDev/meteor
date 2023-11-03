/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.Notebot;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.notebot.decoder.SongDecoders;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class NotebotSongsScreen extends WindowScreen {
    private static final Notebot notebot = Modules.get().get(Notebot.class);

    private WTextBox filter;
    private String filterText = "";

    private WTable table;

    public NotebotSongsScreen(GuiTheme theme) {
        super(theme, "Notebot Songs");
    }

    @Override
    public void initWidgets() {
        // Random Song
        add(theme.button("Random Song", notebot::playRandomSong)).minWidth(400).expandX();

        // Filter
        filter = add(theme.textBox("", "Search for the songs...")).minWidth(400).expandX().widget();
        filter.toggleFocusing().action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initSongsTable();
        };

        table = add(theme.table()).widget();

        initSongsTable();
    }

    private void initSongsTable() {
        AtomicBoolean noSongsFound = new AtomicBoolean(true);
        try (Stream<Path> paths = Files.list(MeteorClient.FOLDER.toPath().resolve("notebot"))) {
            paths.forEach(path -> {
                if (SongDecoders.hasDecoder(path)) {
                    String name = path.getFileName().toString();

                    if (Utils.searchTextDefault(name, filterText, false)) {
                        addPath(path);
                        noSongsFound.set(false);
                    }
                }
            });
        } catch (IOException e) {
            table.add(theme.label("Missing &zmeteor-client/notebot&r folder.")).expandCellX();
            table.row();
        }

        if (noSongsFound.get())
            table.add(theme.label("No songs found.")).expandCellX().center();
    }

    private void addPath(Path path) {
        table.add(theme.horizontalSeparator()).expandX().minWidth(400);
        table.row();

        table.add(theme.label(FilenameUtils.getBaseName(path.getFileName().toString()))).expandCellX();
        table.add(theme.button("Load", () -> {
            notebot.loadSong(path.toFile());
        })).right();
        table.add(theme.button("Preview", () -> {
            notebot.previewSong(path.toFile());
        })).right();

        table.row();
    }
}
