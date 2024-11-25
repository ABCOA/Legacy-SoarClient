package me.eldodebug.soar.management;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.PlayerUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ResourceCounterMod extends HUDMod {

    public ResourceCounterMod() {
        super(TranslateText.RESOURCE_COUNTER, TranslateText.RESOURCE_COUNTER_DESCRIPTION);
    }

    @EventTarget
    public void onRender2D(EventRender2D event) {

        int startX = this.getX() + 6;
        int startY = this.getY() + 6;

        NanoVGManager nvg = Soar.getInstance().getNanoVGManager();

        nvg.setupAndDraw(() -> drawNanoVG());
        GlUtils.startScale(this.getX(), this.getY(), this.getScale());
        RenderUtils.drawItemStack((new ItemStack(Item.getItemById(265))), startX, startY);
        RenderUtils.drawItemStack((new ItemStack(Item.getItemById(266))), startX, startY + 18);
        RenderUtils.drawItemStack((new ItemStack(Item.getItemById(264))), startX, startY + 36);
        RenderUtils.drawItemStack((new ItemStack(Item.getItemById(388))), startX, startY + 54);
        GlUtils.stopScale();
    }

    private void drawNanoVG() {

        int ironAmount = PlayerUtils.countItem(Item.getItemById(265));
        int goldAmount = PlayerUtils.countItem(Item.getItemById(266));
        int diamondAmount = PlayerUtils.countItem(Item.getItemById(264));
        int emeraldAmount = PlayerUtils.countItem(Item.getItemById(388));
        float bgWidth = Math.max(getTextWidth(""+ironAmount, 9, Fonts.REGULAR), Math.max(getTextWidth(""+goldAmount, 9, Fonts.REGULAR),
                Math.max(getTextWidth(""+diamondAmount, 9, Fonts.REGULAR), getTextWidth(""+emeraldAmount, 9, Fonts.REGULAR)))) + 36;
        this.drawBackground(bgWidth, 83);
        drawText(""+ironAmount, 26, 10, 12, Fonts.REGULAR);
        drawText(""+goldAmount, 26, 28, 12, Fonts.REGULAR);
        drawText(""+diamondAmount, 26, 46, 12, Fonts.REGULAR);
        drawText(""+emeraldAmount, 26, 64, 12, Fonts.REGULAR);

        this.setWidth((int)bgWidth);
        this.setHeight(83);
    }
}
