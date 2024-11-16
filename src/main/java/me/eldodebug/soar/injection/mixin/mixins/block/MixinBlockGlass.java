package me.eldodebug.soar.injection.mixin.mixins.block;

import org.spongepowered.asm.mixin.Mixin;

import me.eldodebug.soar.management.mods.impl.ClearGlassMod;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

@Mixin(BlockGlass.class)
public class MixinBlockGlass extends Block {

	protected MixinBlockGlass(Material materialIn) {
		super(materialIn);
	}

    @Override
    public boolean shouldSideBeRendered(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
    	
    	ClearGlassMod clearGlass = ClearGlassMod.getInstance();
    	
    	return (!clearGlass.isToggled() || (clearGlass.isToggled() && !clearGlass.getNormalSetting().isToggled()) 
    			&& super.shouldSideBeRendered(worldIn, pos, side));
    }
}
