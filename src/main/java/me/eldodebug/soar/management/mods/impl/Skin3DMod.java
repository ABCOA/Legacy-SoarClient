package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;

public class Skin3DMod extends Mod {

	private static Skin3DMod instance;
	
	private float baseVoxelSize = 1.15F;
	private float bodyVoxelWidthSize = 1.05F;
	private float headVoxelSize = 1.18F;

	private int renderDistanceLOD = 14;
	
	public Skin3DMod() {
		super(TranslateText.SKIN_3D, TranslateText.SKIN_3D_DESCRIPTION, ModCategory.RENDER);
		
		instance = this;
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		if(MoBendsMod.getInstance().isToggled()) {
			MoBendsMod.getInstance().setToggled(false);
		}
	}

	public static Skin3DMod getInstance() {
		return instance;
	}

	public float getBaseVoxelSize() {
		return baseVoxelSize;
	}

	public float getBodyVoxelWidthSize() {
		return bodyVoxelWidthSize;
	}

	public float getHeadVoxelSize() {
		return headVoxelSize;
	}

	public int getRenderDistanceLOD() {
		return renderDistanceLOD;
	}
}
