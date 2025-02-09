package me.eldodebug.soar.gui.mainmenu.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import net.minecraft.client.gui.ScaledResolution;

public class SelectWorldScene extends MainMenuScene {

    public SelectWorldScene(GuiSoarMainMenu parent) {
        super(parent);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

            Soar instance = Soar.getInstance();
            NanoVGManager nvg = instance.getNanoVGManager();

            nvg.setupAndDraw(() -> drawNanoVG(nvg));
    }

    private void drawNanoVG(NanoVGManager nvg) {

        ScaledResolution sr = new ScaledResolution(mc);

        float xMid = sr.getScaledWidth() / 2;
        float yMid = sr.getScaledHeight() / 2;


    }
}
