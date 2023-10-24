/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render.color;

public class RainbowColor extends Color {

    public static final RainbowColor GLOBAL = new RainbowColor();

    public static SettingColor asSetting() {
        return GLOBAL.toSetting();
    }

    public static RainbowColor next() {
        return next(1);
    }

    public static RainbowColor next(double delta) {
        return GLOBAL.getNext(delta);
    }

    public static double speed() {
        return GLOBAL.getSpeed();
    }

    public static RainbowColor speed(double speed) {
        if (speed != GLOBAL.speed)
            GLOBAL.setSpeed(speed);

        return GLOBAL;
    }

    private double speed;
    private static final float[] hsb = new float[3];

    public RainbowColor() {
        super();
    }

    public double getSpeed() {
        return speed;
    }

    public RainbowColor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public RainbowColor getNext() {
        return getNext(1);
    }

    public RainbowColor getNext(double delta) {
        if (speed > 0) {
            java.awt.Color.RGBtoHSB(r, g, b, hsb);
            int c = java.awt.Color.HSBtoRGB(hsb[0] + (float) (speed * delta), 1, 1);

            r = toRGBAR(c);
            g = toRGBAG(c);
            b = toRGBAB(c);
        }
        return this;
    }

    public RainbowColor set(RainbowColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.speed = color.speed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        return Double.compare(((RainbowColor) o).speed, speed) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(speed);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
