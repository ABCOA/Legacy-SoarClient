package me.eldodebug.soar.injection.mixin.mixins.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import me.eldodebug.soar.viaversion.fixes.FixedSoundEngine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

@Mixin(ItemBlock.class)
public class MixinItemBlock {

	@Overwrite
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		return FixedSoundEngine.onItemUse((ItemBlock)(Object)this, stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
	}
}
