package me.eldodebug.soar.ui.comp.impl;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.mods.settings.impl.ColorSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.util.ResourceLocation;

public class CompColorPicker extends Comp {

	private SimpleAnimation openAnimation = new SimpleAnimation();
	private SimpleAnimation hueAnimation = new SimpleAnimation();
	private SimpleAnimation saturationAnimation = new SimpleAnimation();
	private SimpleAnimation brightnessAnimation = new SimpleAnimation();
	private SimpleAnimation alphaAnimation = new SimpleAnimation();
	
	private ColorSetting colorSetting;
	private boolean open;
	private float scale;
	private float saturationValueSize, brightnessValueSize, alphaValueSize;
	private float hueValueHeight;
	private boolean hueDragging, sbDragging, alphaDragging;
	
	public CompColorPicker(float x, float y, ColorSetting setting) {
		super(x, y);
		this.colorSetting = setting;
		this.scale = 1.0F;
		this.open = false;
	}
	
	public CompColorPicker(ColorSetting setting) {
		super(0, 0);
		this.colorSetting = setting;
		this.scale = 1.0F;
		this.open = false;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		
		NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
		
		openAnimation.setAnimation(open ? 1.1F : 0.0F, 16);
		
		nvg.save();
		nvg.translate(0, 26 * scale);
		
		float hueMaxValue = 1.0F;
		float hueMinValue = 0.0F;
		float hueValue = colorSetting.getHue();
		float size = 100 * scale;
		
		nvg.scale(this.getX(), this.getY(), size, 0, Math.max(0, openAnimation.getValue() - 0.1F));
		
		hueValueHeight = (size - (12 * scale)) * (hueValue - hueMinValue) / (hueMaxValue - hueMinValue);
		
		double hueDiff = Math.min(size, Math.max(0, mouseY - (this.getY() + (28 * scale))));
		
		hueAnimation.setAnimation(hueValueHeight, 16);
		
		if(hueDragging) {
			if(hueDiff == 0) {
				colorSetting.setHue(hueMinValue);
			}else {
				colorSetting.setHue((float) MathUtils.roundToPlace(((hueDiff / size) * (hueMaxValue - hueMinValue) + hueMinValue), 2));
			}
		}
		
		float sbMaxValue = 1.0F;
		float sbMinValue = 0.0F;
		float saturationValue = colorSetting.getSaturation();
		float brightnessValue = colorSetting.getBrightness();
		
		saturationValueSize = (size - (12 * scale)) * (saturationValue - sbMinValue) / (sbMaxValue - sbMinValue);
		brightnessValueSize = (size - (12 * scale)) * (brightnessValue - sbMinValue) / (sbMaxValue - sbMinValue);
		
		double brightnessDiff = Math.min(size, Math.max(0, (this.getY() + (30 * scale)) + size - mouseY));
		double saturationDiff = Math.min(size, Math.max(0, mouseX - (this.getX() - (4 * scale))));
		
		brightnessAnimation.setAnimation(brightnessValueSize, 20);
		saturationAnimation.setAnimation(saturationValueSize, 20);
		
		if(sbDragging) {
			
			if(brightnessDiff == 0) {
				colorSetting.setBrightness(sbMinValue);
			}else {
				colorSetting.setBrightness((float) MathUtils.roundToPlace(((brightnessDiff / size) * (sbMaxValue - sbMinValue) + sbMinValue), 2));
			}
			
			if(saturationDiff == 0) {
				colorSetting.setSaturation(sbMinValue);
			} else {
				colorSetting.setSaturation((float) MathUtils.roundToPlace(((saturationDiff / size) * (sbMaxValue - sbMinValue) + sbMinValue), 2));
			}
		}
		
		float alphaMaxValue = 255;
		float alphaMinValue = 0;
		float alphaValue = colorSetting.getAlpha();
		float alphaWidth = size + (18 * scale);
		
		double alphaDiff = Math.min(alphaWidth, Math.max(0, mouseX - (this.getX() - (4 * scale))));
		
		alphaValueSize = (alphaWidth - (12 * scale)) * (alphaValue - alphaMinValue) / (alphaMaxValue - alphaMinValue);
		
		alphaAnimation.setAnimation(alphaValueSize, 20);
		
		if(alphaDragging) {
			
			if(colorSetting.isShowAlpha()) {
				if(alphaDiff == 0) {
					colorSetting.setAlpha(0);
				} else {
					colorSetting.setAlpha((int) MathUtils.roundToPlace(((alphaDiff / alphaWidth) * (alphaMaxValue - alphaMinValue) + alphaMinValue), 2));
				}
			} else {
				colorSetting.setAlpha(255);
			}
		}
		
		nvg.drawHSBBox(this.getX(), this.getY(), size, size, 6F * scale, Color.getHSBColor(colorSetting.getHue(), 1, 1));
		nvg.drawRoundedImage(new ResourceLocation("soar/hue.png"), this.getX() + (106 * scale), this.getY(), 12 * scale, size, 3 * scale);
		nvg.drawArc(this.getX() + (112 * scale), this.getY() + hueAnimation.getValue() + (6 * scale), 3 * scale, 0, 360, 1.2F * scale, Color.WHITE);
		nvg.drawArc(this.getX() + saturationAnimation.getValue() + (6 * scale), this.getY() + size - brightnessAnimation.getValue() - (6 * scale), 3 * scale, 0, 360, 1.2F * scale, Color.WHITE);
		
		if(colorSetting.isShowAlpha()) {
			nvg.drawRoundedImage(new ResourceLocation("soar/alpha.png"), this.getX(), this.getY() + (106 * scale), size + (18 * scale), 12 * scale, 3 * scale);
			nvg.drawAlphaBar(this.getX(), this.getY() + (106 * scale), alphaWidth, 12 * scale, 3 * scale, Color.getHSBColor(colorSetting.getHue(), 1, 1));
			nvg.drawArc(this.getX() + alphaAnimation.getValue() + (6 * scale), this.getY() + (112 * scale), 3 * scale, 0, 360, 1.2F * scale, Color.WHITE);
		}
		
		nvg.restore();
		
		nvg.drawRoundedRect(this.getX() + (106 * scale), this.getY(), 16 * scale, 16 * scale, 4, colorSetting.getColor());
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		if(open) {
			float size = 100 * scale;
			float alphaWidth = size + (18 * scale);
			float addY = 26 * scale;
			
			if(mouseButton == 0) {
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX() + (106 * scale), this.getY() + addY, 12 * scale, size)) {
					hueDragging = true;
				}
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() + addY, size, size)) {
					sbDragging = true;
				}
				
				if(MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY() + (106 * scale) + addY, alphaWidth, 12 * scale) && colorSetting.isShowAlpha()) {
					alphaDragging = true;
				}
			}
		}
		
		if(mouseButton == 0) {
			
			if(isInsideOpen(mouseX, mouseY)) {
				open = !open;
			}
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		hueDragging = false;
		sbDragging = false;
		alphaDragging = false;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public boolean isOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}
	
	public boolean isShowAlpha() {
		return colorSetting.isShowAlpha();
	}
	
	public boolean isInsideOpen(int mouseX, int mouseY) {
		return MouseUtils.isInside(mouseX, mouseY, this.getX() + (106 * scale), this.getY(), 16 * scale, 16 * scale);
	}
}
