package me.eldodebug.soar.management.cape;

import me.eldodebug.soar.Soar;
import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.management.cape.impl.Cape;
import me.eldodebug.soar.management.cape.impl.CustomCape;
import me.eldodebug.soar.management.cape.impl.NormalCape;
import me.eldodebug.soar.management.file.FileManager;
import me.eldodebug.soar.utils.ImageUtils;
import me.eldodebug.soar.utils.file.FileUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CapeManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private ArrayList<Cape> capes = new ArrayList<Cape>();
	private Cape currentCape;
	
	public CapeManager() {
		
		Soar instance = Soar.getInstance();
		FileManager fileManager = instance.getFileManager();
		File customCapeDir = new File(fileManager.getSoarDir(), "custom-cape");
		File cacheDir = new File(fileManager.getCacheDir(), "cape");
		
		if(!customCapeDir.exists()) {
			fileManager.createDir(customCapeDir);
		}
		
		if(!cacheDir.exists()) {
			fileManager.createDir(cacheDir);
		}
		
		capes.add(new NormalCape("None", null, null, false, CapeCategory.ALL));
		
		add("Minecon 2011", "minecon/2011-sample.png", "minecon/2011.png", false, CapeCategory.MINECON);
		add("Minecon 2012", "minecon/2012-sample.png", "minecon/2012.png", false, CapeCategory.MINECON);
		add("Minecon 2013", "minecon/2013-sample.png", "minecon/2013.png", false, CapeCategory.MINECON);
		add("Minecon 2015", "minecon/2015-sample.png", "minecon/2015.png", false, CapeCategory.MINECON);
		add("Minecon 2016", "minecon/2016-sample.png", "minecon/2016.png", false, CapeCategory.MINECON);
		add("Canada", "flag/canada-sample.png", "flag/canada.png", true, CapeCategory.FLAG);
		add("France", "flag/france-sample.png", "flag/france.png", true, CapeCategory.FLAG);
		add("Germany", "flag/germany-sample.png", "flag/germany.png", true, CapeCategory.FLAG);
		add("India", "flag/india-sample.png", "flag/india.png", true, CapeCategory.FLAG);
		add("Indonesia", "flag/indonesia-sample.png", "flag/indonesia.png", true, CapeCategory.FLAG);
		add("Italy", "flag/italy-sample.png", "flag/italy.png", true, CapeCategory.FLAG);
		add("Japan", "flag/japan-sample.png", "flag/japan.png", true, CapeCategory.FLAG);
		add("Korean", "flag/korean-sample.png", "flag/korean.png", true, CapeCategory.FLAG);
		add("Qing Dynasty", "flag/qing-dynasty-sample.png", "flag/qing-dynasty.png", true, CapeCategory.FLAG);
		add("United Kingdom", "flag/united-kingdom-sample.png", "flag/united-kingdom.png", true, CapeCategory.FLAG);
		add("United States", "flag/united-states-sample.png", "flag/united-states.png", true, CapeCategory.FLAG);
		add("Miku", "abcoc/miku-sample.png", "abcoc/miku.png", true, CapeCategory.ABCOC);
		add("neko1", "nachoneko/neko1-sample.png", "nachoneko/neko1.png", true, CapeCategory.NACHONEKO);
		add("neko2", "nachoneko/neko2-sample.png", "nachoneko/neko2.png", true, CapeCategory.NACHONEKO);
		add("neko3", "nachoneko/neko3-sample.png", "nachoneko/neko3.png", true, CapeCategory.NACHONEKO);
		add("neko4", "nachoneko/neko4-sample.png", "nachoneko/neko4.png", true, CapeCategory.NACHONEKO);
		add("neko5", "nachoneko/neko5-sample.png", "nachoneko/neko5.png", true, CapeCategory.NACHONEKO);
		add("neko6", "nachoneko/neko6-sample.png", "nachoneko/neko6.png", true, CapeCategory.NACHONEKO);

		currentCape = getCapeByName("None");

		for(File f : customCapeDir.listFiles()) {
			
			if(FileUtils.isImageFile(f)) {
				
				File file = new File(cacheDir, f.getName() + ".png");
				
				if(!file.exists()) {
					
					try {
						BufferedImage image = ImageIO.read(f);
						int width = image.getWidth();
						int height = image.getHeight();
					
						BufferedImage outputImage = ImageUtils.scissor(image, (int) (width * 0.03125), (int) (height * 0.0625), (int) (width * 0.125), (int) (height * 0.46875));
						
						ImageIO.write(ImageUtils.resize(outputImage, 1000, 1700), "png", file);
					} catch (IOException e) {
						SoarLogger.error("Failed to load image", e);
						continue;
					}
				}
				
				if(file.exists()) {
					
					try {
						DynamicTexture cape = new DynamicTexture(ImageIO.read(f));
						
						addCustomCape(f.getName().replace("." + FileUtils.getExtension(f), ""), file,
								mc.getTextureManager().getDynamicTextureLocation(String.valueOf(f.getName().hashCode()), cape), true, CapeCategory.CUSTOM);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		for(Cape c : capes) {
			
			if(c instanceof NormalCape) {
				
				NormalCape cape = (NormalCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
			
			if(c instanceof CustomCape) {
				
				CustomCape cape = (CustomCape) c;
				
				if(cape.getSample() != null) {
					instance.getNanoVGManager().loadImage(cape.getSample());
				}
			}
			
			if(c.getCape() != null) {
				mc.getTextureManager().bindTexture(c.getCape());
			}
		}
	}
	
	private void add(String name, String samplePath, String capePath, boolean premium, CapeCategory category) {
		
		String cosmeticPath = "soar/cosmetics/cape/";
		
		capes.add(new NormalCape(name, new ResourceLocation(cosmeticPath + samplePath), new ResourceLocation(cosmeticPath + capePath), premium, category));
	}
	
	private void addCustomCape(String name, File sample, ResourceLocation cape, boolean premium, CapeCategory category) {
		capes.add(new CustomCape(name, sample, cape, premium, category));
	}
	
	public ArrayList<Cape> getCapes() {
		return capes;
	}
	
	public Cape getCurrentCape() {
		return currentCape;
	}

	public void setCurrentCape(Cape currentCape) {
		this.currentCape = currentCape;
	}

	public Cape getCapeByName(String name) {
		
		for(Cape c : capes) {
			if(c.getName().equals(name)) {
				return c;
			}
		}
		
		return getCapeByName("None");
	}
}
