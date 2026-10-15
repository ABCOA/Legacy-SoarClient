package me.eldodebug.soar.gui.modmenu.category.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.SoarAPI;
import me.eldodebug.soar.gui.modmenu.GuiModMenu;
import me.eldodebug.soar.gui.modmenu.category.Category;
import me.eldodebug.soar.management.cape.CapeCategory;
import me.eldodebug.soar.management.cape.CapeManager;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.cape.impl.OfficialCape;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.color.palette.ColorPalette;
import me.eldodebug.soar.management.color.palette.ColorType;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.management.nanovg.font.Icon;
import me.eldodebug.soar.management.notification.NotificationType;
import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.SearchUtils;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.nanovg.NVGPaint;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;

import java.awt.*;

public class CosmeticsCategory extends Category {

	private CapeCategory currentCategory;
	private int officialImage;
	private int officialTexture = -1;
	
	public CosmeticsCategory(GuiModMenu parent) {
		super(parent, TranslateText.COSMETICS, Icon.SHOPPING, true);
	}

	@Override
	public void initGui() {
		currentCategory = CapeCategory.ALL;
	}
	
	@Override
	public void initCategory() {
		scroll.resetAll();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		ColorPalette palette = colorManager.getPalette();
		CapeManager capeManager = instance.getCapeManager();
		
		int offsetX = 0;
		float offsetY = 13;
		int index = 1;
		int prevIndex = 1;
		
		nvg.save();
		nvg.translate(0, scroll.getValue());
		
		for(CapeCategory c : CapeCategory.values()) {
			
			float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);
			boolean isCurrentCategory = c.equals(currentCategory);
			MouseCursor.pointer(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3 + scroll.getValue(), textWidth + 20, 16);
			
			c.getBackgroundAnimation().setAnimation(isCurrentCategory ? 1.0F : 0.0F, 16);
			
			Color defaultColor = palette.getBackgroundColor(ColorType.DARK);
			Color color1 = ColorUtils.applyAlpha(accentColor.getColor1(), (int) (c.getBackgroundAnimation().getValue() * 255));
			Color color2 = ColorUtils.applyAlpha(accentColor.getColor2(), (int) (c.getBackgroundAnimation().getValue() * 255));
			Color textColor = c.getTextColorAnimation().getColor(isCurrentCategory ? Color.WHITE : palette.getFontColor(ColorType.DARK), 20);

			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, defaultColor);
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16, 6, color1, color2);
			
			nvg.drawText(c.getName(), this.getX() + 15 + offsetX + ((textWidth + 20) - textWidth) / 2, this.getY() + offsetY + 1.5F, textColor, 9, Fonts.MEDIUM);
			
			offsetX+=textWidth + 28;
		}
		
		offsetX = 0;
		nvg.drawText(nvg.getLimitText(capeManager.getSyncStatus() + (capeManager.getSyncDetail().isEmpty() ? "" : ": " + capeManager.getSyncDetail()), 8, Fonts.MEDIUM, 388),
				this.getX() + 15, this.getY() + offsetY + 23, palette.getFontColor(ColorType.DARK), 8, Fonts.MEDIUM);
		offsetY = offsetY + 38;
		
		for(Cape cp : capeManager.getCapes()) {
			
			if(filterCape(cp)) {
				continue;
			}
			
			cp.getAnimation().setAnimation(cp.equals(capeManager.getCurrentCape()) ? 1.0F : 0.0F, 16);
			MouseCursor.pointer(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY + scroll.getValue(), 88, 135);
			
			nvg.drawGradientRoundedRect(this.getX() + 15 + offsetX - 2, this.getY() + offsetY - 2, 88 + 4, 135 + 4, 8.5F, ColorUtils.applyAlpha(accentColor.getColor1(), (int) (cp.getAnimation().getValue() * 255)), ColorUtils.applyAlpha(accentColor.getColor2(), (int) (cp.getAnimation().getValue() * 255)));
			nvg.drawRoundedRect(this.getX() + 15 + offsetX, this.getY() + offsetY, 88, 135, 8, palette.getBackgroundColor(ColorType.DARK));
			
			if(cp instanceof OfficialCape) {
				ResourceLocation official = capeManager.getOfficialCape();
				if (official != null) drawOfficialPreview(nvg, official, this.getX() + 24 + offsetX, this.getY() + offsetY + 9);
				else nvg.drawCenteredText(capeManager.getOfficialStatus(), this.getX() + 59 + offsetX, this.getY() + offsetY + 52,
						palette.getFontColor(ColorType.DARK), 7, Fonts.MEDIUM);
			} else if(cp instanceof NormalCape) {
				
				NormalCape c = ((NormalCape)cp);
				
				if(c.getSample() != null) {
					nvg.drawRoundedImage(c.getSample(), this.getX() + 24 + offsetX, this.getY() + offsetY + 9, 70, 105, 8);
				}
			}else if(cp instanceof CustomCape) {
				
				CustomCape c = ((CustomCape)cp);
				
				if(c.getSample() != null) {
					nvg.drawRoundedImage(c.getSample(), this.getX() + 24 + offsetX, this.getY() + offsetY + 9, 70, 105, 8);
				}
			}
			
			nvg.drawCenteredText(capeManager.getDisplayName(cp), this.getX() + 15 + offsetX + (88 / 2), this.getY() + offsetY + 120.5F, palette.getFontColor(ColorType.DARK), 10, Fonts.MEDIUM);
			
			offsetX+=100;
			
			if(index % 4 == 0) {
				offsetX = 0;
				offsetY+=147;
				prevIndex++;
			}
			
			index++;
		}
		
		scroll.setMaxScroll(prevIndex == 1 ? 0 : offsetY - (147 / 1.48F));
		
		nvg.restore();
	}
	
	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		SoarAPI api = instance.getApi();
		
		int offsetX = 0;
		float offsetY = 13 + scroll.getValue();
		CapeManager capeManager = instance.getCapeManager();
		int index = 1;
		
		for(CapeCategory c : CapeCategory.values()) {
			
			float textWidth = nvg.getTextWidth(c.getName(), 9, Fonts.MEDIUM);
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY - 3, textWidth + 20, 16) && mouseButton == 0) {
				currentCategory = c;
			}
			
			offsetX+=textWidth + 28;
		}
		
		offsetX = 0;
		offsetY = offsetY + 38;
		
		for(Cape cp : capeManager.getCapes()) {
			
			if(filterCape(cp)) {
				continue;
			}
			
			if(MouseUtils.isInside(mouseX, mouseY, this.getX() + 15 + offsetX, this.getY() + offsetY, 88, 135) && mouseButton == 0) {
				
				if(cp.isPremium()) {
					
					if(api.isSpecialUser()) {
						capeManager.setCurrentCape(cp);
					} else {
						instance.getNotificationManager().post(TranslateText.ERROR, TranslateText.PREMIUM_ONLY, NotificationType.ERROR);
					}
				} else {
					capeManager.setCurrentCape(cp);
				}
			}
			
			offsetX+=100;
			
			if(index % 4 == 0) {
				offsetX = 0;
				offsetY+=147;
			}
			
			index++;
		}
	}
	
	private boolean filterCape(Cape cp) {
		
		if(!currentCategory.equals(CapeCategory.ALL) && !currentCategory.equals(cp.getCategory())) {
			return true;
		}
		
		if(!this.getSearchBox().getText().isEmpty() && !SearchUtils.isSimillar(Soar.getInstance().getCapeManager().getDisplayName(cp), this.getSearchBox().getText())) {
			return true;
		}
		
		return false;
	}

	private void drawOfficialPreview(NanoVGManager nvg, ResourceLocation location, float x, float y) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.getTextureManager().getTexture(location) == null) return;
		int texture = minecraft.getTextureManager().getTexture(location).getGlTextureId();
		long context = nvg.getContext();
		if (texture != officialTexture) {
			if (officialImage != 0) NanoVG.nvgDeleteImage(context, officialImage);
			officialTexture = texture;
			officialImage = NanoVGGL2.nvglCreateImageFromHandle(context, texture, 64, 32, NanoVGGL2.NVG_IMAGE_NODELETE);
		}
		if (officialImage == 0) return;
		// Built-in samples include the top edge: (1, 0), 10 x 17 pixels in the 64 x 32 atlas.
		float scaleX = 70F / 10F;
		float scaleY = 105F / 17F;
		NVGPaint paint = NVGPaint.calloc();
		try {
			NanoVG.nvgImagePattern(context, x - scaleX, y,
					64F * scaleX, 32F * scaleY, 0, officialImage, 1, paint);
			NanoVG.nvgBeginPath(context);
			NanoVG.nvgRoundedRect(context, x, y, 70, 105, 8);
			NanoVG.nvgFillPaint(context, paint);
			NanoVG.nvgFill(context);
		} finally { paint.free(); }
	}
}
