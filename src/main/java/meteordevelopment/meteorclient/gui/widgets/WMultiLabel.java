/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.gui.widgets;

import net.greemdev.meteor.util.meteor.LegacyText;

import java.util.ArrayList;
import java.util.List;

public abstract class WMultiLabel extends WLabel {
    protected List<String> lines = new ArrayList<>(2);

    protected double maxWidth;

    public WMultiLabel(String text, boolean title, double maxWidth) {
        super(text, title);

        this.maxWidth = maxWidth;
    }

    @Override
    public String get() {
        return text;
    }

    @Override
    protected void onCalculateSize() {
        lines.clear();

        String[] words = (
            LegacyText.needsSpecialRenderer(text)
                ? LegacyText.getColorCodeRegex().replace(text, "")
                : text
            ).split(" ");

        StringBuilder sb = new StringBuilder();

        double spaceWidth = theme.textWidth(" ", 1, title);
        double maxWidth = theme.scale(this.maxWidth);

        double lineWidth = 0;
        double maxLineWidth = 0;

        int iInLine = 0;

        for (int i = 0; i < words.length; i++) {
            double wordWidth = theme.textWidth(words[i], words[i].length(), title);

            double toAdd = wordWidth;
            if (iInLine > 0) toAdd += spaceWidth;

            if (lineWidth + toAdd > maxWidth) {
                lines.add(sb.toString());
                sb.setLength(0);

                lineWidth = 0;
                iInLine = 0;

                i--;
            }
            else {
                if (iInLine > 0) {
                    sb.append(' ');
                    lineWidth += spaceWidth;
                }

                sb.append(words[i]);
                lineWidth += wordWidth;

                maxLineWidth = Math.max(maxLineWidth, lineWidth);
                iInLine++;
            }
        }

        if (!sb.isEmpty()) lines.add(sb.toString());

        width = maxLineWidth;
        height = theme.textHeight(title) * lines.size();
    }

    @Override
    public void set(String text) {
        if (!text.equals(this.text)) invalidate();

        this.text = text;
    }
}
