package me.eldodebug.soar.management.mods.impl;

import java.util.Map;
import java.util.WeakHashMap;

import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventPreRenderChunk;
import me.eldodebug.soar.management.event.impl.EventRenderChunkPosition;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.NumberSetting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.chunk.RenderChunk;

public class ChunkAnimatorMod extends Mod {

	private final Map<RenderChunk, Long> chunks = new WeakHashMap<>();
	
	private NumberSetting duration = new NumberSetting(TranslateText.DURATION, this, 1, 0, 5, true);
	
	public ChunkAnimatorMod() {
		super(TranslateText.CHUNK_ANIMATOR, TranslateText.CHUNK_ANIMATOR_DESCRIPTION, ModCategory.RENDER);
	}

	@EventTarget
	public void preRenderChunk(EventPreRenderChunk event) {
		if(chunks.containsKey(event.getRenderChunk())) {
			
			long time = chunks.get(event.getRenderChunk());
			long now = System.currentTimeMillis();

			if(time == -1L) {
				chunks.put(event.getRenderChunk(), now);
				time = now;
			}

			long passedTime = now - time;

			if(passedTime < (int) (duration.getValue() * 1000)) {
				int chunkY = event.getRenderChunk().getPosition().getY();
				GlStateManager.translate(0, -chunkY + this.easeOut(passedTime, 0, chunkY, (int) (duration.getValue() * 1000)), 0);
			}
		}
	}
	
	@EventTarget
	public void setPosition(EventRenderChunkPosition event) {
		if(mc.thePlayer != null) {
			chunks.put(event.getRenderChunk(), -1L);
		}
	}

	private float easeOut(float t,float b , float c, float d) {
		return c * (float)Math.sin(t/d * (Math.PI/2)) + b;	
	}
}
