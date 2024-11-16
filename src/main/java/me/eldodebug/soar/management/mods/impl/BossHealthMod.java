package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.utils.GlUtils;
import me.eldodebug.soar.utils.ServerUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.boss.BossStatus;

public class BossHealthMod extends HUDMod {

	public BossHealthMod() {
		super(TranslateText.BOSS_HEALTH, TranslateText.BOSS_HEALTH_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		int bossHealthWidth = 182;
		int scale = (int) (BossStatus.healthScale * (float) (bossHealthWidth + 1));
        
        if((ServerUtils.isJoinServer() && BossStatus.bossName != null && BossStatus.statusBarTime > 0) || this.isEditing()) {
        	
            String title = this.isEditing() ? "Boss Health" : BossStatus.bossName;
            
        	GlUtils.startScale(this.getX(), this.getY(), this.getScale());
        	
    		mc.getTextureManager().bindTexture(Gui.icons);
    		BossStatus.statusBarTime--;
    		mc.getTextureManager().bindTexture(Gui.icons);
    		
            RenderUtils.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, bossHealthWidth, 5);
            RenderUtils.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 74, bossHealthWidth, 5);
            
            if(scale > 0) {
            	RenderUtils.drawTexturedModalRect(this.getX(), this.getY() + 14, 0, 79, scale, 5);
            }
            
            fr.drawStringWithShadow(title, (((182 / 2) - (fr.getStringWidth(title) / 2)) + this.getX()), (this.getY() - 10) + 13, 16777215);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(Gui.icons);
            
            GlUtils.stopScale();
        }
        
        this.setWidth(182);
        this.setHeight(20);
	}
}
