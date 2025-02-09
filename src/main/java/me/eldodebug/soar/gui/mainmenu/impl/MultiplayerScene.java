package me.eldodebug.soar.gui.mainmenu.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;

public class MultiplayerScene extends MainMenuScene {

//    boolean

    SimpleAnimation joinServerAnimation = new SimpleAnimation();
    SimpleAnimation directConnectAnimation = new SimpleAnimation();
    SimpleAnimation addServerAnimation = new SimpleAnimation();
    SimpleAnimation editAnimation = new SimpleAnimation();
    SimpleAnimation deleteAnimation = new SimpleAnimation();
    SimpleAnimation refreshAnimation = new SimpleAnimation();
    SimpleAnimation cancleAnimation = new SimpleAnimation();

    public MultiplayerScene(GuiSoarMainMenu parent) {
        super(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        Soar instance = Soar.getInstance();
        NanoVGManager nvg = instance.getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG(nvg, mouseX, mouseY));
    }

    private void drawNanoVG(NanoVGManager nvg, int mouseX, int mouseY) {

        ScaledResolution sr = new ScaledResolution(mc);

        final float yBottom = sr.getScaledHeight();
        final float yMiddle = sr.getScaledHeight() / 2;
        final float xMiddle = sr.getScaledWidth() / 2;
        final float y_first = yBottom - 52;
        final float y_second = yBottom - 24;
        final float x_first = xMiddle - 154;
        drawButton(nvg, TranslateText.JOIN_SERVER.getText(), x_first, y_first, mouseX, mouseY, 100, 20, joinServerAnimation);
        drawButton(nvg, TranslateText.DIRECT_CONNECT.getText(), x_first + 104, y_first, mouseX, mouseY, 100, 20, directConnectAnimation);
        drawButton(nvg, TranslateText.ADD_SERVER.getText(), x_first + 208, y_first, mouseX, mouseY, 100, 20, addServerAnimation);
        drawButton(nvg, TranslateText.EDIT.getText(), x_first, y_second, mouseX, mouseY, 70, 20, editAnimation);
        drawButton(nvg, TranslateText.DELETE.getText(), x_first + 80, y_second, mouseX, mouseY, 70, 20, deleteAnimation);
        drawButton(nvg, TranslateText.REFRESH.getText(), xMiddle + 4, y_second, mouseX, mouseY, 70, 20, refreshAnimation);
        drawButton(nvg, TranslateText.CANCEL.getText(), xMiddle + 84, y_second, mouseX, mouseY, 70, 20, cancleAnimation);


    }
    @Override
    public void drawButton(NanoVGManager nvg, String text, float x, float y, int mouseX, int mouseY, int width, int height, SimpleAnimation animation) {

        boolean isHovered = MouseUtils.isInside(mouseX, mouseY, x - 90, y, width, height);
        Color backgroundColor = isHovered ? new Color(200, 200, 200) : this.getBackgroundColor();

        animation.setAnimation(isHovered ? 1.0F : 0.0F, 10);
        nvg.drawRoundedRect(x - 90, y, width, height, 4.5F, backgroundColor);
        nvg.drawCenteredText(text, x, y + 6.5F, isHovered ? new Color(255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200), 255 - (int) (animation.getValue() * 200)) : Color.white, 9.5F, Fonts.REGULAR);
    }

}
