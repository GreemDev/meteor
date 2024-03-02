/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render.color;

import com.google.common.base.Suppliers;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.greemdev.meteor.util.Strings;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.greemdev.meteor.utils;
import org.joml.Vector4f;

import java.util.Random;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class Color implements ICopyable<Color>, ISerializable<Color> {

    public static final Color WHITE = new Color();
    public static final Color LIGHT_GRAY = new Color(java.awt.Color.LIGHT_GRAY);
    public static final Color GRAY = new Color(java.awt.Color.GRAY);
    public static final Color DARK_GRAY = new Color(java.awt.Color.DARK_GRAY);
    public static final Color BLACK = new Color(java.awt.Color.BLACK);
    public static final Color RED = new Color(java.awt.Color.RED);
    public static final Color PINK = new Color(java.awt.Color.PINK);
    public static final Color ORANGE = new Color(java.awt.Color.ORANGE);
    public static final Color YELLOW = new Color(java.awt.Color.YELLOW);
    public static final Color GREEN = new Color(java.awt.Color.GREEN);
    public static final Color MAGENTA = new Color(java.awt.Color.MAGENTA);
    public static final Color CYAN = new Color(java.awt.Color.CYAN);
    public static final Color BLUE = new Color(java.awt.Color.BLUE);
    public static final Color HYPERLINK_BLUE = new Color(0x0000EE);

    public int r, g, b, a;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int r, int g, int b) {
        this(r, g, b, 255);
    }

    public Color(float r, float g, float b, float a) {
        this((int)(r*255), (int)(g*255), (int)(b*255), (int)(a*255));
    }

    public Color(Vector4f components) {
        this(components.x(), components.y(), components.z(), components.w());
    }

    public Color(Vector3f components) {
        this(components.x(), components.y(), components.z(), 1f);
    }

    public Color(int packed) {
        this(toRGBAR(packed), toRGBAG(packed), toRGBAB(packed), toRGBAA(packed));
    }
    public Color(int packed, int overrideAlpha) {
        this(packed);
        this.a = overrideAlpha;
    }

    public Color(Color color) {
        this(color.r, color.g, color.b, color.a);
    }

    public Color(java.awt.Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public Color(Formatting formatting) {
        this(
            formatting.isColor() ? toRGBAR(formatting.getColorValue()) : 255,
            formatting.isColor() ? toRGBAG(formatting.getColorValue()) : 255,
            formatting.isColor() ? toRGBAB(formatting.getColorValue()) : 255,
            formatting.isColor() ? toRGBAA(formatting.getColorValue()) : 255
        );
    }

    public Color(Style style) {
        this(style.getColor());
    }

    public Color(TextColor textColor) {
        this(
            textColor != null ? toRGBAR(textColor.getRgb()) : 255,
            textColor != null ? toRGBAG(textColor.getRgb()) : 255,
            textColor != null ? toRGBAB(textColor.getRgb()) : 255,
            textColor != null ? toRGBAA(textColor.getRgb()) : 255
        );
    }

    public Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        validate();
    }

    public static int fromRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + (b) + (a << 24);
    }

    public static int toRGBAR(int color) {
        return (color >> 16) & 0x000000FF;
    }
    public static int toRGBAG(int color) {
        return (color >> 8) & 0x000000FF;
    }
    public static int toRGBAB(int color) {
        return (color) & 0x000000FF;
    }
    public static int toRGBAA(int color) {
        return (color >> 24) & 0x000000FF;
    }

    public static Color fromHsv(double h, double s, double v) {
        double hh, p, q, t, ff;
        int i;
        double r, g, b;

        if (s <= 0.0) {       // < is bogus, just shuts up warnings
            r = v;
            g = v;
            b = v;
            return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
        }

        hh = h;
        if (hh >= 360.0) hh = 0.0;
        hh /= 60.0;
        i = (int) hh;
        ff = hh - i;
        p = v * (1.0 - s);
        q = v * (1.0 - (s * ff));
        t = v * (1.0 - (s * (1.0 - ff)));

        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;

            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
        return new Color((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
    }

    public static Color fromString(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4)
            throw new IllegalArgumentException("Invalid RGB(A) number sequence provided.");

        var color = new Color();
        try {
            color.r = Integer.parseInt(split[0]);
            color.g = Integer.parseInt(split[1]);
            color.b = Integer.parseInt(split[2]);

            if (split.length == 4)
                color.a = Integer.parseInt(split[3]);

            return color;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid RGB(A) number sequence provided.", e);
        }
    }


    private static final Supplier<Random> _rand = Suppliers.memoize(Random::new);

    public static Color random() {
        Random rand = _rand.get();
        return new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1f);
    }

    public Color set(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        validate();

        return this;
    }

    public Color r(int r) {
        this.r = r;
        validate();
        return this;
    }

    public Color g(int g) {
        this.g = g;
        validate();
        return this;
    }

    public Color b(int b) {
        this.b = b;
        validate();
        return this;
    }

    public Color a(int a) {
        this.a = a;
        validate();
        return this;
    }

    @Override
    public Color set(Color value) {
        r = value.r;
        g = value.g;
        b = value.b;
        a = value.a;

        validate();

        return this;
    }

    public boolean parse(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4) return false;

        try {
            // Not assigned directly because of exception handling
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            int a = split.length == 4 ? Integer.parseInt(split[3]) : this.a;

            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;

            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public Color copy() {
        return new Color(r, g, b, a);
    }

    public SettingColor toSetting() {
        return new SettingColor(r, g, b, a);
    }

    public TextColor toTextColor() {
        return TextColor.fromRgb(getPacked());
    }

    public Vector3f toVector3f() {
        return new Vector3f(r / 255f, g / 255f, b / 255f);
    }

    public Vector4f toVector4f() {
        return new Vector4f(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    public Style toStyle() {
        return Style.EMPTY.withColor(toTextColor());
    }

    public Style styleWith(Style style) {
        return style.withColor(toTextColor());
    }

    public String hexString() {
        return Strings.buildString("#%02x%02x%02x".formatted(r, g, b), sb -> {
            if (a != 255)
                sb.append("%02x".formatted(a));
        }).toUpperCase();
    }

    public java.awt.Color awt() {
        validate();
        return new java.awt.Color(r, g, b, a);
    }

    public Color darker() {
        return set(utils.meteor(awt().darker()));
    }

    public Color brighter() {
        return set(utils.meteor(awt().brighter()));
    }

    public void validate() {
        if (r < 0) r = 0;
        else if (r > 255) r = 255;

        if (g < 0) g = 0;
        else if (g > 255) g = 255;

        if (b < 0) b = 0;
        else if (b > 255) b = 255;

        if (a < 0) a = 0;
        else if (a > 255) a = 255;
    }

    public Vec3d getVec3d() {
        return new Vec3d(r / 255.0, g / 255.0, b / 255.0);
    }

    public Vector3f getVec3f() {
        return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    public int getPacked() {
        return fromRGBA(r, g, b, a);
    }

    public float red() {
        return r / 255f;
    }

    public float green() {
        return g / 255f;
    }

    public float blue() {
        return b / 255f;
    }

    public float alpha() {
        return a / 255f;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putInt("r", r);
        tag.putInt("g", g);
        tag.putInt("b", b);
        tag.putInt("a", a);

        return tag;
    }

    @Override
    public Color fromTag(NbtCompound tag) {
        r = tag.getInt("r");
        g = tag.getInt("g");
        b = tag.getInt("b");
        a = tag.getInt("a");

        validate();
        return this;
    }

    @Override
    public String toString() {
        return "%d %d %d".formatted(r, g, b);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        return r == color.r && g == color.g && b == color.b && a == color.a;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        result = 31 * result + a;
        return result;
    }
}
