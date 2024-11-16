package me.eldodebug.soar.management.mods.impl;

import java.util.Random;
import java.util.Vector;

import org.lwjgl.opengl.GL11;

import me.eldodebug.soar.injection.interfaces.IMixinGuiIngame;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventRenderPlayerStats;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.impl.appleskin.AppleSkinHelper;
import me.eldodebug.soar.management.mods.impl.appleskin.FoodValues;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.FoodStats;

public class AppleSkinMod extends Mod {

    public Vector<IntPoint> foodBarOffsets = new Vector<>();

    private Random random = new Random();

    private float unclampedFlashAlpha;
    private float flashAlpha;
    private byte alphaDir = 1;
    
	public AppleSkinMod() {
		super(TranslateText.APPLE_SKIN, TranslateText.APPLE_SKIN_DESCRIPTION, ModCategory.PLAYER);
	}

	@EventTarget
	public void onRenderPlayerStats(EventRenderPlayerStats event) {
		
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        FoodStats stats = mc.thePlayer.getFoodStats();

        int right = scaledResolution.getScaledWidth() / 2 + 91;
        int top = scaledResolution.getScaledHeight() - 39;

        this.generateHungerBarOffsets(right, 0, ((IMixinGuiIngame)mc.ingameGUI).getUpdateCounter());

        this.drawSaturationOverlay(0, stats.getSaturationLevel(), 0, stats.getFoodLevel(), right, top, 1.0F);

        ItemStack heldItem = mc.thePlayer.getHeldItem();

        boolean holdingFood = heldItem != null && heldItem.getItem() instanceof ItemFood;

        if (!holdingFood) {
            this.resetFlash();
            return;
        }

        FoodValues foodValues = AppleSkinHelper.getFoodValues(heldItem);

        int foodHunger = foodValues.hunger;
        float foodSaturationIncrement = foodValues.getSaturationIncrement();

        int newFoodValue = stats.getFoodLevel() + foodHunger;
        float newSaturationValue = stats.getSaturationLevel() + foodSaturationIncrement;
        float saturationGained = newSaturationValue > newFoodValue ? newFoodValue - stats.getSaturationLevel() : foodSaturationIncrement;

        this.drawHungerOverlay(foodHunger, stats.getFoodLevel(), right, top, flashAlpha, AppleSkinHelper.isRottenFood(heldItem));

        this.drawSaturationOverlay(saturationGained, stats.getSaturationLevel(), foodHunger, stats.getFoodLevel(), right, top, flashAlpha);
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		
        unclampedFlashAlpha += alphaDir * 0.125F;

        if (unclampedFlashAlpha >= 1.5F) {
            alphaDir = -1;
        } else if (unclampedFlashAlpha <= -0.5F) {
            alphaDir = 1;
        }

        flashAlpha = Math.max(0.0F, Math.min(1.0F, unclampedFlashAlpha)) * Math.min(1.0F, 1.0F);
	}
	
	private void generateHungerBarOffsets(int right, int top, int ticks) {
		
        random.setSeed(ticks * 312871L);

        int preferFoodBars = 10;

        FoodStats stats = mc.thePlayer.getFoodStats();

        float saturationLevel = stats.getSaturationLevel();
        int foodLevel = stats.getFoodLevel();

        boolean shouldAnimatedFood = saturationLevel <= 0.0F && ((IMixinGuiIngame)mc.ingameGUI).getUpdateCounter() % (foodLevel * 3 + 1) == 0;

        if (foodBarOffsets.size() != preferFoodBars) {
            foodBarOffsets.setSize(preferFoodBars);
        }

        for (int i = 0; i < preferFoodBars; ++i) {
            int x = right - i * 8 - 9;
            int y = top;

            if (shouldAnimatedFood) {
                y += random.nextInt(3) - 1;
            }

            IntPoint point = foodBarOffsets.get(i);

            if (point == null) {
                point = new IntPoint();
                foodBarOffsets.set(i, point);
            }

            point.x = x - right;
            point.y = y;
        }
	}
	
	private void drawSaturationOverlay(float saturationGained, float saturationLevel, int hungerRestored, int foodLevel, int right, int top, float alpha) {
		
        if (saturationLevel + saturationGained < 0) {
            return;
        }

        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        float modifiedSaturation = Math.max(0, Math.min(20, saturationLevel + saturationGained));

        int modifiedFood = Math.max(0, Math.min(20, foodLevel + hungerRestored));

        int startSaturationBar = 0;
        int endSaturationBar = (int) Math.ceil(modifiedSaturation / 2.0F);

        if (saturationGained != 0) {
            startSaturationBar = (int) Math.max(saturationLevel / 2.0F, 0);
        }

        int iconStartOffset = 16;
        int iconSize = 9;

        for (int i = startSaturationBar; i < endSaturationBar; ++i) {
            IntPoint offset = foodBarOffsets.get(i);

            if (offset == null) {
                continue;
            }

            int x = right + offset.x;
            int y = top + offset.y;

            int v = 3 * iconSize;
            int u = iconStartOffset + 4 * iconSize;
            int ub = iconStartOffset + iconSize;

            for (PotionEffect e : mc.thePlayer.getActivePotionEffects()) {
                if (e.getPotionID() == Potion.hunger.getId()) {
                    u += 4 * iconSize;
                    break;
                }
            }

            int ubX = x;
            int ubIconSize = iconSize;

            if (i * 2 + 1 == (int) modifiedSaturation) {
                int halfIconSize = iconSize / 2;

                ubX += halfIconSize;
                ub += halfIconSize;
                ubIconSize -= halfIconSize;
            }

            if (i * 2 + 1 == modifiedFood) {
                u += iconSize;
            }

            GlStateManager.color(0.75F, 0.65F, 0.0F, alpha);
            mc.ingameGUI.drawTexturedModalRect(ubX, y, ub, v, ubIconSize, iconSize);

            if (modifiedSaturation > modifiedFood) {
                continue;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            mc.ingameGUI.drawTexturedModalRect(x, y, u, v, iconSize, iconSize);
        }

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	private void drawHungerOverlay(int hungerRestored, int foodLevel, int right, int top, float alpha, boolean useRottenTextures) {
		
		if (hungerRestored <= 0) {
			return;
		}
		
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int modifiedFood = Math.max(0, Math.min(20, foodLevel + hungerRestored));

        int startFoodBars = Math.max(0, foodLevel / 2);
        int endFoodBars = (int) Math.ceil(modifiedFood / 2.0F);

        int iconStartOffset = 16;
        int iconSize = 9;

        for (int i = startFoodBars; i < endFoodBars; ++i) {
        	
            IntPoint offset = foodBarOffsets.get(i);

            if (offset == null) {
                continue;
            }

            int x = right + offset.x;
            int y = top + offset.y;

            int v = 3 * iconSize;
            int u = iconStartOffset + 4 * iconSize;
            int ub = iconStartOffset + iconSize;

            if (useRottenTextures) {
                u += 4 * iconSize;
                ub += 12 * iconSize;
            }

            if (i * 2 + 1 == modifiedFood) {
                u += iconSize;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha * 0.25F);
            mc.ingameGUI.drawTexturedModalRect(x, y, ub, v, iconSize, iconSize);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);

            mc.ingameGUI.drawTexturedModalRect(x, y, u, v, iconSize, iconSize);
        }

        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}
	
	private void resetFlash() {
        unclampedFlashAlpha = 0.0F;
        flashAlpha = 0.0F;
        alphaDir = 1;
	}
	
	private class IntPoint {
	    public int x, y;
	}
}
