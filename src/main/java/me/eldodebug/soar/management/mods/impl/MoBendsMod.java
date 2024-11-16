package me.eldodebug.soar.management.mods.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.lwjgl.util.vector.Vector3f;

import me.eldodebug.soar.injection.interfaces.IMixinMinecraft;
import me.eldodebug.soar.management.event.EventTarget;
import me.eldodebug.soar.management.event.impl.EventPreRenderTick;
import me.eldodebug.soar.management.event.impl.EventRenderPlayer;
import me.eldodebug.soar.management.event.impl.EventTick;
import me.eldodebug.soar.management.language.TranslateText;
import me.eldodebug.soar.management.mods.Mod;
import me.eldodebug.soar.management.mods.ModCategory;
import me.eldodebug.soar.management.mods.settings.impl.BooleanSetting;
import me.eldodebug.soar.management.mods.settings.impl.ColorSetting;
import me.eldodebug.soar.mobends.AnimatedEntity;
import me.eldodebug.soar.mobends.client.model.entity.ModelBendsPlayer;
import me.eldodebug.soar.mobends.client.renderer.entity.RenderBendsPlayer;
import me.eldodebug.soar.mobends.data.Data_Player;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class MoBendsMod extends Mod {

	private static MoBendsMod instance;
	
    public List<UUID> currentlyRenderedEntities = new ArrayList<UUID>();
    
	private boolean loaded, renderingGuiScreen;
	
	private BooleanSetting customColorSetting = new BooleanSetting(TranslateText.CUSTOM_COLOR, this, false);
	private ColorSetting colorSetting = new ColorSetting(TranslateText.COLOR, this, Color.RED, false);
	
	public MoBendsMod() {
		super(TranslateText.MO_BENDS, TranslateText.MO_BENDS_DESCRIPTION, ModCategory.PLAYER);
		
		instance = this;
		loaded = false;
		renderingGuiScreen = false;
	}

	@EventTarget
	public void onPreRenderTick(EventPreRenderTick event) {
		
		if(mc.theWorld == null) {
			return;
		}
		
        for(int i = 0;i < Data_Player.dataList.size();i++){
            Data_Player.dataList.get(i).update(((IMixinMinecraft)mc).getTimer().renderPartialTicks);
        }
	}
	
	@EventTarget
	public void onTick(EventTick event) {
		
        if(mc.theWorld == null){
            return;
        }
        
        for(int i = 0;i < Data_Player.dataList.size();i++){
        	
            Data_Player data = Data_Player.dataList.get(i);
            Entity entity = mc.theWorld.getEntityByID(data.entityID);
            
            if(entity != null){
                if(!data.entityType.equalsIgnoreCase(entity.getName())){
                    Data_Player.dataList.remove(data);
                    Data_Player.add(new Data_Player(entity.getEntityId()));
                }else{

                    data.motion_prev.set(data.motion);

                    data.motion.x=(float) entity.posX-data.position.x;
                    data.motion.y=(float) entity.posY-data.position.y;
                    data.motion.z=(float) entity.posZ-data.position.z;

                    data.position = new Vector3f((float)entity.posX,(float)entity.posY,(float)entity.posZ);
                }
            }else{
                Data_Player.dataList.remove(data);
            }
        }
	}
	
	@EventTarget
	public void onRenderPlayer(EventRenderPlayer event) {
		
        if(!(event.getEntity() instanceof EntityPlayer)){
            return;
        }

        if(AnimatedEntity.getByEntity(event.getEntity()) == null){
            return;
        }

        if(AnimatedEntity.getByEntity(event.getEntity()).animate){
            AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();

            if(!currentlyRenderedEntities.contains(event.getEntity().getUniqueID())){
                currentlyRenderedEntities.add(event.getEntity().getUniqueID());
                event.setCancelled(true);

                RenderBendsPlayer renderer = AnimatedEntity.getPlayerRenderer(player);
                ModelBendsPlayer model = (ModelBendsPlayer) renderer.getMainModel();

                model.bipedHead.isHidden = false;
                model.bipedHeadwear.isHidden = false;

                float entityYaw = event.getEntity().prevRotationYaw + (event.getEntity().rotationYaw - event.getEntity().prevRotationYaw) * event.getPartialTicks();
                AnimatedEntity.getPlayerRenderer(player).doRender(player, event.getX(), event.getY(), event.getZ(), entityYaw, event.getPartialTicks());
                currentlyRenderedEntities.remove(event.getEntity().getUniqueID());
            }
        }
	}
	
	@Override
	public void onEnable() {
		super.onEnable();
		
		if(Skin3DMod.getInstance().isToggled()) {
			Skin3DMod.getInstance().setToggled(false);
		}
		
		if(WaveyCapesMod.getInstance().isToggled()) {
			WaveyCapesMod.getInstance().setToggled(false);
		}
		
		if(!loaded) {
			AnimatedEntity.register();
		}
	}
	
	public static MoBendsMod getInstance() {
		return instance;
	}

	public ColorSetting getColorSetting() {
		return colorSetting;
	}

	public BooleanSetting getCustomColorSetting() {
		return customColorSetting;
	}

	public boolean isRenderingGuiScreen() {
		return renderingGuiScreen;
	}

	public void setRenderingGuiScreen(boolean renderingGuiScreen) {
		this.renderingGuiScreen = renderingGuiScreen;
	}
}
