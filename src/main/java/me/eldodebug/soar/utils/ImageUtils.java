package me.eldodebug.soar.utils;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class ImageUtils {
	
	public static BufferedImage combine(BufferedImage img1, BufferedImage img2) {
		
        int width = Math.max(img1.getWidth(), img2.getWidth());
        int height = Math.max(img1.getHeight(), img2.getHeight());
        BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
	    Graphics g = combinedImage.createGraphics();
	    g.drawImage(img1, 0, 0, null);
	    g.drawImage(img2, 0, 0, null);
	    
	    return combinedImage;
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
		
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = image.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return image;
	}
	
	public static BufferedImage scissor(BufferedImage img, int x, int y, int width, int height) {
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
		
		g2d.drawImage(img, 0, 0, width, height, x, y, x + width, y + height, null);
		g2d.dispose();
		
		return image;
	}
	
	public static BufferedImage flipHorizontal(BufferedImage img) {
		
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcPixel = img.getRGB(x, y);
                int destX = width - x - 1;
                flippedImage.setRGB(destX, y, srcPixel);
            }
        }
        
        return flippedImage;
	}
	
	public static BufferedImage flipVertical(BufferedImage img) {
		
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int srcPixel = img.getRGB(x, y);
                int destY = height - y - 1;
                flippedImage.setRGB(x, destY, srcPixel);
            }
        }
        
        return flippedImage;
	}
}
