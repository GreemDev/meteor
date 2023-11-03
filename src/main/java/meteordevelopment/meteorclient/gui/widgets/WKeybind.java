/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import kotlin.text.StringsKt;
import meteordevelopment.meteorclient.gui.WidgetScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.gui.widgets.pressable.WCheckbox;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.misc.Keybind;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class WKeybind extends WHorizontalList {
    public Runnable action;
    public Runnable actionOnSet;

    public WKeybind action(Runnable action) {
        this.action = action;
        return this;
    }

    public WKeybind onSet(Runnable action) {
        this.actionOnSet = action;
        return this;
    }

    private WLabel label;

    private final Keybind keybind;
    private final Keybind defaultValue;
    private boolean listening;

    private final boolean addResetButton;

    public boolean module = false;

    public WKeybind(Keybind keybind, Keybind defaultValue, boolean resetButton) {
        this.keybind = keybind;
        this.defaultValue = defaultValue;
        this.addResetButton = resetButton;
    }

    @Override
    public void init() {
        if (module)
            add(theme.label("Toggle: "));

        label = add(theme.label(keybind.toString())).widget();
        add(theme.button("Set", () -> {
            listening = true;
            setLabel("...");

            if (actionOnSet != null) actionOnSet.run();
        }));

        add(theme.label("  Released: ", "Activate this keybind when the specified key/mouse button is &zreleased&r."));

        add(theme.checkbox(keybind.onRelease, (c) -> keybind.onRelease = c));

        if (addResetButton)
            add(theme.resetButton(this::resetBind)).expandCellX().right();

        refreshLabel();
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
        reset();
    }

    public void reset() {
        listening = false;
        refreshLabel();
        if (Modules.get().isBinding()) {
            Modules.get().setModuleToBind(null);
        }
    }

    private void refreshLabel() {
        setLabel("  " + keybind.toString());
    }

    private void setLabel(String label) {
        this.label.set(StringsKt.padEnd(label, 10, ' '));
    }
}
