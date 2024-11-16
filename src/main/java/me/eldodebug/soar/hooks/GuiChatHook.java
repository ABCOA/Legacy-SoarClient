package me.eldodebug.soar.hooks;

import java.awt.Color;

import me.eldodebug.soar.utils.ColorUtils;
import me.eldodebug.soar.utils.mouse.MouseUtils;
import me.eldodebug.soar.utils.render.RenderUtils;
import me.eldodebug.soar.utils.translate.Translator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;

public class GuiChatHook {

	private static Minecraft mc = Minecraft.getMinecraft();
	private static boolean toggled = false;
	private static String translateTo = Translator.JAPANESE;
	
	public static void drawScreen(int mouseX, int mouseY, float partialTicks) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		FontRenderer fr = mc.fontRendererObj;
		String isOn = toggled ? EnumChatFormatting.GREEN + "On" : EnumChatFormatting.RED + "Off";
		String translateText = "Translate: " + isOn;
		String toText = "To: " + translateTo;
		
		RenderUtils.drawRect(2, sr.getScaledHeight() - 30, fr.getStringWidth(translateText) + 5, 14, ColorUtils.getColorByInt(Integer.MIN_VALUE));
		fr.drawString(translateText, 5, sr.getScaledHeight() - 27, Color.WHITE.getRGB());
		
		RenderUtils.drawRect(2 + fr.getStringWidth(translateText) + 10, sr.getScaledHeight() - 30, fr.getStringWidth(toText) + 5, 14, ColorUtils.getColorByInt(Integer.MIN_VALUE));
		fr.drawString(toText, 2 + fr.getStringWidth(translateText) + 10 + 3, sr.getScaledHeight() - 27, Color.WHITE.getRGB());
	}
	
	public static void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
		ScaledResolution sr = new ScaledResolution(mc);
		FontRenderer fr = mc.fontRendererObj;
		String isOn = toggled ? EnumChatFormatting.GREEN + "On" : EnumChatFormatting.RED + "Off";
		String translateText = "Translate: " + isOn;
		String toText = "To: " + translateTo;
		
		if(MouseUtils.isInside(mouseX, mouseY, 2, sr.getScaledHeight() - 30, fr.getStringWidth(translateText) + 5, 14) && mouseButton == 0) {
			toggled = !toggled;
		}
		
		if(MouseUtils.isInside(mouseX, mouseY, 2 + fr.getStringWidth(translateText) + 10, sr.getScaledHeight() - 30, fr.getStringWidth(toText) + 5, 14) && mouseButton == 0) {
			
			if(translateTo.equals(Translator.JAPANESE)) {
				translateTo = Translator.ENGLISH;
			} else if(translateTo.equals(Translator.ENGLISH)) {
				translateTo = Translator.CHINESE_TRADITIONAL;
			} else if(translateTo.equals(Translator.CHINESE_TRADITIONAL)) {
				translateTo = Translator.CHINESE_SIMPLIFIED;
			} else if(translateTo.equals(Translator.CHINESE_SIMPLIFIED)) {
				translateTo = Translator.JAPANESE;
			}
		}
	}
	
    public static void sendTranslatedMessage(String message) {
    	
    	String lastMessage = message;
    	
    	if(!lastMessage.startsWith(".soarcmd") || !lastMessage.startsWith("/")) {
    		try {
				lastMessage = Translator.translate(lastMessage, Translator.AUTO_DETECT, translateTo);
			} catch (Exception e) {}
    	}
    	
        mc.ingameGUI.getChatGUI().addToSentMessages(lastMessage);
        mc.thePlayer.sendChatMessage(lastMessage);
    }

	public static boolean isToggled() {
		return toggled;
	}
}
