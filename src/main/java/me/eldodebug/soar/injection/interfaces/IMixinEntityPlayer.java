package me.eldodebug.soar.injection.interfaces;

import me.eldodebug.soar.management.mods.impl.WaveyCapesMod;
import me.eldodebug.soar.management.mods.impl.skin3d.render.CustomizableModelPart;
import me.eldodebug.soar.management.mods.impl.waveycapes.sim.StickSimulation;
import me.eldodebug.soar.management.mods.impl.waveycapes.sim.StickSimulation.Point;
import me.eldodebug.soar.management.mods.impl.waveycapes.sim.StickSimulation.Stick;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;

public interface IMixinEntityPlayer {
	
	public CustomizableModelPart getHeadLayers();
	public void setupHeadLayers(CustomizableModelPart box);
	public CustomizableModelPart[] getSkinLayers();
	public void setupSkinLayers(CustomizableModelPart[] box);
	
    public StickSimulation getSimulation();
    
    public default void updateSimulation(EntityPlayer abstractClientPlayer, int partCount) {
    	
        StickSimulation simulation = getSimulation();
        boolean dirty = false;
        
        if(simulation.points.size() != partCount) {
        	
            simulation.points.clear();
            simulation.sticks.clear();
            
            for (int i = 0; i < partCount; i++) {
                Point point = new Point();
                point.position.y = -i;
                point.locked = i == 0;
                simulation.points.add(point);
                if(i > 0) {
                    simulation.sticks.add(new Stick(simulation.points.get(i-1), point, 1f));
                }
            }
            dirty = true;
        }
        
        if(dirty) {
            for(int i = 0; i < 10; i++) {
                simulate(abstractClientPlayer);
            }
        }
    }
    
    public default void simulate(EntityPlayer abstractClientPlayer) {
    	
        StickSimulation simulation = getSimulation();
        
        if(simulation.points.isEmpty()) {
            return;
        }
        
        simulation.points.get(0).prevPosition.copy(simulation.points.get(0).position);
        double d = abstractClientPlayer.chasingPosX
                - abstractClientPlayer.posX;
        double m = abstractClientPlayer.chasingPosZ
                - abstractClientPlayer.posZ;
        float n = abstractClientPlayer.prevRenderYawOffset + abstractClientPlayer.renderYawOffset - abstractClientPlayer.prevRenderYawOffset;
        double o = Math.sin(n * 0.017453292F);
        double p = -Math.cos(n * 0.017453292F);
        float heightMul = WaveyCapesMod.getInstance().getHeightMultiplierSetting().getValueInt();
        double fallHack = MathHelper.clamp_double((simulation.points.get(0).position.y - (abstractClientPlayer.posY*heightMul)), 0d, 1d); 
        
        simulation.points.get(0).position.x += (d * o + m * p) + fallHack;
        simulation.points.get(0).position.y = (float) (abstractClientPlayer.posY*heightMul + (abstractClientPlayer.isSneaking() ? -4 : 0));
        simulation.simulate();
    }
}
