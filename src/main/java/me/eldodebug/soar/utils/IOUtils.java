package me.eldodebug.soar.utils;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.utils.transferable.ImageTransferable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class IOUtils {

	private static Minecraft mc = Minecraft.getMinecraft();
	
	public static void copyStringToClipboard(String s) {
		StringSelection stringSelection = new StringSelection(s);
		getToolkit().getSystemClipboard().setContents(stringSelection, null);
	}
	
	public static String getStringFromClipboard() {
		try {
			return getToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.stringFlavor).toString();
		} catch(Exception e) {
			return null;
		}
	}
	
	public static void copyImageToClipboard(Image image) {
		ImageTransferable imageSelection = new ImageTransferable(image);
		getToolkit().getSystemClipboard().setContents(imageSelection, null);
	}
	
	public static Image getImageFromClipboard() {
		try {
			return (Image) getToolkit().getSystemClipboard().getContents(null).getTransferData(DataFlavor.imageFlavor);
		} catch (Exception e) {
			return null;
		}
	}
	
	private static Toolkit getToolkit() {
		return Toolkit.getDefaultToolkit();
	}
	
    public static ByteBuffer resourceToByteBuffer(ResourceLocation location) {
    	
		try {
			byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(mc.getResourceManager().getResource(location).getInputStream());
			
	        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
	        ((Buffer) data).flip();
	        
	        return data;
		} catch (Exception e) {
			SoarLogger.error("Failed to load resource", e);
		}
        
		return null;
    }
    
    public static ByteBuffer resourceToByteBuffer(File file) {
    	
		try {
			byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(new FileInputStream(file));
			
	        ByteBuffer data = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder()).put(bytes);
	        ((Buffer) data).flip();
	        
	        return data;
		} catch (Exception e) {
			SoarLogger.error("Failed to load resource", e);
		}
        
		return null;
    }
}
