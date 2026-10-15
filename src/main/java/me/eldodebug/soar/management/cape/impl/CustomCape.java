package me.eldodebug.soar.management.cape.impl;

import me.eldodebug.soar.management.cape.CapeCategory;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class CustomCape extends Cape {

	private File sample;
	private final File sourceFile;
	private final byte[] png;
	private final String sha256;
	private final String syncError;
	
	public CustomCape(String name, File sample, ResourceLocation cape, boolean premium, CapeCategory category) {
		this(name, sample, sample, cape, premium, category, null, null, null);
	}

	public CustomCape(String name, File sample, File sourceFile, ResourceLocation cape, boolean premium,
			CapeCategory category, byte[] png, String sha256, String syncError) {
		super(name, cape, premium, category, "custom:" + sourceFile.getName());
		this.sample = sample;
		this.sourceFile = sourceFile;
		this.png = png == null ? null : png.clone();
		this.sha256 = sha256;
		this.syncError = syncError;
	}

	public File getSourceFile() { return sourceFile; }
	public byte[] getNormalizedPng() { return png == null ? null : png.clone(); }
	public String getSha256() { return sha256; }
	public String getSyncError() { return syncError; }

	public File getSample() {
		return sample;
	}
}
