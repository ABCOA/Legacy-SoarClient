package me.eldodebug.soar.gui.mainmenu.impl.welcome;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.impl.AccountScene;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import me.eldodebug.soar.utils.buffer.ScreenAlpha;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

public class FirstLoginScene extends AccountScene {
	
	private Animation fadeAnimation;
	private ScreenAlpha screenAlpha = new ScreenAlpha();
	
	public FirstLoginScene(GuiSoarMainMenu parent) {
		super(parent);
	}

	@Override
	public void initScene() {
		super.initScene();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		Soar instance = Soar.getInstance();
		NanoVGManager nvg = instance.getNanoVGManager();
		
		if(fadeAnimation == null) {
			fadeAnimation = new DecelerateAnimation(800, 1);
			fadeAnimation.setDirection(Direction.FORWARDS);
			fadeAnimation.reset();
		}
		
		if(fadeAnimation.isDone(Direction.BACKWARDS)) {
			this.setCurrentScene(this.getSceneByClass(LastMessageScene.class));
		}
		
		BlurUtils.drawBlurScreen(14);
		
		screenAlpha.wrap(() -> this.drawNanoVG(mouseX, mouseY, partialTicks, sr, instance, nvg), fadeAnimation.getValueFloat());
	}
	
	/*@Override
	public Runnable afterMicrosoftLogin() {
		
		AccountManager accountManager = Soar.getInstance().getAccountManager();
		
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if(accountManager.getCurrentAccount() != null) {
					accountManager.save();
					fadeAnimation.setDirection(Direction.BACKWARDS);
				}
			}
		};
		
		return runnable;
	}*/
	
	@Override
	public void keyTyped(char typedChar, int keyCode) {
		textBox.keyTyped(typedChar, keyCode);
	}
}