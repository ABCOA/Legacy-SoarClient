package me.eldodebug.soar.injection.interfaces;

import me.eldodebug.soar.management.mods.impl.skin3d.layers.BodyLayerFeatureRenderer;
import me.eldodebug.soar.management.mods.impl.skin3d.layers.HeadLayerFeatureRenderer;

public interface IMixinRenderPlayer {
	public boolean hasThinArms();
	public HeadLayerFeatureRenderer getHeadLayer();
	public BodyLayerFeatureRenderer getBodyLayer();
}