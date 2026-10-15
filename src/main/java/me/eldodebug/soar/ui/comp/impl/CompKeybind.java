package me.eldodebug.soar.ui.comp.impl;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.color.AccentColor;
import me.eldodebug.soar.management.color.ColorManager;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.settings.KeybindConflicts;
import me.eldodebug.soar.management.mods.settings.impl.KeybindSetting;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.ui.comp.Comp;
import me.eldodebug.soar.utils.mouse.MouseCursor;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class CompKeybind extends Comp {

	private KeybindSetting setting;
	private float width;
	private boolean binding;
	
	public CompKeybind(float x, float y, float width, KeybindSetting setting) {
		super(x, y);
		this.setting = setting;
		this.width = width;
	}
	
	public CompKeybind(float width, KeybindSetting setting) {
		super(0, 0);
		this.width = width;
		this.setting = setting;
	}

	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		MouseCursor.pointer(mouseX, mouseY, this.getX(), this.getY(), width, 16);
		
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		ColorManager colorManager = instance.getColorManager();
		AccentColor accentColor = colorManager.getCurrentColor();
		
		java.util.List<KeybindSetting> conflicts = KeybindConflicts.findFor(setting);
		boolean conflict = !binding && !conflicts.isEmpty();
		String info = binding ? "Binding..." : KeybindConflicts.keyName(setting.getKeyCode()) + (conflict ? " !" : "");
		
		nvg.drawGradientRoundedRect(this.getX(), this.getY(), width, 16, 4,
				conflict ? new Color(220, 120, 45) : accentColor.getColor1(), conflict ? new Color(205, 70, 50) : accentColor.getColor2());
		
		nvg.drawCenteredText(info, this.getX() + (width / 2), this.getY() + 5F, new Color(255, 255, 255), 8, Fonts.REGULAR);
		if(conflict && MouseUtils.isInside(mouseX, mouseY, this.getX(), this.getY(), width, 16)) {
			String warning = TranslateText.KEYBIND_CONFLICT.getText() + ": " + KeybindConflicts.names(conflicts);
			warning = nvg.getLimitText(warning, 8, Fonts.REGULAR, 164);
			float tooltipWidth = nvg.getTextWidth(warning, 8, Fonts.REGULAR) + 12;
			nvg.drawRoundedRect(this.getX() - 90, this.getY() - 23, tooltipWidth, 18, 4, new Color(30, 30, 30, 240));
			nvg.drawText(warning, this.getX() - 84, this.getY() - 18, new Color(255, 185, 100), 8, Fonts.REGULAR);
		}
	}
	
	@Override
	public void mouseClicked(int mouseX, int mosueY, int mouseButton) {
		if(MouseUtils.isInside(mouseX, mosueY, this.getX(), this.getY(), width, 16) && mouseButton == 0) {
			binding = !binding;
		}
	}
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		
		if(binding) {
			
			if(keyCode == Keyboard.KEY_ESCAPE) {
				setting.setKeyCode(Keyboard.KEY_NONE);
				binding = false;
				return;
			}
			
			setting.setKeyCode(keyCode);
			binding = false;
			KeybindConflicts.warnForBinding(setting);
		}
	}

	public boolean isBinding() {
		return binding;
	}

	public void setBinding(boolean binding) {
		this.binding = binding;
	}
}
