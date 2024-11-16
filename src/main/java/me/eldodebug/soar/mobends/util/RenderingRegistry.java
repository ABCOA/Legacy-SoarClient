package me.eldodebug.soar.mobends.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;

public class RenderingRegistry {
	
    private static final RenderingRegistry INSTANCE = new RenderingRegistry();

    private Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderersOld = Maps.newHashMap();

    public static void registerEntityRenderingHandler(Class<? extends Entity> entityClass, Render<? extends Entity> renderer) {
        INSTANCE.entityRenderersOld.put(entityClass, renderer);
    }

    public static void loadEntityRenderers(Map<Class<? extends Entity>, Render<? extends Entity>> entityRenderMap) {
        entityRenderMap.putAll(INSTANCE.entityRenderersOld);
    }
}