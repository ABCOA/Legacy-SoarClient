package me.eldodebug.soar.gui.mainmenu.impl.welcome;

import java.awt.Color;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.gui.mainmenu.GuiSoarMainMenu;
import me.eldodebug.soar.gui.mainmenu.MainMenuScene;
import me.eldodebug.soar.management.nanovg.NanoVGManager;
import me.eldodebug.soar.management.nanovg.font.Fonts;
import me.eldodebug.soar.utils.TimerUtils;
import me.eldodebug.soar.utils.animation.normal.Animation;
import me.eldodebug.soar.utils.animation.normal.Direction;
import me.eldodebug.soar.utils.animation.normal.other.DecelerateAnimation;
import me.eldodebug.soar.utils.render.BlurUtils;
import net.minecraft.client.gui.ScaledResolution;

public class WelcomeMessageScene extends MainMenuScene {

	private Animation fadeAnimation;
	private int step;
	private String message;
	
	private TimerUtils timer = new TimerUtils();
	
	public WelcomeMessageScene(GuiSoarMainMenu parent) {
		super(parent);
		
		step = 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		NanoVGManager nvg = Soar.getInstance().getNanoVGManager();
		String welcomeMessage = "Welcome to Soar Client";
		String setupMessage = "Let's start setting up the Soar Client!";
		
		BlurUtils.drawBlurScreen(14);
		
		if(fadeAnimation == null && this.getParent().isDoneBackgroundAnimation()) {
			fadeAnimation = new DecelerateAnimation(800, 1);
			fadeAnimation.setDirection(Direction.FORWARDS);
			fadeAnimation.reset();
			timer.reset();
		}
		
		if(fadeAnimation != null) {
			
			switch(step) {
				case 0:
					message = welcomeMessage;
					break;
				case 1:
					message = setupMessage;
					break;
			}
			
			nvg.setupAndDraw(() -> {
				nvg.drawCenteredText(message, sr.getScaledWidth() / 2, 
						(sr.getScaledHeight() / 2) - (nvg.getTextHeight(message, 26, Fonts.REGULAR) / 2), 
						new Color(255, 255, 255, (int) (fadeAnimation.getValueFloat() * 255)), 26, Fonts.REGULAR);
			});
			
			if(timer.delay(5000) && fadeAnimation.getDirection().equals(Direction.FORWARDS)) {
				fadeAnimation.setDirection(Direction.BACKWARDS);
				timer.reset();
			}
			
			if(fadeAnimation.isDone(Direction.BACKWARDS)) {
				
				if(step == 1) {
					this.setCurrentScene(this.getSceneByClass(ThemeSelectScene.class));
					return;
				}
				
				step++;
				fadeAnimation.setDirection(Direction.FORWARDS);
			}
		}
	}
}
