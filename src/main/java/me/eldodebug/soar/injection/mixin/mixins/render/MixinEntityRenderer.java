package me.eldodebug.soar.injection.mixin.mixins.render;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.management.event.impl.EventBlockHighlightRender;
import me.eldodebug.soar.management.event.impl.EventCameraRotation;
import me.eldodebug.soar.management.event.impl.EventGamma;
import me.eldodebug.soar.management.event.impl.EventHurtCamera;
import me.eldodebug.soar.management.event.impl.EventPlayerHeadRotation;
import me.eldodebug.soar.management.event.impl.EventRender3D;
import me.eldodebug.soar.management.event.impl.EventShader;
import me.eldodebug.soar.management.event.impl.EventZoomFov;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.impl.EntityCullingMod;
import me.eldodebug.soar.management.mods.impl.MinimalViewBobbingMod;
import me.eldodebug.soar.management.mods.impl.MoBendsMod;
import me.eldodebug.soar.management.mods.impl.OldAnimationsMod;
import me.eldodebug.soar.management.mods.impl.WeatherChangerMod;
import me.eldodebug.soar.management.mods.settings.impl.ComboSetting;
import me.eldodebug.soar.management.mods.settings.impl.combo.Option;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.entity.Entity;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Shadow
    public abstract void setupViewBobbing(float partialTicks);
    
	@Shadow
	protected abstract boolean isDrawBlockOutline();
	
    @Final
    @Unique
    private final Minecraft mc = Minecraft.getMinecraft();
    
    @Unique
    private float height;
    
    @Unique
    private float eyeHeight;
    
    @Unique
    private float previousHeight;
    
	private float rotationYaw;
	private float prevRotationYaw;
	private float rotationPitch;
	private float prevRotationPitch;
	
	@Shadow
    private float thirdPersonDistance;
	
    @Inject(method = "renderWorldPass", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/EntityRenderer;renderHand:Z", shift = At.Shift.BEFORE))
    private void onRender3D(int pass, float partialTicks, long finishTimeNano, CallbackInfo ci) {
    	new EventRender3D(partialTicks).call();
    }
    
	@Redirect(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;setAngles(FF)V"))
	public void updateCameraAndRender(EntityPlayerSP entityPlayerSP, float yaw, float pitch) {
		
		EventPlayerHeadRotation event = new EventPlayerHeadRotation(yaw, pitch);
		event.call();
		
		yaw = event.getYaw();
		pitch = event.getPitch();

		if(!event.isCancelled()) {
			entityPlayerSP.setAngles(yaw, pitch);
		}
	}
	
	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V", shift = At.Shift.BEFORE))
	public void renderingBefore(float partialTicks, long nanoTime, CallbackInfo ci) {
		MoBendsMod.getInstance().setRenderingGuiScreen(true);
	}
	
	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V", shift = At.Shift.AFTER))
	public void renderingAfter(float partialTicks, long nanoTime, CallbackInfo ci) {
		MoBendsMod.getInstance().setRenderingGuiScreen(false);
	}
	
    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V"))
    private void shouldPerformCulling(CallbackInfo ci) {
    	EntityCullingMod.shouldPerformCulling = true;
    }

    @Inject(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V", shift = At.Shift.AFTER))
    private void shouldNotPerformCulling(CallbackInfo ci) {
        EntityCullingMod.shouldPerformCulling = false;
    }
    
    @Redirect(method = "hurtCameraEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GlStateManager;rotate(FFFF)V"))
    public void adjustHurtCameraEffect(float angle, float x, float y, float z) {
    	
    	EventHurtCamera event = new EventHurtCamera();
    	event.call();
    	
    	if(event.getIntensity() != 1F) {
    		angle *= event.getIntensity();
    	}
    	
    	GlStateManager.rotate(angle, x, y, z);
    }
    
	@Inject(method = "orientCamera", at = @At("HEAD"))
	public void orientCamera(float partialTicks, CallbackInfo ci) {
		
		rotationYaw = mc.getRenderViewEntity().rotationYaw;
		prevRotationYaw = mc.getRenderViewEntity().prevRotationYaw;
		rotationPitch = mc.getRenderViewEntity().rotationPitch;
		prevRotationPitch = mc.getRenderViewEntity().prevRotationPitch;
		float roll = 0;

		thirdPersonDistance = 4.0F;
		
		EventCameraRotation event = new EventCameraRotation(rotationYaw, rotationPitch, roll, thirdPersonDistance);
		event.call();
		
		thirdPersonDistance = event.getThirdPersonDistance();
		
		rotationYaw = event.getYaw();
		rotationPitch = event.getPitch();
		roll = event.getRoll();

		prevRotationYaw = event.getYaw();
		prevRotationPitch = event.getPitch();
		GlStateManager.rotate(event.getRoll(), 0, 0, 1);
	}
	
    @Redirect(method = "orientCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float modifyEyeHeight(Entity entity, float partialTicks) {
    	
    	OldAnimationsMod mod = OldAnimationsMod.getInstance();
    	
        return mod.isToggled() && mod.getSneakSetting().isToggled() ? previousHeight + (height - previousHeight) * partialTicks : entity.getEyeHeight();
    }
    
    @Redirect(method = "setupCameraTransform", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;setupViewBobbing(F)V"))
    public void setupCameraTransform(EntityRenderer entityRenderer, float f) {
    	if(!MinimalViewBobbingMod.getInstance().isToggled()) {
    		this.setupViewBobbing(f);
    	}
    }
    
    @Redirect(method = "renderWorldDirections", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getEyeHeight()F"))
    public float syncCrossHair(Entity entity, float partialTicks) {
        return modifyEyeHeight(entity, partialTicks);
    }

    @Inject(method = "updateRenderer", at = @At("HEAD"))
    public void preUpdateRenderer(CallbackInfo ci) {
    	
        Entity entity = mc.getRenderViewEntity();
        float nowEyeHeight = entity.getEyeHeight();
        
        previousHeight = height;
        
        if (nowEyeHeight < height) {
            height = nowEyeHeight;
        } else {
            height += (nowEyeHeight - height) * 0.5f;
        }

        eyeHeight = height;
    }
    
	@Inject(method = "addRainParticles", at = @At("HEAD"), cancellable = true)
	public void preAddRainParticles(CallbackInfo ci) {
		
		WeatherChangerMod mod = WeatherChangerMod.getInstance();
		ComboSetting setting = mod.getWeatherSetting();
		Option weather = setting.getOption();
		
		if(mod.isToggled() && (weather.getTranslate().equals(TranslateText.CLEAR) || 
				weather.getTranslate().equals(TranslateText.SNOW))) {
			ci.cancel();
		}
	}
	
	@Inject(method = "updateCameraAndRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;bindFramebuffer(Z)V", shift = At.Shift.BEFORE))
	public void addShaders(float partialTicks, long nanoTime, CallbackInfo callback) {
		
		EventShader event = new EventShader();
		event.call();
		
		for(ShaderGroup group : event.getGroups()) {
			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.loadIdentity();
			group.loadShaderGroup(((IMixinMinecraft)mc).getTimer().renderPartialTicks);
			GlStateManager.popMatrix();
		}
	}
	
	@Redirect(method = "updateLightmap", at = @At(value = "FIELD", target = "Lnet/minecraft/client/settings/GameSettings;gammaSetting:F"))
	public float overrideGamma(GameSettings settings) {
		
		EventGamma event = new EventGamma(settings.gammaSetting);
		event.call();
		
		return event.getGamma();
	}
	
	@Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
	public void onFovModifier(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> cir) {
		
		EventZoomFov event = new EventZoomFov(cir.getReturnValue());
		event.call();
		
		cir.setReturnValue(event.getFov());
	}
	
	@Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/EntityRenderer;isDrawBlockOutline()Z"))
	public boolean overrideCanDraw(EntityRenderer renderer) {
		return true;
	}
	
	@Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z", ordinal = 0))
	public boolean overrideWetBlockHighlight(Entity entity, Material materialIn) {
		
		boolean maybeWould = entity.isInsideOfMaterial(materialIn);
		boolean would = maybeWould && isDrawBlockOutline();
		
		EventBlockHighlightRender event = new EventBlockHighlightRender(mc.objectMouseOver, ((IMixinMinecraft)mc).getTimer().renderPartialTicks);
		event.call();
		
		if(maybeWould && event.isCancelled()) {
			return false;
		}
		
		return would;
	}

	@Redirect(method = "renderWorldPass", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z", ordinal = 1))
	public boolean overrideBlockHighlight(Entity entity, Material materialIn) {
		
		boolean totallyWouldNot = entity.isInsideOfMaterial(materialIn);
		boolean wouldNot = totallyWouldNot || !isDrawBlockOutline();
		
		EventBlockHighlightRender event = new EventBlockHighlightRender(mc.objectMouseOver, ((IMixinMinecraft)mc).getTimer().renderPartialTicks);
		event.call();
		
		if(!totallyWouldNot && event.isCancelled()) {
			return true;
		}
		
		return wouldNot;
	}
	
	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationYaw:F"))
	public float getRotationYaw(Entity entity) {
		return rotationYaw;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationYaw:F"))
	public float getPrevRotationYaw(Entity entity) {
		return prevRotationYaw;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;rotationPitch:F"))
	public float getRotationPitch(Entity entity) {
		return rotationPitch;
	}

	@Redirect(method = "orientCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/Entity;prevRotationPitch:F"))
	public float getPrevRotationPitch(Entity entity) {
		return prevRotationPitch;
	}
	
    @Inject(method = "renderStreamIndicator", at = @At("HEAD"), cancellable = true)
    private void cancelStreamIndicator(CallbackInfo ci) {
        ci.cancel();
    }
    
    @Inject(method = "renderWorldPass", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumWorldBlockLayer;TRANSLUCENT:Lnet/minecraft/util/EnumWorldBlockLayer;")),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;DILnet/minecraft/entity/Entity;)I",
            ordinal = 0))
    private void enablePolygonOffset(CallbackInfo ci) {
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(-0.325F, -0.325F);
    }

    @Inject(method = "renderWorldPass", slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/util/EnumWorldBlockLayer;TRANSLUCENT:Lnet/minecraft/util/EnumWorldBlockLayer;")),
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;renderBlockLayer(Lnet/minecraft/util/EnumWorldBlockLayer;DILnet/minecraft/entity/Entity;)I",
            ordinal = 0, shift = At.Shift.AFTER))
    private void disablePolygonOffset(CallbackInfo ci) {
        GlStateManager.disablePolygonOffset();
    }
}
