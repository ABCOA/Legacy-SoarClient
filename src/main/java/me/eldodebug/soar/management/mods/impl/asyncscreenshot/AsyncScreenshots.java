package me.eldodebug.soar.management.mods.impl.asyncscreenshot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.management.mods.impl.AsyncScreenshotMod;
import net.minecraft.client.Minecraft;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

public class AsyncScreenshots extends Thread {

	private Minecraft mc = Minecraft.getMinecraft();
	
    private static BufferedImage image;
    private static File screenshot;
    private final int width, height;
    private final int[] pixelValues;
    
    public AsyncScreenshots(int width, int height, int[] pixelValues) {
        this.width = width;
        this.height = height;
        this.pixelValues = pixelValues;
    }
    
	@Override
	public void run() {
		
        processPixelValues(pixelValues, width, height);
        screenshot = getTimestampedPNGFileForDirectory();
        
        try {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            image.setRGB(0, 0, width, height, pixelValues, 0, width);
            ImageIO.write(image, "png", screenshot);

            if(AsyncScreenshotMod.getInstance().getMessageSetting().isToggled()) {
            	mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(EnumChatFormatting.UNDERLINE + "Saved screenshot" + EnumChatFormatting.RESET + " ")
            			.appendSibling(new ChatComponentText("[Open] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd screenshot open " + screenshot.getName())))
            					.appendSibling(new ChatComponentText("[Copy] ").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.BLUE).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd screenshot copy " + screenshot.getName())))
            							.appendSibling(new ChatComponentText("[Delete]").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.RED).setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, ".soarcmd screenshot del " + screenshot.getName())))))));
            }
            
        	if(AsyncScreenshotMod.getInstance().getClipboardSetting().isToggled()) {
        		mc.thePlayer.sendChatMessage(".soarcmd screenshot copy " + screenshot.getName());
        	}
        } catch (Exception e) {}
	}
	
    private void processPixelValues(int[] pixels, int displayWidth, int displayHeight) {
        final int[] xValues = new int[displayWidth];
        for (int yValues = displayHeight >> 1, val = 0; val < yValues; ++val) {
            System.arraycopy(pixels, val * displayWidth, xValues, 0, displayWidth);
            System.arraycopy(
                pixels,
                (displayHeight - 1 - val) * displayWidth,
                pixels,
                val * displayWidth,
                displayWidth
            );
            System.arraycopy(xValues, 0, pixels, (displayHeight - 1 - val) * displayWidth, displayWidth);
        }
    }
    
    public static File getTimestampedPNGFileForDirectory() {
    	
        String dateFormatting = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
        int screenshotCount = 1;
        File screenshot;

        while (true) {
            screenshot = new File(
                Soar.getInstance().getFileManager().getScreenshotDir(),
                dateFormatting + ((screenshotCount == 1) ? "" : ("_" + screenshotCount)) + ".png"
            );
            if (!screenshot.exists()) {
                break;
            }

            ++screenshotCount;
        }

        return screenshot;
    }

}