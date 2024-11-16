package me.eldodebug.soar.management.mods.impl;

import java.util.ArrayList;
import java.util.Arrays;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRender2D;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.HUDMod;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import me.eldodebug.soar.utils.GlUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class ArmorStatusMod extends HUDMod {

	private ComboSetting modeSetting = new ComboSetting(TranslateText.MODE, this, TranslateText.HORIZONTAL, new ArrayList<Option>(Arrays.asList(
			new Option(TranslateText.VERTICAL), new Option(TranslateText.HORIZONTAL))));
	private BooleanSetting valueSetting = new BooleanSetting(TranslateText.VALUE, this, false);
	
	public ArmorStatusMod() {
		super(TranslateText.ARMOR_STATUS, TranslateText.ARMOR_STATUS_DESCRIPTION);
	}

	@EventTarget
	public void onRender2D(EventRender2D event) {
		
		boolean vertical = modeSetting.getOption().getTranslate().equals(TranslateText.VERTICAL);
		
		ItemStack[] fakeStack = new ItemStack[4];
		
		fakeStack[3] = new ItemStack(Items.diamond_helmet);
		fakeStack[2] = new ItemStack(Items.diamond_chestplate);
		fakeStack[1] = new ItemStack(Items.diamond_leggings);
		fakeStack[0] = new ItemStack(Items.diamond_boots);
		
		GlUtils.startScale(this.getX(), this.getY(), this.getScale());
		this.renderItems(this.isEditing() ? fakeStack : mc.thePlayer.inventory.armorInventory, vertical);
		GlUtils.stopScale();
		
		this.setWidth(vertical ? 16 : 16 * 4);
		this.setHeight(vertical ? 16 * 4 : 16);
	}
	
	private void renderItems(ItemStack[] items, boolean vertical) {
		
		for(int i = 0; i < 4; i++) {
			
			ItemStack item = items[Math.abs(3 - i)];
			int addX = vertical ? 0 : 16 * i;
			int addY = vertical ? 16 * i : 0;
			
			if(item != null) {
				this.drawItemStack(item, this.getX() + addX, this.getY() + addY);
			}
		}
	}
	
	private void drawItemStack(ItemStack stack, int x, int y) {
		
		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		RenderItem itemRender = mc.getRenderItem();
		
		GlStateManager.translate(0, 0, 32);
		float prevZ = itemRender.zLevel;
		
		itemRender.zLevel = 0.0F;
		
		itemRender.renderItemAndEffectIntoGUI(stack, x, y);
		
		if(valueSetting.isToggled()) {
			itemRender.renderItemOverlayIntoGUI(fr, stack, x, y, "");
		}
		
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.popMatrix();
		itemRender.zLevel = prevZ;
	}
}
