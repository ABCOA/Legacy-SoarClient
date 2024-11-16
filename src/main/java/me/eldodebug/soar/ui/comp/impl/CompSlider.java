package me.eldodebug.soar.ui.comp.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.MathUtils;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import me.eldodebug.soar.utils.mouse.MouseUtils;

public class CompSlider extends Comp {
	
	private NumberSetting setting;
	private double width, height;
	
	private double baseWidth, valueWidth;
	private boolean dragging;
	private boolean circle;
	private boolean showValue;
	
	private SimpleAnimation animation = new SimpleAnimation();
	private SimpleAnimation draggingAnimation = new SimpleAnimation();
	
	public CompSlider(float x, float y, NumberSetting setting, float width) {
		super(x, y);
		this.setting = setting;
		this.width = width;
		this.height = 4;
		this.circle = true;
		this.showValue = true;
	}
	
	public CompSlider(NumberSetting setting) {
		super(0, 0);
		this.setting = setting;
		this.width = 90;
		this.height = 4;
		this.circle = true;
		this.showValue = true;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		
		double maxValue = setting.getMaxValue();
		double minValue = setting.getMinValue();
		double value = setting.getValue();
		
		baseWidth = width * (maxValue - minValue) / (maxValue - minValue);
		valueWidth = width * (value - minValue) / (maxValue - minValue);
		
		double diff = Math.min(width, Math.max(0, mouseX - (this.getX() - 1.5F)));
		
		if(dragging) {
			if(diff == 0) {
				setting.setValue(minValue);
			}else {
				setting.setValue(MathUtils.roundToPlace(((diff / width) * (maxValue - minValue) + minValue), 2));
			}
		}
		
		animation.setAnimation((float) valueWidth, 16);
		draggingAnimation.setAnimation(MouseUtils.isInside(mouseX, mouseY, this.getX() - 6, this.getY() - 3, baseWidth + 12, height * height) ? 1.0F : 0.0F, 16);
		
		nvg.drawRoundedRect(this.getX(), this.getY(), (float) baseWidth, (float) height, 2F, palette.getBackgroundColor(ColorType.NORMAL));
		nvg.drawGradientRoundedRect(this.getX(), this.getY(), animation.getValue(), (float) height, 2F, accentColor.getColor1(), accentColor.getColor2());
		
		if(circle) {
			nvg.drawGradientRoundedRect(this.getX() + animation.getValue() - 6, this.getY() - 2, 8, 8, 4, accentColor.getColor1(), accentColor.getColor2());
		}
		
		if(showValue) {
			nvg.save();
			nvg.translate(0, 2 - (draggingAnimation.getValue() * 2));
			
			nvg.drawText(String.valueOf(value), this.getX() + animation.getValue() - (nvg.getTextWidth(String.valueOf(value), 7, Fonts.REGULAR) / 2), 
					this.getY() - 10, palette.getFontColor(ColorType.NORMAL, (int) (draggingAnimation.getValue() * 255)), 7, Fonts.REGULAR);
			
			nvg.restore();
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if(MouseUtils.isInside(mouseX, mouseY, this.getX() - 6, this.getY() - 3, baseWidth + 12, height * height) && mouseButton == 0) {
			dragging = true;
		}
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		dragging = false;
	}
	
	public double getWidth() {
		return width;
	}

	public void setWidth(double width) {
		this.width = width;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public NumberSetting getSetting() {
		return setting;
	}

	public void setCircle(boolean circle) {
		this.circle = circle;
	}

	public void setShowValue(boolean showValue) {
		this.showValue = showValue;
	}
}
