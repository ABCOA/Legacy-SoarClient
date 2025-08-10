package me.eldodebug.soar.gui;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

import java.awt.*;

public class Button {
    private SimpleAnimation animation = new SimpleAnimation();

    public SimpleAnimation getAnimation() {
        return animation;
    }

    public static void drawButton(NanoVGManager nvg, String text, float x, float y, int mouseX, int mouseY, int width, int height, SimpleAnimation animation, Color backgroundColor) {
        boolean isHovered = MouseUtils.isInside(mouseX, mouseY, x - 90, y, width, height);
        AccentColor accentColor = Soar.getInstance().getColorManager().getCurrentColor();
        int alpha = (int) (animation.getValue() * 255);

        animation.setAnimation(isHovered ? 1.0F : 0.0F, 10);

        if (isHovered) {
            nvg.save();
            float fillWidth = animation.getValue() * width; // 动态调整填充宽度
            nvg.drawRoundedRect(x - 90, y, fillWidth, height, 4.5F, Color.LIGHT_GRAY); // 从左到右填充
            nvg.drawGradientOutlineRoundedRect(x - 90, y, width, height, 4.5F, 1.5F, ColorUtils.applyAlpha(accentColor.getColor1(), alpha), ColorUtils.applyAlpha(accentColor.getColor2(), alpha));
        }
        nvg.drawRoundedRect(x - 90, y, width, height, 4.5F, backgroundColor);
        nvg.drawCenteredText(text, x, y + 6.5F, isHovered ? new Color(255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200)) : Color.white, 9.5F, Fonts.REGULAR);
        nvg.restore();
    }
}
