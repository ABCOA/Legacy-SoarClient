package me.eldodebug.soar.injection.mixin.mixins.chunk;

import java.io.DataInputStream;
import java.io.IOException;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

@Mixin(AnvilChunkLoader.class)
public class MixinAnvilChunkLoader {

    @Inject(method = "loadChunk", at = { @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompressedStreamTools;read(Ljava/io/DataInputStream;)Lnet/minecraft/nbt/NBTTagCompound;", shift = At.Shift.AFTER) }, locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void closeInputstream(final World worldIn, final int x, final int z, final CallbackInfoReturnable<Chunk> cir, final ChunkCoordIntPair pair, final NBTTagCompound nbt, final DataInputStream inputStream) throws IOException {
        inputStream.close();
    }
}
