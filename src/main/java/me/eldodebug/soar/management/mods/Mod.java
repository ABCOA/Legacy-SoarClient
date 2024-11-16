package me.eldodebug.soar.management.mods;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.utils.animation.simple.SimpleAnimation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class Mod {

	public Minecraft mc = Minecraft.getMinecraft();
	public FontRenderer fr = mc.fontRendererObj;
	
	private TranslateText nameTranslate, descriptionTranslate;
	private boolean toggled, hide;
	private SimpleAnimation animation = new SimpleAnimation();
	private ModCategory category;
	
	public Mod(TranslateText nameTranslate, TranslateText descriptionTranslate, ModCategory category) {
		
		this.nameTranslate = nameTranslate;
		this.descriptionTranslate = descriptionTranslate;
		this.toggled = false;
		this.category = category;
		
		this.setup();
	}
	
	public void setup() {}
	
	public void onEnable() {
		Soar.getInstance().getEventManager().register(this);
	}
	
	public void onDisable() {
		Soar.getInstance().getEventManager().unregister(this);
	}
	
	public void toggle() {
		
		toggled = !toggled;
		
		if(toggled) {
			onEnable();
		}else {
			onDisable();
		}
	}
	
	public void setToggled(boolean toggled) {
		
		this.toggled = toggled;
		
		if(toggled) {
			onEnable();
		}else {
			onDisable();
		}
	}
	
	public String getName() {
		return nameTranslate.getText();
	}
	
	public String getDescription() {
		return descriptionTranslate.getText();
	}
	
	public String getNameKey() {
		return nameTranslate.getKey();
	}

	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public boolean isToggled() {
		return toggled;
	}

	public ModCategory getCategory() {
		return category;
	}
	
	public void setCategory(ModCategory category) {
		this.category = category;
	}

	public SimpleAnimation getAnimation() {
		return animation;
	}
}
