/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;

public class WKeybind extends WHorizontalList {
    public Runnable action;
    public Runnable actionOnSet;

    private WLabel label;
    private WCheckbox onRelease;

    private final Keybind keybind;
    private final Keybind defaultValue;
    private boolean listening;

    public boolean module = false;

    public WKeybind(Keybind keybind, Keybind defaultValue) {
        this.keybind = keybind;
        this.defaultValue = defaultValue;
    }

    @Override
    public void init() {
        if (!module)
            add(theme.label(" "));
        label = add(theme.label(fixLabel(keybind.toString()))).widget();
        add(theme.button("Set", () -> {
            listening = true;
            setLabel("...");

            if (actionOnSet != null) actionOnSet.run();
        }));

        WLabel release = add(theme.label("  Released: ")).widget();
        release.tooltip = "Activate this keybind when the specified key/mouse button is &zreleased&r.";

        onRelease = add(theme.checkbox(keybind.onRelease, (c) -> keybind.onRelease = c)).widget();

        add(theme.button(GuiRenderer.RESET, this::resetBind)).expandCellX().right();
    }

    public WKeybind onSet(Runnable onSet) {
        actionOnSet = onSet;
        return this;
    }

    public boolean onAction(boolean isKey, int value) {
        if (listening && keybind.canBindTo(isKey, value)) {
            keybind.set(isKey, value);
            reset();

            return true;
        }

        return false;
    }

    public void resetBind() {
        keybind.set(defaultValue);
        onRelease.checked = defaultValue.onRelease;
        reset();
    }

    public void reset() {
        listening = false;
        refreshLabel();
        if (Modules.get().isBinding())
            Modules.get().setModuleToBind(null);
    }

    private void refreshLabel() {
        setLabel(keybind.toString());
    }

    private void setLabel(String label) {
        this.label.set(fixLabel(label));
    }

    private String fixLabel(String label) {
        return module
            ? "Toggle: " + label
            : label;
    }
}
