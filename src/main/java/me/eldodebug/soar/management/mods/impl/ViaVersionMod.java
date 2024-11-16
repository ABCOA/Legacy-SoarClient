package me.eldodebug.soar.management.mods.impl;

import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.utils.Multithreading;
import me.eldodebug.soar.viaversion.ViaLoadingBase;
import me.eldodebug.soar.viaversion.ViaSoar;
import me.eldodebug.soar.viaversion.protocolinfo.ProtocolInfo;

public class ViaVersionMod extends Mod {

	private static ViaVersionMod instance;
	
	private boolean loaded;
	
	public ViaVersionMod() {
		super(TranslateText.VIA_VERSION, TranslateText.VIA_VERSION_DESCRIPTION, ModCategory.OTHER);
		
		instance = this;
		loaded = false;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		if(!loaded) {
			loaded = true;
			Multithreading.runAsync(() -> {
				ViaSoar.create();
				ViaSoar.getInstance().initAsyncSlider();
			});
		}
	}
	
	@Override
	public void onDisable() {
		
		super.onDisable();
		
		if(loaded) {
			ViaSoar.getInstance().getAsyncVersionSlider().setVersion(ProtocolInfo.R1_8.getProtocolVersion().getVersion());
			ViaLoadingBase.getInstance().reload(ProtocolInfo.R1_8.getProtocolVersion());
		}
	}

	public static ViaVersionMod getInstance() {
		return instance;
	}

	public boolean isLoaded() {
		return loaded;
	}
}
