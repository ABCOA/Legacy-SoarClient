package me.eldodebug.soar.management.nanovg.asset;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.HashMap;

import org.lwjgl.nanovg.NSVGImage;
import org.lwjgl.nanovg.NanoSVG;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.nanovg.NanoVGGL2;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

import me.eldodebug.soar.logger.SoarLogger;
import me.eldodebug.soar.utils.IOUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public class AssetManager {

	private Minecraft mc = Minecraft.getMinecraft();
	
	private HashMap<String, NVGAsset> imageCache = new HashMap<String, NVGAsset>();
	private HashMap<Integer, Integer> glTextureCache = new HashMap<Integer, Integer>();
	private HashMap<String, NVGAsset> svgCache = new HashMap<String, NVGAsset>();
	
	public boolean loadImage(long nvg, int texture, float width, float height) {
		
		if(!glTextureCache.containsKey(texture)) {
			
			glTextureCache.put(texture, NanoVGGL2.nvglCreateImageFromHandle(nvg, texture, (int) width, - (int) height, 0));
			
			return true;
		}
		
		return true;
	}
	
	public boolean loadImage(long nvg, ResourceLocation location) {
		
		if(!imageCache.containsKey(location.getResourcePath())) {
			
			int[] width = {0};
			int[] height = {0};
			int[] channels = {0};
			
			ByteBuffer image = IOUtils.resourceToByteBuffer(location);
			
			if(image == null) {
				return false;
			}
			
			ByteBuffer buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4);
			
			if(buffer == null) {
				return false;
			}
			
			imageCache.put(location.getResourcePath(), new NVGAsset(NanoVG.nvgCreateImageRGBA(nvg, width[0], height[0], NanoVG.NVG_IMAGE_REPEATX | NanoVG.NVG_IMAGE_REPEATY | NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, buffer), width[0], height[0]));
			
			return true;
		}
		
		return true;
	}
	
	public boolean loadImage(long nvg, File file) {
		
		if(!imageCache.containsKey(file.getName())) {
			
			int[] width = {0};
			int[] height = {0};
			int[] channels = {0};
			
			ByteBuffer image = IOUtils.resourceToByteBuffer(file);
			
			if(image == null) {
				return false;
			}
			
			ByteBuffer buffer = STBImage.stbi_load_from_memory(image, width, height, channels, 4);
			
			if(buffer == null) {
				return false;
			}
			
			imageCache.put(file.getName(), new NVGAsset(NanoVG.nvgCreateImageRGBA(nvg, width[0], height[0], NanoVG.NVG_IMAGE_REPEATX | NanoVG.NVG_IMAGE_REPEATY | NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, buffer), width[0], height[0]));
			
			return true;
		}
		
		return true;
	}
	
	public boolean loadSvg(long nvg, ResourceLocation location, float width, float height) {
		
		String name = location.getResourcePath() + "-" + width + "-" + height;
		
		if(!svgCache.containsKey(name)) {
			
			try {
				
				InputStream inputStream = mc.getResourceManager().getResource(location).getInputStream();
				
				if (inputStream == null) {
					return false;
				}
				
				StringBuilder resultStringBuilder = new StringBuilder();
				
				try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        resultStringBuilder.append(line);
                    }
				}
				
                CharSequence s = resultStringBuilder.toString();
                NSVGImage svg = NanoSVG.nsvgParse(s, "px", 96f);
                
                if (svg == null) {
                	return false;
                }
                
                long rasterizer = NanoSVG.nsvgCreateRasterizer();
                
                int w = (int) svg.width();
                int h = (int) svg.height();
                
                float scale = Math.max(width / w, height / h);
                w = (int) (w * scale);
                h = (int) (h * scale);

                ByteBuffer image = MemoryUtil.memAlloc(w * h * 4);
                NanoSVG.nsvgRasterize(rasterizer, svg, 0, 0, scale, image, w, h, w * 4);

                NanoSVG.nsvgDeleteRasterizer(rasterizer);
                NanoSVG.nsvgDelete(svg);

                svgCache.put(name, new NVGAsset(NanoVG.nvgCreateImageRGBA(nvg, w, h, NanoVG.NVG_IMAGE_REPEATX | NanoVG.NVG_IMAGE_REPEATY | NanoVG.NVG_IMAGE_GENERATE_MIPMAPS, image), w, h));
                
                return true;
                
			} catch (Exception e) {
				
				SoarLogger.error("Failed to load svg", e);
				
				return false;
			}
		}
		
		return true;
	}
	
	public int getImage(ResourceLocation location) {
		return imageCache.get(location.getResourcePath()).getImage();
	}
	
	public int getImage(File file) {
		return imageCache.get(file.getName()).getImage();
	}
	
	public int getImage(int texture) {
		return glTextureCache.get(texture);
	}
	
	public void removeImage(long nvg, ResourceLocation location) {
		NanoVG.nvgDeleteImage(nvg, imageCache.get(location.getResourcePath()).getImage());
		imageCache.remove(location.getResourcePath());
	}
	
	public void removeImage(long nvg, File file) {
		NanoVG.nvgDeleteImage(nvg, imageCache.get(file.getName()).getImage());
		imageCache.remove(file.getName());
	}
	
	public int getSvg(ResourceLocation location, float width, float height) {
		
		String name = location.getResourcePath() + "-" + width + "-" + height;
		
		return svgCache.get(name).getImage();
	}
	
	public void removeSvg(long nvg, String path, float width, float height) {
		
		String name = path + "-" + width + "-" + height;
		
		NanoVG.nvgDeleteImage(nvg, svgCache.get(name).getImage());
		svgCache.remove(name);
	}
}
