package me.eldodebug.soar.management.nanovg.asset;

public class NVGAsset {

	private int image;
	private int width;
	private int height;
	
	public NVGAsset(int image, int width, int height) {
		this.image = image;
		this.width = width;
		this.height = height;
	}

	public int getImage() {
		return image;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
