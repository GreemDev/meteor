/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiDebugRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CursorStyle;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;
import static meteordevelopment.meteorclient.utils.Utils.getWindowHeight;
import static meteordevelopment.meteorclient.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.*;

public abstract class WidgetScreen extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();

    public Runnable taskAfterRender;
    protected Runnable enterAction;

    public Screen parent;
    protected final WContainer root;

    protected final GuiTheme theme;

    public boolean locked, lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;

    private double lastMouseX, lastMouseY;

    public double animProgress;

    private List<Runnable> onClosed;

    protected boolean firstInit = true;

    public WidgetScreen(GuiTheme theme, String title) {
        super(Text.literal(title));

        this.parent = mc.currentScreen;
        this.root = new WFullScreenRoot();
        this.theme = theme;

        root.theme = theme;

        if (parent != null) {
            animProgress = 1;

            if (this instanceof TabScreen && parent instanceof TabScreen) {
                parent = ((TabScreen) parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return root.add(widget);
    }

    protected <W extends WContainer> void within(W container, Consumer<W> widgets) {
        widgets.accept(container);
    }

    protected <W extends WContainer> void within(Cell<W> containerCell, Consumer<W> widgets) {
        widgets.accept(containerCell.widget());
    }

    protected <W extends WContainer> void add(W container, BiConsumer<Cell<W>, W> containerModifier) {
        Cell<W> cell = add(container);
        within(cell.widget(), c ->
            containerModifier.accept(cell, c)
        );
    }


    public void clear() {
        root.clear();
    }

    public void invalidate() {
        root.invalidate();
    }

    @Override
    protected void init() {
        MeteorClient.EVENT_BUS.subscribe(this);

        closed = false;

        if (firstInit) {
            firstInit = false;
            initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        clear();
        initWidgets();
    }

    public void reloadParent() {
        if (parent != null && parent instanceof WidgetScreen ws)
            ws.reload();
    }

    public void onClosed(Runnable action) {
        if (onClosed == null) onClosed = new ArrayList<>(2);
        onClosed.add(action);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (locked) return false;

        mouseX *= mc.getWindow().getScaleFactor();
        mouseY *= mc.getWindow().getScaleFactor();

        return root.mouseClicked(mouseX, mouseY, button, false);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (locked) return false;

        mouseX *= mc.getWindow().getScaleFactor();
        mouseY *= mc.getWindow().getScaleFactor();

        return root.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (locked) return;

        mouseX *= mc.getWindow().getScaleFactor();
        mouseY *= mc.getWindow().getScaleFactor();

        root.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);

        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        if (locked) return false;

        root.mouseScrolled(amount);

        return super.mouseScrolled(mouseX, mouseY, amount);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (locked) return false;

        if (Input.isCtrl(modifiers) && keyCode == GLFW_KEY_9) {
            debug = !debug;
            return true;
        }

        if ((keyCode == GLFW_KEY_ENTER || keyCode == GLFW_KEY_KP_ENTER) && enterAction != null) {
            enterAction.run();
            return true;
        }

        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (locked) return false;

        boolean shouldReturn = root.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        if (shouldReturn) return true;

        // Select next text box if TAB was pressed
        if (keyCode == GLFW_KEY_TAB) {
            AtomicReference<WTextBox> firstTextBox = new AtomicReference<>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);

            root.forEachWidget(widget -> {
                if (done.get() || !(widget instanceof WTextBox textBox)) return;

                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();

                    done.set(true);
                }
                else {
                    if (textBox.isFocused()) {
                        textBox.setFocused(false);
                        foundFocused.set(true);
                    }
                }

                if (firstTextBox.get() == null) firstTextBox.set(textBox);
            });

            if (!done.get() && firstTextBox.get() != null) {
                firstTextBox.get().setFocused(true);
                firstTextBox.get().setCursorMax();
            }

            return true;
        }


        if (Input.isCtrl(modifiers) && keyCode == GLFW_KEY_C && toClipboard()) {
            return true;
        }
        else if (Input.isCtrl(modifiers) && keyCode == GLFW_KEY_V && fromClipboard()) {
            reload();
            if (parent instanceof WidgetScreen wScreen) {
                wScreen.reload();
            }
            return true;
        }

        return false;
    }

    public void keyRepeated(int key, int modifiers) {
        if (locked) return;

        root.keyRepeated(key, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (locked) return false;

        return root.charTyped(chr);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) renderBackground(context);

        double s = mc.getWindow().getScaleFactor();
        mouseX *= (int) mc.getWindow().getScaleFactor();
        mouseY *= (int) mc.getWindow().getScaleFactor();

        animProgress += delta / 20 * 14;
        animProgress = MathHelper.clamp(animProgress, 0, 1);

        GuiKeyEvents.canUseKeys = true;

        // Apply projection without scaling
        Utils.unscaledProjection();

        onRenderBefore(context, delta);

        RENDERER.theme = theme;
        theme.beforeRender();

        RENDERER.begin(context);
        RENDERER.setAlpha(animProgress);
        root.render(RENDERER, mouseX, mouseY, delta / 20);
        RENDERER.setAlpha(1);
        RENDERER.end();

        boolean tooltip = RENDERER.renderTooltip(context, mouseX, mouseY, delta / 20);

        if (debug) {
            DEBUG_RENDERER.render(root, context.getMatrices());
            if (tooltip)
                DEBUG_RENDERER.render(RENDERER.tooltipWidget, context.getMatrices());
        }

        Utils.scaledProjection();

        runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (taskAfterRender != null) {
            taskAfterRender.run();
            taskAfterRender = null;
        }
    }

    protected void onRenderBefore(DrawContext drawContext, float delta) {}

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        root.invalidate();
    }

    @Override
    public void close() {
        if (!locked || lockedAllowClose) {
            boolean preOnClose = onClose;
            onClose = true;

            removed();

            onClose = preOnClose;
        }
    }

    @Override
    public void removed() {
        if (!closed || lockedAllowClose) {
            closed = true;
            onClosed();

            Input.setCursorStyle(CursorStyle.Default);

            root.forEachWidget(widget -> {
                if (widget instanceof WTextBox textBox && textBox.isFocused())
                    textBox.setFocused(false);
            });

            MeteorClient.EVENT_BUS.unsubscribe(this);
            GuiKeyEvents.canUseKeys = true;

            if (onClosed != null) {
                for (Runnable action : onClosed) action.run();
            }

            if (onClose) {
                taskAfterRender = () -> {
                    locked = true;
                    mc.setScreen(parent);
                };
            }
        }
    }

    protected void onClosed() {}

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return !locked || lockedAllowClose;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static class WFullScreenRoot extends WContainer implements WRoot {
        private boolean valid;

        @Override
        public void invalidate() {
            valid = false;
        }

        @Override
        protected void onCalculateSize() {
            width = getWindowWidth();
            height = getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell<?> cell : cells) {
                cell.x = 0;
                cell.y = 0;

                cell.width = width;
                cell.height = height;

                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!valid) {
                calculateSize();
                calculateWidgetPositions();

                valid = true;
                mouseMoved(mc.mouse.getX(), mc.mouse.getY(), mc.mouse.getX(), mc.mouse.getY());
            }

            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}
