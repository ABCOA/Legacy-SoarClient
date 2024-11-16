package me.eldodebug.soar.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldSettings;

public class PlayerUtils {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	
    public static Map<Integer, Float> MODIFIER_BY_TICK = new HashMap<>();
    
    static {
        MODIFIER_BY_TICK.put(0, 0.0F);
        MODIFIER_BY_TICK.put(1, 0.00037497282f);
        MODIFIER_BY_TICK.put(2, 0.0015000105f);
        MODIFIER_BY_TICK.put(3, 0.0033749938f);
        MODIFIER_BY_TICK.put(4, 0.0059999824f);
        MODIFIER_BY_TICK.put(5, 0.009374976f);
        MODIFIER_BY_TICK.put(6, 0.013499975f);
        MODIFIER_BY_TICK.put(7, 0.01837498f);
        MODIFIER_BY_TICK.put(8, 0.023999989f);
        MODIFIER_BY_TICK.put(9, 0.030375004f);
        MODIFIER_BY_TICK.put(10, 0.037500024f);
        MODIFIER_BY_TICK.put(11, 0.04537499f);
        MODIFIER_BY_TICK.put(12, 0.05400002f);
        MODIFIER_BY_TICK.put(13, 0.063374996f);
        MODIFIER_BY_TICK.put(14, 0.07349998f);
        MODIFIER_BY_TICK.put(15, 0.084375024f);
        MODIFIER_BY_TICK.put(16, 0.096000016f);
        MODIFIER_BY_TICK.put(17, 0.10837501f);
        MODIFIER_BY_TICK.put(18, 0.121500015f);
        MODIFIER_BY_TICK.put(19, 0.13537502f);
        MODIFIER_BY_TICK.put(20, 0.14999998f);
    }
    
	public static boolean hasItem(Item item) {
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem().equals(item)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static float getSpeed() {
		
		double distTraveledLastTickX = mc.thePlayer.posX - mc.thePlayer.prevPosX;
		double distTraveledLastTickZ = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
		double currentSpeed = MathHelper.sqrt_double(distTraveledLastTickX * distTraveledLastTickX
				+ distTraveledLastTickZ * distTraveledLastTickZ);
		
		return (float) (currentSpeed / 0.05);
	}
	
	public static int getItemSlot(Item item) {
		
		int slot = -1;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem().equals(item)) {
				slot = i;
				break;
			}
		}
		
		if(slot == -1) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	public static int getBestBow(Entity entity) {
		
		int slot = -1;
		ItemStack bestBow = null;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem() instanceof ItemBow) {
				if(bestBow == null || getBowStrength(itemStack) > getBowStrength(bestBow)) {
					bestBow = itemStack;
					slot = i;
				}
			}
		}
		
		if(slot == -1 || bestBow == null) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	public static int getBestSword(Entity entity) {
		
		int slot = -1;
		ItemStack bestSword = null;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem() instanceof ItemSword) {
				if(bestSword == null || getSwordStrength(itemStack) > getSwordStrength(bestSword)) {
					bestSword = itemStack;
					slot = i;
				}
			}
		}
		
		if(slot == -1 || bestSword == null) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	public static int getBestAxe(Entity entity) {
		
		int slot = -1;
		ItemStack bestAxe = null;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem() instanceof ItemAxe) {
				if(bestAxe == null || getToolEfficiency(itemStack) > getToolEfficiency(bestAxe)) {
					bestAxe = itemStack;
					slot = i;
				}
			}
		}
		
		if(slot == -1 || bestAxe == null) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	public static int getBestPickaxe(Entity entity) {
		
		int slot = -1;
		ItemStack bestPickaxe = null;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem() instanceof ItemPickaxe) {
				if(bestPickaxe == null || getToolEfficiency(itemStack) > getToolEfficiency(bestPickaxe)) {
					bestPickaxe = itemStack;
					slot = i;
				}
			}
		}
		
		if(slot == -1 || bestPickaxe == null) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	public static int getBestBlock(Entity entity) {
		
		int slot = -1;
		ItemBlock bestBlock = null;
		
		boolean wool = false;
		boolean planks = false;
		boolean cobblestone = false;
		
		for(int i = 0; i < 9; i++) {
			
			ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
			
			if(itemStack != null && itemStack.getItem() instanceof ItemBlock) {
			
				ItemBlock itemBlock = (ItemBlock) itemStack.getItem();
				Block block = itemBlock.getBlock();
				
				if(bestBlock == null) {
					bestBlock = itemBlock;
					slot = i;
				}
				
				if(bestBlock != null) {
					
					if(!wool && block.equals(Blocks.wool)) {
						wool = true;
						bestBlock = itemBlock;
						slot = i;
						continue;
					}
					
					if(!wool && !planks && block.equals(Blocks.planks)) {
						planks = true;
						bestBlock = itemBlock;
						slot = i;
						continue;
					}
					
					if(!wool && !planks && !cobblestone && block.equals(Blocks.cobblestone)) {
						cobblestone = false;
						bestBlock = itemBlock;
						slot = i;
						continue;
					}
				}
			}
		}
		
		if(slot == -1 || bestBlock == null) {
			return mc.thePlayer.inventory.currentItem;
		}
		
		return slot;
	}
	
	private static float getBowStrength(ItemStack stack) {
		
		if(stack.getItem() instanceof ItemBow) {
			
			ItemBow bow = (ItemBow) stack.getItem();
			float power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack) * 1.5F;
			float flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 1.2F;
			
			return bow.getMaxDamage() + power + flame;
		}
		
		return 0;
	}
	
    private static float getSwordStrength(ItemStack stack) {
    	
        if (stack.getItem() instanceof ItemSword) {
        	
            ItemSword sword = (ItemSword) stack.getItem();
            float sharpness = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack) * 1.25F;
            float fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack) * 1.5F;
            
            return sword.getDamageVsEntity() + sharpness + fireAspect;
        }
        
        return 0;
    }
    
    private static float getToolEfficiency(ItemStack stack) {
    	
    	if(stack.getItem() instanceof ItemAxe) {
    		
    		ItemAxe axe = (ItemAxe) stack.getItem();
    		
    		float efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 1.25F;
    		
    		return axe.getStrVsBlock(stack, Blocks.planks) + efficiency;
    	}
    	
    	if(stack.getItem() instanceof ItemPickaxe) {
    		
    		ItemPickaxe pickaxe = (ItemPickaxe) stack.getItem();
    		
    		float efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack) * 1.25F;
    		
    		return pickaxe.getStrVsBlock(stack, Blocks.stone) + efficiency;
    	}
    	
    	return 0;
    }
	
    public static int getPotionsFromInventory(Potion inputPotion) {
    	
        int count = 0;

        for (int i = 1; i < 45; ++i) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getHasStack()) {
                ItemStack is = mc.thePlayer.inventoryContainer.getSlot(i).getStack();
                Item item = is.getItem();

                if (item instanceof ItemPotion) {
                    ItemPotion potion = (ItemPotion) item;

                    if (potion.getEffects(is) != null) {
                        Iterator<PotionEffect> iterator = potion.getEffects(is).iterator();

                        while (iterator.hasNext()) {
                            Object o = iterator.next();
                            PotionEffect effect = (PotionEffect) o;

                            if (effect.getPotionID() == inputPotion.id) {
                                ++count;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }
    
    public static boolean isSpectator(){
        NetworkPlayerInfo networkplayerinfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == WorldSettings.GameType.SPECTATOR;
    }
    
    public static boolean isCreative(){
        NetworkPlayerInfo networkplayerinfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == WorldSettings.GameType.CREATIVE;
    }
    
    public static boolean isSurvival(){
        NetworkPlayerInfo networkplayerinfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getGameProfile().getId());
        return networkplayerinfo != null && networkplayerinfo.getGameType() == WorldSettings.GameType.SURVIVAL;
    }
}
